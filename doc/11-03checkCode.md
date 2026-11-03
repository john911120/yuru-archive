# 2026-11-03 コード点検・セキュリティ修正記録

## 概要

yuru-archive のソースコードを点検し、確認された問題を修正しました。

- 対象: Javaソース、Thymeleafテンプレート、テストコード
- 対象外: 設定ファイル（`application*.properties`、`.env`、`docker-compose.yml`、`Dockerfile`、`build.gradle` など）とAPIキー関連の箇所。今回は変更していません
- ローカル版（yuruArchive_Local）にも同一の修正を適用済みです

## 修正一覧

| No | 重要度 | 内容 | 主な修正ファイル |
|---|---|---|---|
| 1 | 高 | 例外メッセージ経由の反射型XSS | `exception/GlobalExceptionHandler.java` |
| 2 | 高 | 画像のDecompression bomb（メモリ枯渇） | `util/FileValidator.java` |
| 3 | 中 | 回答の添付ファイルが他人の質問に紐づく | `answer/AnswerController.java` |
| 4 | 中 | ユーザー名 "admin" で ROLE_ADMIN が付与される | `user/UserSecurityService.java`, `user/UserController.java` |
| 5 | 中 | 会員登録時のパスワード長制限なし | `user/UserCreateForm.java` |
| 6 | 中 | ログイン試行制限: パスワードスプレー未対策、保存件数の上限なし | `user/LoginAttemptService.java` |
| 7 | 中 | 未完成のリンクカードプレビューAPIが公開状態 / カード数無制限 | `linkpreview/controller/LinkCardController.java`（削除）, `linkpreview/service/LinkCardRenderService.java` |
| 8 | 中 | 質問削除後も添付ファイルの実体が公開されたまま残る | `attach/service/AttachService(Impl).java`, `question/QuestionService.java` |
| 9 | 低 | Content-Security-Policyなし / H2 ConsoleのCSRF除外が残存 | `SecurityConfig.java` |
| 10 | 低 | ログインジェクション、負のページ番号、本文長の上限なし、郵便番号未入力時のNPE | `util/LogSanitizer.java`（新規）ほか |
| 11 | 低 | 回答の入力エラー時に500エラー / 質問・回答本文が空で表示される | `answer/AnswerController.java`, `templates/question_detail.html` |
| 12 | - | 存在しないパスのテストで404ではなく500になる（既存不具合） | `exception/GlobalExceptionHandler.java` |

---

## 1. 例外メッセージ経由の反射型XSS【高】

### 問題
`GlobalExceptionHandler.handleBadRequest()` が `ex.getMessage()` をそのまま応答本文として返していました。
`MethodArgumentTypeMismatchException` などのメッセージには利用者の入力値が含まれます。
例: `For input string: "<img src=x onerror=alert(1)>"`

`ResponseEntity<String>` はブラウザのAcceptヘッダー（text/html優先）に合わせて `text/html` で返される可能性があり、
次のようなURLを踏ませるだけでスクリプトが実行される恐れがありました。

```
/question/list?page=<img src=x onerror=alert(1)>
/question/detail/<svg onload=...>
/cards/preview?url=<...>   （ログイン不要）
```

### 修正
- `MethodArgumentTypeMismatchException` / `MissingServletRequestParameterException` / `IllegalArgumentException` には固定メッセージ「リクエストの内容が正しくありません。」のみを返すようにした
- 全てのハンドラーで `Content-Type: text/plain;charset=UTF-8` を明示した
- `InvalidUploadFileException` は専用ハンドラーに分離した。サーバー側で定義した固定文言のみを返す
- HTTPステータスコードは従来どおり（400 / 403 / 404 / 405 / 500）

### テスト
- 追加: `exception/GlobalExceptionHandlerXssTest.java`（入力値を返さないこと、text/plainであること）
- 既存: `GlobalExceptionHandlerTest`、`StateChangeHttpContractTest`（不正なIDで400）はそのまま通る想定

---

## 2. 画像のDecompression bomb【高】

### 問題
`FileValidator.validateDecodableImage()` の `ImageIO.read()` と、保存後の `Thumbnailator.createThumbnail()` が画像全体を展開していました。
ファイルサイズが小さくても、幅・高さが巨大な画像で大量のメモリを消費します。

ローカル検証結果（JDK 21、ヒープ1GB）:

| 画像 | ファイルサイズ | 修正前 | 修正後 |
|---|---|---|---|
| 12000×12000 PNG | 約410KB | 受け入れる（展開時に約430MBを確保し、サムネイル生成で再度展開） | 即時に拒否 |
| 同上を4件同時アップロード | - | 2件受理・2件はメモリ不足で失敗 | 4件とも即時に拒否 |
| 20000×20000 PNG | 約1.1MB | JDK内部でOutOfMemoryErrorが発生（例外に変換されて拒否） | 即時に拒否 |

