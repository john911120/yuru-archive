# テストと CI

自動テスト、GitHub Actions による CI、手動での確認項目をまとめた資料です。

## 1. テストの分類

どのテストを実行するかは `build.gradle` で決めています。

| タスク | 対象 | 用途 |
|---|---|---|
| `test` | `@Tag("integration")` が付いて**いない**テスト | 短時間で終わる単体テスト |
| `integrationTest` | `@Tag("integration")` が付いたテスト | Spring コンテキストや DB を使う、時間のかかるテスト |
| `check` | 上の両方 | CI の標準 |

```bash
./gradlew test              # 単体テストのみ
./gradlew integrationTest   # 統合テストのみ
./gradlew check             # すべて（CI と同じ）
```

時間のかかるテストを追加するときは、テストクラスに `@Tag("integration")` を付けます。

```java
import org.junit.jupiter.api.Tag;

@Tag("integration")
@SpringBootTest
class SomeIntegrationTest { ... }
```

## 2. 主なテスト

| テストクラス | 確認している内容 |
|---|---|
| `security/StateChangeHttpContractTest`、`StateChangeMappingTest` | 状態変更が POST のみであること、HTTP ステータスの対応、存在しない URL が 404 になること |
| `exception/GlobalExceptionHandlerTest`、`GlobalExceptionHandlerXssTest` | 例外と HTTP ステータスの対応、エラー応答に入力値を含めないこと |
| `linkpreview/service/LinkCardRenderServiceTest` | `<script>` や `onerror` の除去、`javascript:` のリンクカードを拒否すること |
| `util/FileValidatorTest`、`FileValidatorDimensionTest` | 正常な画像の受け付け、偽装・破損ファイルと巨大な画像の拒否 |
| `user/LoginAttemptServiceTest`、`LoginAttemptServiceIpLimitTest`、`LoginRateLimitFilterTest` | ユーザー単位・IP 単位のログイン試行制限 |
| `user/UserSessionServiceTest` | パスワード変更時に他のセッションだけを失効させること |
| `question/QuestionServiceTest`、`answer/AnswerServiceTest`、`attach/AttachServiceTest` | 質問・回答・添付ファイルのサービス処理 |

`YuruArchiveApplicationTests`（アプリケーション全体の起動テスト）は `@Disabled` のため、現在は実行されません。有効にする場合、CI のサーバーには PostgreSQL がないため、H2 や Testcontainers などテスト用の DB を用意する必要があります。

## 3. CI（GitHub Actions）

2026年11月に試験導入しました。定義ファイルは `.github/workflows/ci.yml` です。

### 実行のタイミング

| きっかけ | 実行するタスク |
|---|---|
| `main` への push | `check` |
| Pull Request の作成・更新 | `check` |
| Actions タブからの手動実行 | 選んだタスク（`check` / `test` / `integrationTest`） |

### 実行環境

- GitHub の Ubuntu サーバー、JDK 21（Temurin）、Gradle Wrapper 9.8.2
- 使用している Actions：`actions/checkout`、`actions/setup-java`、`gradle/actions/setup-gradle`（キャッシュは `basic`）、`actions/upload-artifact`
- 権限はリポジトリの読み取りのみ。同じブランチで新しい実行が始まると、古い実行は取り消されます。

### 結果の確認

- リポジトリの **Actions** タブ、またはコミット横の ✅ / ❌ で確認します。
- テスト結果の HTML レポートは、各実行ページの「Artifacts」から `test-reports` をダウンロードできます（14日間保存）。
- 失敗すると、GitHub に登録したメールアドレスに通知が届きます。

### 手動実行

1. **Actions** タブを開く
2. 左の一覧から **CI** を選ぶ
3. **Run workflow** を押し、ブランチ（`main`）と実行するタスクを選んで実行する

## 4. Dependabot

`.github/dependabot.yml` で、CI が使う GitHub Actions の新しいバージョンを月1回確認します。

- 更新があると、まとめて1件の Pull Request が作られます（コミットメッセージの先頭に `CI` が付きます）。
- PR 上で CI が実行されるので、✅ を確認して Merge し、ローカルで `git pull` します。
- Gradle の依存関係（Spring Boot など）と JDK・Docker のベースイメージは対象外です。これらは手動で確認します（[保守ガイド](MAINTENANCE.md#2-依存関係の更新)）。

## 5. 注意点

- 公開リポジトリでは、CI のログは誰でも閲覧できます。パスワードや API キーをログに出力しないでください。必要な場合は GitHub の Secrets を使います。
- `ci.yml` は原則として変更せず、実行するテストの範囲は `build.gradle` 側で調整します。

## 6. 手動での確認項目

依存関係の更新やセキュリティ修正のあとに、画面で確認する項目です。

### 基本機能

1. アプリケーションが起動する
2. 会員登録（郵便番号からの住所検索を含む）、ログイン、ログアウト
3. 会員情報の参照・編集、パスワード変更
4. 質問一覧の表示、タイトル・作成者での検索、ページ移動
5. 質問の登録（画像付きを含む：JPEG / PNG / GIF / WebP）
6. 質問の修正（画像の追加・削除）、質問の削除
7. 回答の登録・修正・削除、いいね
8. Markdown の表示、リンクカードの表示
9. ライト / ダークモードの切り替え
10. PC・モバイルでの表示
11. 既存の PostgreSQL データが表示される

### セキュリティ

12. `<script>` などを含む本文を投稿しても、スクリプトが実行されない
13. `javascript:` などの不正な URL のリンクカードが拒否される
14. 7文字以下のパスワードや `admin` 系のユーザー ID で会員登録できない
15. ログインに5回失敗するとロックされる
16. パスワードを変更すると、他のブラウザのセッションがログアウトされる
17. 質問を削除すると、添付画像の URL が 404 になる
18. 応答ヘッダーに CSP が付いている

```bash
# 18. CSP ヘッダーの確認
curl -I http://localhost:8081/question/list

# エラー応答に入力値が含まれず、text/plain で返ること
curl -i -H "Accept: text/html" "http://localhost:8081/question/list?page=%3Cimg%20src%3Dx%3E"
```
