# セットアップとビルド

## 前提

- JDK 21
- PostgreSQL
- Gradle Wrapper 8.14.5
- STS / Eclipse または IntelliJ IDEA

## ローカル設定

`src/main/resources/application.properties` の以下をローカル環境に合わせます。

- `server.port`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `com.yuru.archive.upload.path`

0.1のローカル版ではポート `8081` を使用します。

## ビルド

```bash
./gradlew clean build
```

Windows:

```bat
gradlew.bat clean build
```

リソースのみ確認する場合:

```bat
gradlew.bat clean processResources --no-daemon
```

## Lombok

Gradleでは Lombok 1.18.46 を使用します。

Eclipse / STSで `log cannot be resolved`、`setXxx() is undefined`、`blank final field` などが表示され、Gradleビルドが成功する場合はIDE側のLombok連携を確認してください。配布ZIPには `lombok.jar` を同梱せず、Gradle依存関係で管理します。IDEへの導入が必要な場合はLombok公式配布物を使用します。

## キャッシュ障害時

ランダムな静的リソースを「存在しない」と判定する場合は、ソースを修正する前に以下を確認します。

1. STSを終了
2. Gradle Daemonを停止
3. プロジェクト内の `.gradle` / `build` / `bin` を削除
4. Gradle Wrapperで再ビルド
5. IDEへExisting Gradle Projectとして再Import

キャッシュ・生成物はソース配布ZIPへ含めない方針です。

## MFEメモSPAのビルド（Last Flight）

本コミットでは、Vue / Vite製「ゆる~メスペット」をSpring Boot内部へ統合する従来方式を最後に記録します。

Vueプロジェクト側でビルドします。

```bash
npm install
npm run build
```

Viteの `base` は `/memos/` です。生成された `dist` の内容をSpring Boot側の以下へ反映します。

```text
src/main/resources/static/memo/
```

Spring Boot側では `MemoResourceConfig` が `/memos/` と `/memos/assets/**` を提供し、拡張子のないSPAルートを `index.html` へフォールバックします。

この手動同期方式は本コミットを最後に終了予定です。次フェーズではJava/Spring BootとVue/Viteを分離し、それぞれ単独でビルド・テスト・Git管理できる構成へ移行します。
