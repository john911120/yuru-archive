# yuru-archive
![YuruArchive Logo](assets/yuruArchieve_Logo.png)

## プロジェクト説明
🚀 ゆるアーカイブは、ある週末の夜、  
ふと「質問掲示板ってもっと気軽にできないかな」と思ったことから始まりました。

Spring Boot × PostgreSQLをベースに、  
まだ発展途中ながらも、ログイン・投稿・回答など基本機能を少しずつ組み立てています。

設計から実装まですべて一人でこつこつ進めています。  
「ゆるく、でもちゃんと残る」アーカイブを目指して。

実はこのプロジェクト名には、  
私が日頃楽しんでいるモバイルゲーム「ブルーアーカイブ」からの  
ささやかなインスピレーションも込められています。

**「日常に、小さな奇跡を。」**  
その言葉のように、  
このプロジェクトも、誰かの小さな気づきや発見に繋がればという気持ちで作っています。

🌱 よかったらそっと見守ってください。

## 現在のステータス

- 基本機能開発完了: **2025年6月26日**
- DevOps開始日: **2025年6月27日**
- 現在のフェーズ: **保守・セキュリティ改善 / UI・UX改善**
- アプリケーションバージョン: **0.1**

詳細な開発環境と過去のDevOps記録は [docs/README.md](./docs/README.md) を参照してください。

## 🔧 現在の開発・保守環境

基本機能の開発完了後も、継続的な保守および機能改善を行っています。

- Java **21**（Temurin **21.0.13+9**）
- Spring Boot **4.1.2**
- 組み込みTomcat **11.0.27**
- Gradle Wrapper **8.14.5**
- PostgreSQL
- Docker / Docker Compose
- Spring AI BOM **2.0.1**
- jsoup **1.23.2**
- Thymeleaf / Spring Security
- ローカルポート: **8081**

Spring Bootおよび関連ライブラリの更新、セキュリティ対策、UI・UX改善を継続しています。

Spring AI / Anthropicの依存関係は将来の検証用として残していますが、現時点ではAI機能を実装していません。`spring.ai.model.chat=none` によりChatModelの自動構成を停止しています。

## 主な機能

- ユーザー登録 / ログイン / ログアウト
- 会員情報の参照・編集（メールアドレス / 日本住所）
- パスワード変更
- 質問の登録・修正・削除・検索
- 回答の登録・修正・削除
- 回答へのいいね
- 画像添付 / サムネイル生成
- CommonMark + jsoupによる安全なMarkdown表示
- 外部URLのリンクカード表示
- ダークモード
- PC / タブレット / モバイル向けレスポンシブUI

## MFEアーキテクチャの現在位置

Vue 3 / TypeScript / Viteで構築したメモSPA「ゆる~メスペット」は、Java Webとモダンフロントエンドを接続する実験・学習用途として約1年以上運用しました。

現在はJava本体の独立ビルド・保守・Git管理を容易にするため、MFE統合を解消し、**Spring Boot / Thymeleafを中心とした単一Webアプリケーション**として保守しています。

ローカル開発環境に残っていた旧MFEのビルド成果物、`package.json`、`package-lock.json` は今回のGitHub反映対象には含めていません。

過去のMFE統合手順は以下の履歴資料に残しています。

- [MFE Integration Step1](./docs/MFE_Integration_Step1.md)
- [MFE Integration Step1 Plus](./docs/MFE_Integration_Step1_Plus.md)

## 2026-09 構成・セキュリティ改善

MFE分離後のJava本体を中心に、構成、UI・UX、セキュリティを見直しました。

- 未使用のlegacy添付ファイル経路を整理
- 添付ファイル保存時のパス正規化と保存領域外参照の拒否
- CommonMark + jsoupによる本文サニタイズ
- リンクカードURLを `http` / `https` の公開ホストへ限定
- loopback / private / link-local宛てリンクカードURLを拒否
- 安全ではないURLを通常リンクへフォールバックしない構成へ変更
- Spring AI BOMを **1.1.8 → 2.0.1** へ更新
- jsoupを **1.17.2 → 1.23.2** へ更新
- ナビゲーション、画像表示、モバイル表示等のUI改善