### 修正
- デコード前に `ImageReader` で画像ヘッダーの幅・高さのみを読み取り、上限を超える画像を拒否する `validateDimensions()` を追加した
  - 縦・横それぞれ最大 8000px、総ピクセル数は最大 4,000万
- WebP（ImageIOではデコードしない）も VP8X / VP8L / VP8 ヘッダーから幅・高さを読み取り、同じ上限で確認するようにした
- 通常サイズの画像（例: 4000×3000）は従来どおりアップロードできる

### テスト
- 追加: `util/FileValidatorDimensionTest.java`（30000×30000を宣言したPNGを拒否）
- 既存の `FileValidatorTest`（正常PNG/JPEG受理、偽装・破損ファイル拒否）は、スタブ環境で同等の確認済み

---

## 3. 回答の添付ファイルが他人の質問に紐づく【中】

### 問題
`AnswerController.createAnswer()` が `attachService.uploadFiles(uploadFiles, question, siteUser)` を呼んでいました。
そのため回答者のファイルが「質問」の添付として保存され、次の状態になっていました。

- ログインしていれば誰でも他人の質問詳細画面に画像を追加できる
- 質問者は、そのファイルを削除できない（削除処理が所有者IDを確認するため）

### 修正
- 回答作成時にファイルが送信された場合は `InvalidUploadFileException`（400）で拒否するようにした
- 画面の回答フォームには元々ファイル入力欄がないため、通常操作への影響はない
- 回答に画像を添付する機能が必要な場合は、回答に紐づく添付モデルを別途設計すること

---

## 4. ユーザー名 "admin" で ROLE_ADMIN が付与される【中】

### 問題
`UserSecurityService` が `"admin".equals(username)` の場合に ROLE_ADMIN を付与していました。
会員登録が公開されているため、"admin" が未登録なら誰でも登録して管理者権限を得られます。
現時点では ROLE_ADMIN を必要とする機能がないため、管理機能を追加した時点で権限昇格の脆弱性になります。

### 修正
- 全ユーザーに `ROLE_USER` を付与するようにした（ユーザー名による判定を撤去）
- 会員登録で、管理者と誤認されやすいユーザー名を禁止した（`admin` で始まるもの、`administrator` / `root` / `system` / `superuser` / `manager` / `operator`。大文字・小文字は区別しない）
- 既存のユーザーはそのままログインできる

### 今後の対応
管理機能を追加する場合は、`SiteUser` に role カラムを追加し、DBの値から権限を決定すること。
DBスキーマ変更が必要なため、今回は実施していない。

---

## 5. 会員登録時のパスワード長制限なし【中】

### 問題
`UserCreateForm.password1` には `@NotEmpty` しかなく、1文字のパスワードでも登録できました。
パスワード変更画面（`UserPasswordForm`）は8文字以上で、制限が一致していませんでした。

### 修正
- `@Size(min = 8, max = 100)` を追加した（パスワード変更画面と同じ条件）
- 既存ユーザーのパスワードには影響しない

---

## 6. ログイン試行制限の強化【中】

### 問題
- 制限のキーが「ユーザー名|IP」のみだった。ユーザー名を変えながら試行するパスワードスプレー攻撃を防げない
- 保存先が上限なしの `ConcurrentHashMap` だった。ランダムなユーザー名で失敗を繰り返されるとメモリが増え続ける

### 修正
- 従来の「ユーザー名|IP」単位（10分間に5回失敗で10分ロック）はそのまま残した
- 「IP」単位の制限を追加した（10分間に20回失敗で、そのIPからのログインを10分ロック）
  - IP単位のカウンターは、1回のログイン成功ではリセットしない
- 保存先を Caffeine キャッシュに変更した（最大10万件、20分で自動削除）。Caffeine は既存の依存関係で、追加ライブラリはない
- 公開メソッド（`loginFailed` / `isBlocked` / `loginSucceeded`）のシグネチャは変更していない

### 注意
同じグローバルIPを共有する環境（社内ネットワークなど）では、IP単位の制限を他の利用者と共有します。
必要に応じて `MAX_FAILURES_PER_IP` を調整してください。

### テスト
- 追加: `user/LoginAttemptServiceIpLimitTest.java`
- 既存: `LoginAttemptServiceTest`、`LoginRateLimitFilterTest` は従来どおり通る（スタブ環境で同等の確認済み）

---

## 7. リンクカード関連【中】

