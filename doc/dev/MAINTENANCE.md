# 保守ガイド

ゆるアーカイブを保守するときの方針、依存関係の更新手順、既知の課題をまとめた資料です。

## 1. 保守の方針

### コード

- DB スキーマを安易に変更しない（変更する場合は別タスクとして計画する）
- 既存の URL をできるだけ変えない
- Controller から業務処理・ファイル I/O を減らし、Service に寄せる
- 利用者の入力を HTML として出力する場合は、必ず安全化の経路を通す
- 使っていない公開エンドポイントや、解決済みのデバッグコードを残さない

### 依存関係

- 脆弱性情報だけで判断せず、実際の利用条件と照らして影響を評価する
- 影響がなくても、保守できる範囲で修正版へ更新する（予防的な更新）
- 更新後は自動テストに加え、[手動での確認項目](TESTING_AND_CI.md#6-手動での確認項目) を確認する

### AI 機能

- 0.1 では AI 機能を実装しない
- Spring AI（BOM 2.0.1、Anthropic スターター）の依存関係は将来の検証用として残し、`spring.ai.model.chat=none` で自動構成を停止する
- 実装する場合は、別ブランチ・別タスクとして明示的に有効化する

## 2. 依存関係の更新

### 現在の基準バージョン

| 対象 | バージョン | 定義場所 |
|---|---|---|
| JDK | 21（Temurin 21.0.13+9） | 開発 PC、`Dockerfile`（`eclipse-temurin:21-jdk` / `21-jre`）、CI |
| Spring Boot | 4.1.2（組み込み Tomcat 11.0.27） | `build.gradle` |
| Gradle Wrapper | 9.8.2 | `gradle/wrapper/gradle-wrapper.properties` |
| Spring AI BOM | 2.0.1 | `build.gradle` |
| jsoup | 1.23.2 | `build.gradle` |
| Lombok | 1.18.46 | `build.gradle`、IDE |
| PostgreSQL | 16 | `docker-compose.yml` |

### 情報の確認先

| 対象 | 確認先 | 時期の目安 |
|---|---|---|
| JDK | Oracle Critical Patch Update、Adoptium（Temurin）のリリース | 1・4・7・10月の第3火曜日（日本時間では翌日）。緊急アラートは随時 |
| Spring Boot / Spring Security | spring.io のリリース・セキュリティ情報 | 毎月のパッチリリース |
| Tomcat | Apache Tomcat 11 の脆弱性情報 | 随時 |
| GitHub Actions | Dependabot の Pull Request | 月1回（自動） |

Dependabot は GitHub Actions だけを対象にしているため、JDK・Gradle の依存関係・Docker のベースイメージは上の確認先で手動確認します。

### Spring Boot を更新する（Tomcat を含む）

組み込み Tomcat のバージョンは Spring Boot が管理しています。Tomcat の脆弱性に対応するときも、Spring Boot のパッチバージョンを上げます。

1. `build.gradle` の `org.springframework.boot` プラグインのバージョンを変更する
2. `./gradlew clean check` を実行する
3. 画面で手動確認を行う
4. README の技術スタックと [変更履歴](../CHANGELOG.md) を更新する

### JDK を更新する

1. 新しい Temurin をインストールする（新しいフォルダに別途インストールされます）
2. IDE の Installed JREs に追加し、`JavaSE-21` の割り当てを新しい JDK に変更する
3. Docker はベースイメージを取得し直して再ビルドする：`docker compose build --pull`
4. 起動ログまたは `java -version` で、新しいビルド番号を確認する
5. 古い JDK は、動作確認が終わってから削除する

`Dockerfile` はバージョンを固定しないタグ（`21-jdk` / `21-jre`）を使っているため、ファイルの変更は不要です。

### Gradle Wrapper を更新する

```bash
./gradlew wrapper --gradle-version <新しいバージョン>
```

`gradle-wrapper.properties` に `distributionSha256Sum` を設定すると、ダウンロードした Gradle 本体の改ざんを検出できます（現在は未設定）。

## 3. 生成物の扱い

次のものは Git の管理対象や配布用のソース ZIP に含めません。

- `.git`、`.gradle`、`build`、`bin`、`target`、`out`
- `.env`、ログファイル、IDE の設定ファイル
- `node_modules`、`package.json`、`package-lock.json`（MFE 撤去後は不要）

## 4. ドキュメントの書き方

| 書く内容 | 書く場所 |
|---|---|
| プロジェクトの概要・入口 | `README.md`（短く保つ） |
| 使い方 | `doc/user/USER_GUIDE.md` |
| 現在の仕様・設計・手順 | `doc/dev/` |
| いつ何を変えたか | `doc/CHANGELOG.md`（新しいものを上に追記） |
| 調査・作業の詳細な記録 | `doc/history/records/`（日付をファイル名に入れる） |

- 現在の仕様は `doc/dev/` を正とし、変更のたびに更新します。履歴の資料は書き換えません。
- セキュリティ対応を記録するときは、「何が問題か」「影響を受ける構成か」「何をしたか」「確認方法」を残します。

## 既知の課題

2026年11月の資料整理時点で確認した課題です。いずれも緊急の対応は不要ですが、機会を見て対応します。

| No | 課題 | 内容 | 対応案 |
|---|---|---|---|
| 1 | `application.properties` が Git の管理対象になっている | `.gitignore` に記載があるが、既に追跡されているため除外されていない。ローカル DB のパスワードが含まれる | 接続情報を環境変数（`SPRING_DATASOURCE_*`）に移し、ファイルには既定値だけを残す |
| 2 | `.gitignore` の `static` / `templates` 行 | 既存のファイルは追跡されているが、今後追加するテンプレートや静的ファイルが自動で除外される | 意図した設定でなければ、該当行を削除する |
| 3 | 初期開発時のクラスが残っている | `/hello`、`/sbb` がログインなしで応答する。`GET /user/` は存在しないテンプレート `main` を返すためエラーになる。`backup/PostController`（中身はコメントのみ）、`HelloLombok` | 不要であれば削除する |
| 4 | 本番プロファイルの設定 | `application-prod.properties` が `ddl-auto=update`、`show_sql=true` になっている | 「スキーマを変更しない」方針に合わせ、`none` と `false` にする |
| 5 | Docker Compose の DB ポート | `5432:5432` は全てのネットワークインターフェースで待ち受ける | ローカル専用なら `127.0.0.1:5432:5432` にする |
| 6 | `sql/` のスクリプト | 変更履歴をそのまま記録しており、新規環境の初期化用としては使いにくい | 現在のスキーマを出力した初期化用スクリプトを別に用意する |
| 7 | 統合テストが無効 | `YuruArchiveApplicationTests` が `@Disabled` | Testcontainers などで DB を用意して有効化する |
| 8 | CSP の `'unsafe-inline'` | テンプレートにインラインのスクリプト・スタイルが残っている | 外部ファイル化して nonce 方式へ移行する |
| 9 | Gradle Wrapper のチェックサム | `distributionSha256Sum` が未設定 | 次に Wrapper を更新するときに設定する |