詳細は [2026-09 セキュリティ改善記録](./doc/SECURITY_IMPROVEMENTS_2026-09-10.md) を参照してください。

## 2026-10-07 ローカル保守内容の反映

2026-10-07にローカル環境で実施・確認した保守内容を、GitHub側の既存構成と履歴を維持したまま反映しました。

### 状態変更処理 / CSRF

- 質問削除を `POST /question/delete/{id}` へ統一
- 回答削除を `POST /answer/delete/{id}` へ統一
- いいねを `POST /answer/vote/{id}` へ統一
- ログアウトをPOSTへ変更
- 通常の状態変更処理でCSRF保護を有効化
- H2 Console以外の状態変更URLをCSRF除外対象から削除

### 添付ファイル

- 削除対象を `fileId + questionId + userId` で照合
- 全対象の所有権確認が完了してからファイルを削除
- 他ユーザー、または別質問に属する添付ファイルIDの不正指定を拒否

### アップロード画像

- 許可拡張子の確認
- MIMEタイプの確認
- JPEG / PNG / GIF / WebPのファイルシグネチャ確認
- 拡張子・MIME・画像実体の整合性確認
- JPEG / PNG / GIFの実画像デコード確認
- 破損画像・偽装画像の拒否

### ログイン / セッション

- 同一ユーザー名・接続元のログイン失敗を記録
- **5回失敗で10分間のログイン制限**
- 正常ログイン時に失敗記録を解除
- 同一アカウントの複数セッションは許可
- パスワード変更時は現在のセッションを維持し、その他の既存セッションを失効

ログイン失敗情報はアプリケーションメモリ上で管理するため、アプリケーション再起動時にはリセットされます。

### HTTP例外処理

想定可能なクライアントエラーを一律500にしないよう、主なHTTPステータスを用途別に整理しました。

| 状況 | HTTPステータス |
|---|---:|
| 不正な入力・型不一致 | 400 Bad Request |
| 認証済みだが権限なし | 403 Forbidden |
| 対象データ・ページなし | 404 Not Found |
| POST専用URLへのGET等 | 405 Method Not Allowed |
| 重複いいね | 409 Conflict |
| 想定外の内部例外 | 500 Internal Server Error |

上記の保守対象はローカル環境でブラウザ実機確認を行っています。

## 2026-10-08 GitHub反映方針

今回の更新では、ローカル開発版をGitHub側へ単純上書きせず、両方の内容を比較したうえで必要な差分のみ反映しています。

- GitHub側の既存 `.git` 履歴を維持
- `rootProject.name = 'yuruArchive'` を維持
- Eclipseプロジェクト名 `yuruArchive` を維持
- GitHub側の最新モバイルテーブルUI / CSSを維持
- GitHub側に存在する既存ドキュメント・開発記録を維持
- PostgreSQLスキーマ・既存DBデータは変更しない
- DB接続方式・Docker構成は変更しない
- ローカルDBダンプは反映しない
- 旧MFEのローカルビルド成果物は反映しない

## 2026-10 Apache Tomcat セキュリティパッチ対応

2026年10月20日に公開されたApache Tomcatの脆弱性に対応するため、Spring Bootを **4.1.1 → 4.1.2** へ更新しました。
この更新により、Spring Bootに組み込まれているTomcatも **11.0.24 → 11.0.27** へ更新されています。

| 項目 | 更新前 | 更新後 |
|---|---|---|
| Spring Boot | 4.1.1 | 4.1.2 |
| 組み込みTomcat | 11.0.24 | 11.0.27 |

Tomcat 11.0.25〜11.0.27では複数の脆弱性が修正されています。主なもの（Important）は以下のとおりです。

- **CVE-2026-103119**: AJPレスポンスの混在
- **CVE-2026-101356**: HTTP/2非同期処理エラー後のリクエスト／レスポンスの混在
- **CVE-2026-101296**: JNDIRealmのバインド認証情報検証の不備
- **CVE-2026-76183**: WebSocketエンドポイントに対するセキュリティ制約のバイパス
- **CVE-2026-65182**: セキュリティ制約のバイパス

