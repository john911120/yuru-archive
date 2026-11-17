# 開発環境の構築と起動

ゆるアーカイブをローカルで動かすための手順です。

## 1. 必要なもの

| 項目 | バージョン・備考 |
|---|---|
| JDK | 21（動作確認済み：Temurin 21.0.13+9） |
| PostgreSQL | 16 推奨（Docker Compose では `postgres:16` を使用） |
| Gradle | 不要（同梱の Gradle Wrapper 9.8.2 を使用） |
| IDE（任意） | Spring Tools 5 for Eclipse / Eclipse、または IntelliJ IDEA |
| Docker（任意） | Docker Compose で起動する場合のみ |

Node.js / npm は不要です（2026年9月に Vue / Vite ベースの MFE を撤去したため）。

## 2. 設定

設定は `src/main/resources/application.properties` にまとまっています。主な項目は次のとおりです。

| プロパティ | 既定値 | 内容 |
|---|---|---|
| `server.port` | `8081` | アプリケーションのポート |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/yuru_archive_run` | 接続先 DB |
| `spring.datasource.username` / `password` | ローカル用の値 | DB の接続ユーザー |
| `spring.jpa.hibernate.ddl-auto` | `none` | テーブルを自動で作成・変更しない |
| `com.yuru.archive.upload.path` | `C:/Upload` | 添付ファイルの保存先 |
| `spring.servlet.multipart.max-file-size` / `max-request-size` | `10MB` | アップロードの上限 |
| `app.microlink.base-url` | `https://api.microlink.io` | リンクカード情報の取得先 |
| `app.microlink.timeout-ms` | `5000` | リンクカード取得のタイムアウト（ミリ秒） |
| `app.cache.og-ttl-min` | `10` | リンクカード情報のキャッシュ時間（分） |
| `spring.ai.model.chat` | `none` | AI の自動構成を停止（変更しない） |

接続情報は、ファイルを書き換える代わりに環境変数で上書きすることもできます。

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/yuru_archive_run
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=（自分のパスワード）
export COM_YURU_ARCHIVE_UPLOAD_PATH=/path/to/upload
```

- アップロード先には、アプリケーションが書き込めるフォルダを指定してください。日付ごとのフォルダ（`yyyy/MM/dd`）は保存時に自動で作成されます。
- 本番用の設定は `application-prod.properties`（`spring.profiles.active=prod`）にあります。接続先・パスワードはプレースホルダーのため、実際の値は環境変数などで渡してください。

## 3. データベースの準備

1. PostgreSQL に `yuru_archive_run` データベースを作成します。
2. `ddl-auto=none` のため、テーブルは自動では作られません。既存の DB を使うか、`sql/` のスクリプトを参考にテーブルを作成します。

`sql/` のスクリプトについて：

| ファイル | 内容 |
|---|---|
| `ゆるアーカイブQuery.sql` | テーブル作成と、その後の変更（ALTER）を時系列で記録したもの |
| `ゆるアーカイブ_FK&Index.sql` | 外部キー・インデックスの再構成（2025年10月） |
| `ゆるアーカイブ_DummyData_Query.sql` | 動作確認用のダミーデータ |

`ゆるアーカイブQuery.sql` は変更履歴をそのまま記録したスクリプトで、上から順に実行するだけでは現在のスキーマ（`site_user` テーブルなど）と一致しない箇所があります。現在のテーブル構成は [DB・添付ファイル](DATABASE_AND_FILES.md) を基準にしてください。

## 4. 起動

### Gradle で起動

```bash
# Linux / macOS
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

`http://localhost:8081` を開くと、質問一覧（`/question/list`）へ移動します。

### Docker Compose で起動

1. プロジェクトのルートに `.env` を作成します（Git の管理対象外です）。

   ```dotenv
   DB_NAME=yuru_archive_run
   DB_USER=postgres
   DB_PASSWORD=（自分のパスワード）
   ```

2. 起動します。

   ```bash
   docker compose up --build
   ```

3. `http://localhost:8080` を開きます（ホストの 8080 番をコンテナの 8081 番へ転送しています）。

| サービス | 内容 |
|---|---|
| `yuru-archive` | アプリケーション。`Dockerfile` でビルド（JDK 21 でビルドし、JRE 21 で実行） |
| `db` | PostgreSQL 16。データは `pgdata` ボリュームに保存 |

- 添付ファイルは、ホストの `C:/Upload` をコンテナの `/data/uploads` にマウントして保存します。Windows 以外で使う場合は `docker-compose.yml` の `volumes` を環境に合わせて変更してください。
- JDK のセキュリティ更新を反映するときは、ベースイメージを取得し直して再ビルドします：`docker compose build --pull`

## 5. ビルドとテスト

```bash
./gradlew clean build        # ビルド（テストを含む）
./gradlew test               # 単体テストのみ
./gradlew integrationTest    # 統合テストのみ
./gradlew check              # すべて（CI と同じ）
```

テストの分類と CI については [テストと CI](TESTING_AND_CI.md) を参照してください。

## 6. IDE の設定

### Spring Tools / Eclipse

1. File → Import → Existing Gradle Project でプロジェクトのフォルダを読み込みます。
2. Window → Preferences → Java → Installed JREs に JDK 21 を登録し、Execution Environments の `JavaSE-21` に割り当てます。
3. Lombok を IDE に組み込みます（下記）。

### Lombok

Gradle のビルドでは Lombok 1.18.46 を使います。IDE でも同じバージョン以上の Lombok を有効にしてください。

1. `lombok.jar`（1.18.46 以上）を実行し、IDE のインストール先を指定して「Install / Update」を押します。
2. IDE を再起動し、Help → About に「Lombok v1.18.xx is installed」と表示されることを確認します。

IntelliJ IDEA では Lombok プラグイン（標準で同梱）と、Annotation Processing の有効化を確認してください。

## 7. うまく動かないとき

| 症状 | 対処 |
|---|---|
| IDE で `log cannot be resolved`、`setXxx() is undefined`、`blank final field` などのエラーが出るが、Gradle のビルドは成功する | IDE 側の Lombok 設定を確認します（6章） |
| IDE だけで「フィールドが使われていない」などの警告が出るが、ソースでは使われている | IDE の出力フォルダ（`bin/`）が古いままです。Project → Clean で再ビルドし、Project → Build Automatically が有効か確認します。古い `bin/` のまま IDE から起動すると、修正前のコードで動くことがあります |
| 静的リソースが見つからないエラーがランダムに出る | ① IDE を終了 ② `./gradlew --stop` ③ `.gradle` / `build` / `bin` を削除 ④ Gradle Wrapper で再ビルド ⑤ IDE へ再インポート |
| 起動時に DB 接続エラーになる | 接続先・ユーザー・パスワード、PostgreSQL の起動状態、`yuru_archive_run` の有無を確認します |
| 画像を添付すると保存エラーになる | `com.yuru.archive.upload.path` のフォルダが存在し、書き込めるか確認します |
