# CI（GitHub Actions）

2026-11 に試験導入。コードを push するたびに、GitHub がビルドとテストを自動で実行する。

## 何が自動で動くか

| きっかけ | 実行内容 |
|---|---|
| `main` への push | `./gradlew check`（単体テスト＋統合テスト） |
| Pull Request の作成・更新 | `./gradlew check` |
| Actions タブから手動実行 | 選んだタスク（`check` / `test` / `integrationTest`） |

- 実行環境：GitHub の Ubuntu サーバー、JDK 21（Temurin）
- 定義ファイル：`.github/workflows/ci.yml`（GitHub の決まりにより、この場所から移動できない）
- 結果：リポジトリの **Actions** タブ、またはコミット横の ✅ / ❌ で確認する
- テスト結果のHTMLレポートは、各実行ページの「Artifacts」から `test-reports` をダウンロードできる（14日間保存）

## テストの分類

どのテストを実行するかは `build.gradle` で決める。`ci.yml` は原則変更しない。

| タスク | 対象 | 用途 |
|---|---|---|
| `test` | `@Tag("integration")` が付いて**いない**テスト | 短時間で終わる単体テスト |
| `integrationTest` | `@Tag("integration")` が付いたテスト | Springコンテキスト・DBを使う時間のかかるテスト |
| `check` | 上の両方 | CI の標準 |

新しく時間のかかるテストを追加するときは、テストクラスに `@Tag("integration")` を付けるだけでよい。

```java
import org.junit.jupiter.api.Tag;

@Tag("integration")
@SpringBootTest
class SomeIntegrationTest { ... }
```

ローカルでも同じタスクを実行できる。

```powershell
.\gradlew.bat test             # 単体テストのみ
.\gradlew.bat integrationTest  # 統合テストのみ
.\gradlew.bat check            # すべて（CI と同じ）
```

## 手動実行の手順

1. GitHub のリポジトリで **Actions** タブを開く
2. 左の一覧から **CI** を選ぶ
3. 右上の **Run workflow** を押し、ブランチ（`main`）と実行するタスクを選んで **Run workflow** を押す

## 注意点

- 現在の統合テスト（`YuruArchiveApplicationTests`）は `@Disabled` のため、`integrationTest` ではスキップされる
- 統合テストを有効にする場合、CI のサーバーには PostgreSQL がないため、H2 や Testcontainers などテスト用の DB を用意する必要がある
- CI のログは、公開リポジトリでは誰でも閲覧できる。パスワード・APIキーをログに出力しないこと（必要な場合は GitHub の Secrets を使う）
- テストが失敗すると、GitHub に登録したメールアドレスに通知が届く