### 問題
- `LinkCardController`（`/cards/preview`、`/cards/preview/fragment`）はログイン不要で公開されていた。しかし返却テンプレート（`card-preview`、`cards/_card`）が存在せず、常に500エラーになっていた。それでも外部API（Microlink）呼出しは先に実行されるため、匿名利用者が外部APIの利用枠を消費できた。さらに1のXSSの経路にもなっていた
- 質問詳細の表示時、`[[linkcard ...]]` 1件ごとに最大5秒の同期呼出しが発生する。大量のカードを含む投稿1件でリクエストスレッドを占有できた

### 修正
- 未使用で動作もしていない `LinkCardController` を削除した。質問本文のリンクカード表示（`LinkCardRenderService`）は従来どおり動作する
- 1投稿あたりのリンクカード表示を5件までに制限した。6件目以降は「リンクカードは1つの投稿につき5件まで表示されます。」と表示する

---

## 8. 質問削除後も添付ファイルの実体が残る【中】

### 問題
`QuestionService.deleteQuestionWithFiles()` はDBの添付レコードのみ削除していました。
実ファイル（原本・サムネイル）はディスクに残り、ログイン不要の `/upload/**` でURLを知っていれば閲覧できる状態でした。

### 修正
- `AttachService.deleteFilesByQuestionId()` を追加した。DBレコードと実ファイル（原本・`s_` サムネイル）の両方を削除する
- 実ファイルはTransactionのCommit後に削除する。DB削除がRollbackされた場合にファイルだけ消える事態を防ぐ
- 保存パスは既存の `resolveStoredFile()`（アップロード領域外の参照を拒否）で解決する
- 既存の `deleteFileRecordsByQuestionId()` は互換性のために残した

---

## 9. SecurityConfig【低】

### 修正
- Content-Security-Policy を追加した（XSSが発生した場合の被害軽減）
  - 外部スクリプトの読込、`object`、`base` の改変、外部へのフォーム送信、他サイトからのframe埋込を禁止
  - 画像は `https:` を許可（Markdown本文の外部画像・リンクカード用）
  - 郵便番号検索のため `connect-src` に `https://zipcloud.ibsnet.co.jp` を許可
  - 既存テンプレートにインラインscript / `onclick` / style属性が残っているため、現時点では `'unsafe-inline'` を許可している。将来的にはスクリプトを外部ファイル化し、nonce方式に移行すること
- 未使用のH2 Console（`/h2-console/**`）のCSRF除外を撤去した。全エンドポイントでCSRF保護が有効になる

---

## 10. その他【低】

| 内容 | 修正 |
|---|---|
| ログインジェクション | `util/LogSanitizer.java` を新規追加。利用者入力（ユーザー名、検索語、ファイル名、URL、メール）をログ出力する前に改行・制御文字を除去し、100文字に制限する |
| 負のページ番号 | `QuestionService.getList()` で `Math.max(page, 0)` に補正（従来は例外が発生していた） |
| 本文長の上限なし | 質問本文を20,000文字、回答を10,000文字までに制限（閲覧毎にMarkdown変換するため） |
| 郵便番号未入力時のNPE | `UserService.create()` で既存の `normalizeZipcode()` を使用するように変更 |

---

## 11. 回答入力エラー時の500エラー【低・既存不具合】

### 問題
回答の入力エラー時、`AnswerController` は `question_detail` を再描画します。
このときモデルに `answerHtmlMap` がないため、回答が1件以上ある質問では `answerHtmlMap[answer.id]` の評価で500エラーになっていました。
今回、本文長の上限を追加したことで、この経路に入る機会が増えるため対応しました。

### 修正
- `AnswerController` の入力エラー時の処理で、通常の詳細画面（`QuestionController.detail()`）と同じモデル（`htmlBody`、`uploadedFiles`、`answerHtmlMap`）を設定するようにした
  - 従来は `question` のみ設定していたため、質問本文・添付画像・回答本文が空で表示されていた（画面確認で判明）
  - `AnswerController` に `LinkCardRenderService` と `CommonUtil` を追加したため、`StateChangeHttpContractTest` のコンストラクタ呼出しも合わせて修正した
- `question_detail.html` で `answerHtmlMap` が null の場合は空文字を表示するようにした（再発時に500エラーとしないための安全策）

---

## 12. 存在しないパスで500エラーになる【既存不具合】

### 問題
`StateChangeHttpContractTest.unknownPathReturns404InsteadOf500()` が失敗していました（期待値404、実際は500）。
このテストは 2026-10-09 に追加されましたが、当時の環境ではGradleテストを実行できず、今回初めて実行して判明しました。

- 実際のアプリケーションでは、Spring Bootの静的リソースハンドラーが全パスを受けるため、存在しないパスでは `NoResourceFoundException` が発生し、404になる（正常）
- テストの `MockMvcBuilders.standaloneSetup()` には静的リソースハンドラーがないため、Spring Framework 6.1以降の既定動作で `NoHandlerFoundException` が発生する
- `GlobalExceptionHandler` にはこの例外のハンドラーがなく、`Exception.class` のハンドラーに流れて500になっていた

