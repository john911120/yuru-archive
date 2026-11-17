# アーキテクチャ

ゆるアーカイブの構成と、主な処理の流れをまとめた資料です。

## 1. 全体構成

Spring Boot / Thymeleaf を中心とした、サーバーサイドレンダリングの単一 Web アプリケーションです。

```text
ブラウザ
  │  HTML（Thymeleaf + Bootstrap）
  ▼
Spring Security（認証・認可、CSRF、CSP、ログイン試行制限）
  ▼
Controller（画面遷移・入力チェック）
  ▼
Service（業務処理・ファイル処理・HTML の安全化）
  ▼
Repository（Spring Data JPA）
  ▼
PostgreSQL

外部サービス
  ├─ Microlink：リンクカード用のページ情報（サーバーから呼び出し）
  └─ ZipCloud：郵便番号検索（ブラウザから呼び出し）
```

2026年9月に Vue / Vite ベースの MFE（メモ機能）を撤去し、Spring Boot 単体でビルド・実行できる構成に戻しました。経緯は [変更履歴](../CHANGELOG.md) を参照してください。

## 2. パッケージ構成

ルートパッケージは `com.yuru.archive` です。

| パッケージ | 主なクラス | 役割 |
|---|---|---|
| （ルート） | `SbbApplication`、`SecurityConfig`、`CommonUtil`、`MainController` | 起動クラス、セキュリティ設定、Markdown 変換、トップページのリダイレクト |
| `question` | `QuestionController`、`QuestionService`、`Question` | 質問の一覧・検索・詳細・登録・修正・削除 |
| `answer` | `AnswerController`、`AnswerService`、`Answer` | 回答の登録・修正・削除、いいね |
| `attach` | `AttachService(Impl)`、`UploadedFile`、`AttachFileRepository` | 添付ファイルの保存・削除・サムネイル生成 |
| `linkpreview` | `LinkCardRenderService`、`ExternalOgService`、`OgDto` | 本文の安全な HTML 化とリンクカード生成 |
| `user` | `UserController`、`UserService`、`UserSecurityService`、`LoginAttemptService`、`LoginRateLimitFilter`、`UserSessionService` | 会員登録・会員情報・パスワード変更、認証、ログイン試行制限、セッション失効 |
| `exception` | `GlobalExceptionHandler` ほか | 例外と HTTP ステータスの対応 |
| `util` | `FileValidator`、`LogSanitizer`、`GreetingUtil`、`HttpConfig` | 画像の検証、ログ出力の無害化、あいさつ文、外部 API 用 WebClient とキャッシュ |
| `config` | `WebConfig` | `/upload/**` の静的配信、ロケール（日本語固定）、Multipart 設定 |
| `common` | `GlobalModelAttributeAdvice` | 全画面共通のモデル属性（ログインユーザー名）。画面のあいさつ文は `static/greeting.js` がブラウザの時刻で表示 |

`backup/PostController`、`HelloController`、`HelloLombok` は初期開発時の名残です。扱いは [保守ガイド](MAINTENANCE.md#既知の課題) を参照してください。

## 3. URL 一覧

| メソッド | URL | ログイン | 内容 |
|---|---|:---:|---|
| GET | `/` | - | `/question/list` へリダイレクト |
| GET | `/question/list` | - | 質問一覧（`page`、`kw`、`type=subject\|author`） |
| GET | `/question/detail/{id}` | - | 質問詳細 |
| GET / POST | `/question/create` | ○ | 質問の登録 |
| GET / POST | `/question/modify/{id}` | ○ | 質問の修正（投稿者のみ） |
| POST | `/question/delete/{id}` | ○ | 質問の削除（投稿者のみ） |
| POST | `/answer/create/{id}` | ○ | 回答の登録 |
| GET / POST | `/answer/modify/{id}` | ○ | 回答の修正（投稿者のみ） |
| POST | `/answer/delete/{id}` | ○ | 回答の削除（投稿者のみ） |
| POST | `/answer/vote/{id}` | ○ | いいね（Ajax） |
| GET / POST | `/user/signup` | - | 会員登録 |
| GET / POST | `/user/login` | - | ログイン（POST は Spring Security が処理） |
| POST | `/user/logout` | ○ | ログアウト |
| GET | `/user/profile` | ○ | 会員情報 |
| GET / POST | `/user/profile/edit` | ○ | 会員情報の編集 |
| GET / POST | `/user/password` | ○ | パスワード変更 |
| GET | `/upload/**` | - | 添付ファイルの配信 |

状態を変更する操作はすべて POST で、CSRF トークンが必要です。

## 4. 主な処理の流れ

### 質問詳細の表示

```text
QuestionController.detail()
  ├─ QuestionService.getQuestion()        質問を取得
  ├─ AttachService.getFilesByQuestionId() 添付ファイル一覧
  ├─ LinkCardRenderService.render()       質問本文を安全な HTML へ
  │     ├─ 通常の本文 → CommonMark → jsoup（Safelist.basicWithImages）
  │     └─ [[linkcard url="..."]] → URL 検証 → ExternalOgService（Microlink、キャッシュ） → card.html
  └─ CommonUtil.markdown()                各回答の本文を安全な HTML へ（answerHtmlMap）
        ▼
  question_detail.html（安全化済みの HTML だけを th:utext で表示）
```

- 利用者が入力した本文を、そのまま `th:utext` に渡す経路はありません。
- リンクカードは1投稿5件まで。`card.html` は現在タイトルと「原文を開く」ボタンだけを表示します（画像・説明文は2025-12-04から一時停止）。Microlink の呼び出しは5秒でタイムアウトし、失敗時は簡易表示にします。結果は10分間キャッシュします。

### 画像の添付

```text
QuestionController（create / modify）
  ▼
QuestionService
  ▼
AttachService
  ├─ FileValidator：拡張子・Content-Type・ファイルシグネチャ・画像の大きさを検証
  ├─ 元のファイル名を basename に正規化
  ├─ {upload.path}/yyyy/MM/dd/{UUID}_{ファイル名} に保存（保存先を正規化し、領域外を拒否）
  ├─ Thumbnailator でサムネイル（s_ で始まるファイル）を作成
  └─ uploaded_file テーブルへ登録
```

- 削除は DB の ID を基準に行い、ファイル名を利用者から直接受け取りません。
- 質問を削除すると、添付ファイルの DB レコードと実ファイル（原本・サムネイル）を削除します。実ファイルはトランザクションのコミット後に削除します。

### ログインとセッション

```text
POST /user/login
  ▼
LoginRateLimitFilter：制限中なら認証前に /user/login?blocked へ
  ▼
Spring Security のフォーム認証（UserSecurityService、BCrypt）
  ├─ 成功：LoginAttemptService.loginSucceeded() → /
  └─ 失敗：LoginAttemptService.loginFailed() → /user/login?error（または ?blocked）
```

- パスワード変更時は `UserSessionService` が、操作中のセッション以外を失効させます（`SessionRegistry` で管理）。
- 詳細は [セキュリティ設計](SECURITY.md) を参照してください。

## 5. 設計上の約束ごと

- DB スキーマ（テーブル・カラム・外部キー）は変更しない。変更が必要な場合は別タスクとして扱う
- Controller からファイル I/O や Repository への直接アクセスを減らし、Service に寄せる
- 利用者の入力を HTML として出力する場合は、必ず安全化の経路を通す
- 使っていない公開エンドポイントを残さない

その他の方針は [保守ガイド](MAINTENANCE.md) を参照してください。