### 影響評価

本プロジェクトはAJP、HTTP/2、WebSocket、JNDIRealmを使用しておらず、認証・認可はTomcatの宣言的セキュリティ制約ではなくSpring Securityで行っています。そのため、上記の多くは直接的な影響を受ける構成ではありません。

ただし、組み込みサーバーはすべてのリクエストの入口となるため、予防的なセキュリティ対応として修正版を含むバージョンへ更新しました。

- アプリケーションコードの変更なし
- PostgreSQLスキーマ・既存データの変更なし
- ローカル環境で主要機能の動作確認済み

修正内容の詳細は [Apache Tomcat 11 vulnerabilities](https://tomcat.apache.org/security-11.html) を参照してください。

## 2026-10 Java（Temurin）セキュリティパッチ対応

2026年10月20日のOracle Java SE定例更新（Critical Patch Update）と、その後に続いたJSSE関連の緊急セキュリティアラートに対応するため、実行環境のJDKを **Temurin 21.0.11 → 21.0.13** へ更新しました。

| 項目 | 更新前 | 更新後 |
|---|---|---|
| JDK | Temurin 21.0.11 | Temurin 21.0.13 |
| ビルド | - | `OpenJDK Runtime Environment Temurin-21.0.13+9` |

### 今回の更新の経緯

- **10月20日**: Oracle Java SE定例更新。Java 21系は 21.0.13 で修正
- **10月21日**: JSSE（TLS）に関する緊急セキュリティアラート **CVE-2026-91403**（CVSS 9.1）。eMudhra関連の中間CA証明書の信頼除外
- **10月24日**: 追加の緊急セキュリティアラート **CVE-2026-114820**（CVSS 9.1）。eMudhra関連のルートCA証明書の削除

10月21日のアラートでは、10月20日にリリースされた 21.0.13 自体も影響対象とされています。そのため、本プロジェクトでは単に 21.0.13 を適用するのではなく、eMudhra関連ルートCA証明書の削除を含むTemurinのビルド **21.0.13+9** を採用しました。

### 影響評価

本プロジェクトは、リンクカード機能でMicrolink APIへ外部HTTPS通信を行っており、JSSE（TLS）の証明書検証は実際に利用している経路です。Tomcatの脆弱性とは異なり直接関係する構成であるため、修正ビルドへの更新を優先して実施しました。

- `build.gradle` の変更なし（`sourceCompatibility = JavaVersion.VERSION_21` のまま）
- Dockerイメージは `eclipse-temurin:21-jdk` / `21-jre` タグを使用しているため、ベースイメージを更新して再ビルド
- アプリケーションコード・PostgreSQLスキーマの変更なし
- ローカル環境・Docker環境で `Temurin-21.0.13+9` での起動および主要機能の動作確認済み

詳細は [Oracle Security Alert CVE-2026-91403](https://www.oracle.com/security-alerts/alert-cve-2026-91403.html)、[Oracle Security Alert CVE-2026-114820](https://www.oracle.com/security-alerts/alert-cve-2026-114820.html)、[Adoptium: October 2026 Eclipse Temurin releases](https://adoptium.net/news/2026/10/temurin-october-2026-emudhra) を参照してください。

## 起動

PostgreSQLとアップロード先ディレクトリを準備し、プロジェクトルートから実行します。

Linux / macOS:

```bash
./gradlew bootRun
```

Windows:

```bat
gradlew.bat bootRun
```

ローカルURL:

```text
http://localhost:8081
```

DB設定およびファイル保存設定は `src/main/resources/application.properties` を確認してください。

---

## 過去の開発・保守記録

以下は、これまでREADMEに記録してきた開発履歴を失わないために残している履歴情報です。現在の実行構成については上記の「現在のステータス」「現在の開発・保守環境」を参照してください。

### 🛫 MFEアーキテクチャ Last Flight

本コミットは、Vue 3 / TypeScript / Viteで構築したメモSPA「ゆる~メスペット」を、
Spring Bootプロジェクト内部へMFEスタイルで統合して運用する**最終状態（Last Flight）**を記録するものです。

当時の統合経路は以下でした。

```text
ゆる~メスペット (Vue 3 / TypeScript / Vite)
        ↓ npm build
      dist/
        ↓ 手動反映
src/main/resources/static/memo/
        ↓ MemoResourceConfig
      /memos/**
```

- Vite側の `base` は `/memos/`
- Spring側は `MemoResourceConfig` で `/memos/`、`/memos/assets/**`、SPA fallbackを提供
- Thymeleafのナビゲーションから `/memos/` へ遷移
- メモデータはブラウザ `localStorage` を使用し、Spring / PostgreSQLの業務データとは分離

約1年以上、Java Webとモダンフロントエンドを接続する実験・学習用途として運用してきましたが、Java本体の独立ビルド・保守・Git管理を容易にするため、MFE統合を解消し、Vue SPAをJavaプロジェクト外へ分離する方針へ移行しました。

この記録は**Javaプロジェクト内部にMFEアーキテクチャが存在した最後の状態**を示す履歴として扱います。

### 🔐 CVE-2026-40971対応記録

Spring BootのRabbitMQ自動構成に関する脆弱性 **CVE-2026-40971** への対応として、当時Spring Bootを3.5.9から3.5.15へ更新しました。

本脆弱性は、RabbitMQをSSL Bundle経由で接続した際に、TLSホスト名検証が正しく行われない問題です。

本プロジェクトではRabbitMQおよび該当するSSL Bundle構成を使用していないため、直接的な影響を受ける構成ではありませんでした。

しかし、将来的な社内共有や利用範囲の拡大を想定し、予防的なセキュリティ対応として修正版を含むSpring Boot 3.5.15へ更新しました。

その後も依存関係更新を継続し、現在はSpring Boot 4.1.2を使用しています。

### Spring Boot 3.2.12への更新記録

過去にはDoS対策（CVE-2024-22262等）を含む安定版として、Spring Boot 3.2.1から3.2.12へ更新しました。

- 依存関係の安全性向上のため、3.2.1 → 3.2.12に更新
- `build.gradle` のpluginバージョンを修正
- 開発端末2台で3.2.12の動作確認を実施（2025年6月27日）

### README / DevOps整理記録

- メインREADMEをプロジェクト紹介＋DevOpsフェーズ中心の構成へ整理
- 詳細な開発環境ドキュメントを `docs/README.md` へ分離
- CI/CD・運用設計を `docs/devops.md` で管理

## 📁 ドキュメント一覧

### 現行ドキュメント

- [機能一覧](./doc/FEATURES.md)
- [アーキテクチャ](./doc/ARCHITECTURE.md)
- [セットアップとビルド](./doc/SETUP_AND_BUILD.md)
- [DB・添付ファイル](./doc/DATABASE_AND_FILES.md)
- [セキュリティ](./doc/SECURITY.md)
- [2026-09 セキュリティ改善記録](./doc/SECURITY_IMPROVEMENTS_2026-09-10.md)
- [保守方針](./doc/MAINTENANCE.md)
- [0.1 リファクタリング](./doc/REFACTORING_0.1.md)
- [検証記録](./doc/VERIFICATION.md)
- [変更履歴](./doc/CHANGELOG.md)
- [過去の開発履歴概要](./doc/DEVELOPMENT_HISTORY.md)

### 過去のDevOps / 開発記録

- [開発に関する詳細](./docs/README.md)
- [CI/CD・運用設計](./docs/devops.md)
- [MFE Integration Step1](./docs/MFE_Integration_Step1.md)
- [MFE Integration Step1 Plus](./docs/MFE_Integration_Step1_Plus.md)

## License
This project is **NOT open source**.  
© 2025~2026 John Dev – All rights reserved.  
Commercial use without prior written permission is strictly prohibited.