### 修正
- `handleNoResource()` で `NoResourceFoundException` と `NoHandlerFoundException` の両方を404として扱うようにした
- 引数の型は `Exception` に変更した（呼出し元はSpringのみで、テストからの直接呼出しはない）

---

## 検証

### 実施済み（この環境での確認）
- 変更した全Javaファイルの構文チェック（javac）: エラーなし
- `FileValidator` と `LoginAttemptService` は、外部依存（MultipartFile、Caffeine）をスタブに置き換えて実際のコードを実行し、全項目で期待どおりの結果を確認
  - 正常なPNG/JPEG/WebPを受理、偽装・破損ファイルを拒否、巨大な宣言サイズの画像を拒否
  - 5回失敗でユーザーをロック、成功でユーザーのロックを解除、別ユーザーには影響しない、20ユーザー名へのスプレーでIPをロック

### ローカルでのテスト実行結果（2026-11-03）
- `gradlew.bat clean test` の初回実行で上記の失敗を確認し、修正した

### 未実施（ローカルで実施してください）
この環境ではMaven Centralに接続できず、Gradleビルドができませんでした。以下を実施してください。

```powershell
.\gradlew.bat clean test
.\gradlew.bat bootRun
```

起動後の確認:

```powershell
# 1. XSS対策: Content-Type が text/plain で、本文が固定メッセージであること
curl.exe -i -H "Accept: text/html" "http://localhost:8081/question/list?page=%3Cimg%20src%3Dx%20onerror%3Dalert(1)%3E"

# 2. CSPヘッダーが付与されていること
curl.exe -I http://localhost:8081/question/list

# 3. 削除したAPIが404になること
curl.exe -i "http://localhost:8081/cards/preview?url=https://example.com"
```

画面での確認:
- 質問の作成・修正・削除、画像添付（JPEG/PNG/GIF/WebP）、添付削除
- 質問削除後、添付画像のURLが404になること
- 回答の作成・修正・削除・いいね
- 会員登録（7文字以下のパスワード、`admin` 系ユーザー名が拒否されること）
- ログイン失敗5回でのロック
- 郵便番号検索、ダークモード切替（CSP追加後も動作すること）
- リンクカードを含む質問の表示

## 変更ファイル一覧

| 種別 | ファイル |
|---|---|
| 修正 | `src/main/java/com/yuru/archive/SecurityConfig.java` |
| 修正 | `src/main/java/com/yuru/archive/exception/GlobalExceptionHandler.java` |
| 修正 | `src/main/java/com/yuru/archive/util/FileValidator.java` |
| 修正 | `src/main/java/com/yuru/archive/answer/AnswerController.java` |
| 修正 | `src/main/java/com/yuru/archive/answer/AnswerForm.java` |
| 修正 | `src/main/java/com/yuru/archive/user/UserSecurityService.java` |
| 修正 | `src/main/java/com/yuru/archive/user/UserController.java` |
| 修正 | `src/main/java/com/yuru/archive/user/UserCreateForm.java` |
| 修正 | `src/main/java/com/yuru/archive/user/UserService.java` |
| 修正 | `src/main/java/com/yuru/archive/user/LoginAttemptService.java` |
| 修正 | `src/main/java/com/yuru/archive/linkpreview/service/LinkCardRenderService.java` |
| 修正 | `src/main/java/com/yuru/archive/attach/service/AttachService.java` |
| 修正 | `src/main/java/com/yuru/archive/attach/service/AttachServiceImpl.java` |
| 修正 | `src/main/java/com/yuru/archive/question/QuestionController.java` |
| 修正 | `src/main/java/com/yuru/archive/question/QuestionService.java` |
| 修正 | `src/main/java/com/yuru/archive/question/QuestionForm.java` |
| 修正 | `src/main/resources/templates/question_detail.html` |
| 修正 | `src/test/java/com/yuru/archive/security/StateChangeHttpContractTest.java`（AnswerControllerのコンストラクタ変更に対応） |
| 新規 | `src/main/java/com/yuru/archive/util/LogSanitizer.java` |
| 新規 | `src/test/java/com/yuru/archive/exception/GlobalExceptionHandlerXssTest.java` |
| 新規 | `src/test/java/com/yuru/archive/util/FileValidatorDimensionTest.java` |
| 新規 | `src/test/java/com/yuru/archive/user/LoginAttemptServiceIpLimitTest.java` |
| 削除 | `src/main/java/com/yuru/archive/linkpreview/controller/LinkCardController.java` |
| 修正 | `README.md`（ドキュメント一覧にこのファイルへのリンクを1行追加） |
| 新規 | `doc/11-03checkCode.md`（このファイル） |
