# 変更履歴

新しいものから順に記載しています。詳細な作業記録がある場合は、各項目からリンクしています。

## 2026年

### 2026-11-17 ドキュメントの再構成

- README を入口として整理し、利用者向け（`doc/user/`）と開発者向け（`doc/dev/`）に資料を分割
- `docs/` の開発ログと、`doc/` の日付付き記録を `doc/history/` に移動
- 古い記述（Gradle 8.14.5、CSRF の一部除外など）を現在の構成に合わせて修正
- 履歴資料に残っていた Git のコンフリクトマーカーと、壊れていた画像リンクを修正
- アプリケーションコード・DB スキーマの変更なし

### 2026-11-09 Gradle Wrapper の更新

- Gradle Wrapper 8.14.5 → 9.8.2

### 2026-11-05 CI（GitHub Actions）の試験導入、Dependabot の導入

- `.github/workflows/ci.yml` を追加。`main` への push と Pull Request で `./gradlew check` を自動実行
- Actions タブから、実行するテストの範囲（`check` / `test` / `integrationTest`）を選んで手動実行できるようにした
- `build.gradle` でテストを単体テスト（`test`）と統合テスト（`integrationTest`、`@Tag("integration")`）に分離
- Node.js 20 の非推奨警告に対応し、Actions を Node.js 24 対応版へ更新。setup-gradle のキャッシュに `basic` を指定
- `.github/dependabot.yml` を追加（GitHub Actions を月1回確認）。最初の PR で `actions/setup-java` v5 → v6
- 詳細：[テストと CI](dev/TESTING_AND_CI.md)

### 2026-11-03 コード点検に基づくセキュリティ修正

12件の課題を修正しました。主な内容：

- 例外メッセージ経由の反射型 XSS を修正（固定文言・`text/plain` で応答）
- 画像の Decompression bomb 対策（デコード前に縦横・総ピクセル数を確認）
- 回答作成時のファイル添付を拒否（他人の質問に画像が付く問題）
- ユーザー名 `admin` で管理者権限が付与される処理を撤去し、紛らわしいユーザー名の登録を禁止
- 会員登録のパスワードを8〜100文字に制限
- ログイン試行制限に IP 単位の制限を追加し、記録の保存先を上限付きのキャッシュに変更
- 未使用のリンクカードプレビュー API（`LinkCardController`）を削除し、1投稿のリンクカードを5件までに制限
- 質問の削除時に、添付ファイルの実体も削除
- Content-Security-Policy を追加し、H2 Console の CSRF 除外を撤去
- ログインジェクション対策、負のページ番号の補正、本文長の上限、郵便番号未入力時のエラーを修正
- 回答の入力エラー時に 500 エラーや空の画面になる問題、存在しない URL のテストで 500 になる問題を修正
- 詳細：[2026-11-03 コード点検・セキュリティ修正記録](history/records/2026-11-03_code_review.md)

### 2026-10-30 Java（Temurin）セキュリティパッチ

- 2026年10月20日の Oracle Java SE 定例更新と、10月21日・24日の JSSE 緊急セキュリティアラート（CVE-2026-91403 / CVE-2026-114820）に対応
- JDK Temurin 21.0.11 → 21.0.13（`Temurin-21.0.13+9`、eMudhra 関連のルート CA 証明書の削除を含むビルド）
- リンクカードの Microlink 呼び出しで JSSE（TLS）を実際に使っているため、優先して対応
- Docker のベースイメージを取得し直して再ビルド。アプリケーションコード・DB スキーマの変更なし

### 2026-10-27 Apache Tomcat セキュリティパッチ

- 2026年10月20日に公開された Apache Tomcat の脆弱性に対応
- Spring Boot 4.1.1 → 4.1.2（組み込み Tomcat 11.0.24 → 11.0.27）
- AJP・HTTP/2・WebSocket・JNDIRealm は使っておらず、多くは直接の影響を受けない構成だが、予防的に更新
- 会員情報（プロフィール）機能の不足していたファイルを追加

### 2026-10-07 ローカル保守内容の反映（GitHub 反映：10-08）

- 状態を変更する操作を POST に統一（質問・回答の削除、いいね、ログアウト）し、CSRF 保護を有効化
- 添付ファイルの削除で、ファイル ID・質問 ID・会員 ID を照合し、全件の所有者確認後に削除
- アップロード画像の検証を強化（拡張子・MIME・ファイルシグネチャの整合性、実画像のデコード確認）
- ログイン失敗5回で10分間の制限、パスワード変更時に他のセッションを失効
- 想定できるクライアントエラーを 400 / 403 / 404 / 405 / 409 に整理
- GitHub への反映では、ローカル版で上書きせず差分だけを反映（Git 履歴・プロジェクト名・既存資料・DB 構成を維持）

### 2026-09 構成・セキュリティ改善、MFE の撤去

- Vue / Vite ベースの MFE（メモ機能「ゆる~メスペット」）を本体から撤去し、Spring Boot 単体でビルド・実行できる構成に整理（約1年間の検証を経て、保守・ビルドのコストが大きいと判断）
- 未使用の `AttachController` を撤去し、ファイル名を直接指定する表示・削除の経路を削除
- 添付ファイルの保存・削除パスを正規化し、アップロード領域外の参照を拒否
- 質問本文をそのまま HTML 表示する経路を廃止し、CommonMark + jsoup で安全化した後にリンクカードを合成する方式へ変更
- リンクカードの URL を `http` / `https` の公開ホストに限定
- jsoup 1.17.2 → 1.23.2、Spring AI BOM 1.1.8 → 2.0.1
- ナビゲーション、ヘッダー、カード・フォーム・一覧のデザインを統一し、ダークモードの可読性を改善
- 詳細：[2026-09 セキュリティ改善記録](history/records/2026-09_security_improvements.md)、[検証記録](history/records/2026-09_verification.md)

### 2026（0.1） 1次リファクタリング

- オリジナル版を保存したまま、ローカル開発版を分離
- Controller と Service の責務を整理し、質問の添付処理の二重実行を解消
- リンクカードの描画を Service に分離し、Repository への直接依存を削減
- `QuestionRepository` の ID 型を `Long` に統一、いいねの重複判定を整理
- 詳細：[0.1 リファクタリング記録](history/records/2026-0.1_refactoring.md)

### 2026-08-24 Spring Boot 4.1.1

- Spring Boot 4.1 → 4.1.1
- 特定の拡張子（WebP）の添付画像が表示されない問題を修正

### 2026-07 Spring Boot 3.5.16、Spring Boot 4.1 への移行準備

- Spring Boot 3.5.16 へ更新、非推奨の `@MockBean` を `@MockitoBean` へ置き換え（07-01）
- Spring Boot 4.1 系への移行方針を決定（07-07）。Spring AI は移行作業の対象外

### 2026-06 Spring Boot 3.5.15（CVE-2026-40971）

- Spring Boot の RabbitMQ 自動構成の脆弱性 CVE-2026-40971 に対応し、3.5.9 → 3.5.15 へ更新（RabbitMQ は未使用のため予防的な更新）
- 更新後に発生した Thymeleaf のテンプレート解析エラーに対応し、Markdown 変換をテンプレートから Controller へ移動
- Docker の Java を 17 → 21 に統一（06-26）

### 2026-01 Java 21 への移行

- JDK 17 → 21（01-02）
- Spring Security の非推奨 API（`AntPathRequestMatcher` など）を排除
- リンクカード（Microlink）の失敗時に、画面全体を壊さず通常リンクで表示するよう安定化
- Spring Boot 3.5.6 → 3.5.9、dependency-management プラグイン 1.1.4 → 1.1.7

## 2025年

### 2025-11〜12 リンクカード機能

- `[[linkcard url="..."]]` の形式で、URL のタイトル・説明・画像をカード表示する機能を追加（11-10〜11-28）
- 画像の安定表示やレイアウトの UI / UX 改善（12-01）、改行の保持（12-01）
- SNS（X/Twitter など）側の取得制限により、カード内の画像・説明文・JPG ダウンロードを一時停止（12-04）。タイトルと「原文を開く」は継続
- 詳細：[リンクカードの開発ログ](history/devlog/devlog-SNS.md)

### 2025-10-22 DB の整合性改善

- 外部キー制約とインデックスを再構成し、ER 図を更新
- 詳細：[DevOps アップグレード記録](history/devlog/devops_upgrade.md)

### 2025-09 MFE の統合

- Vue 3 + Vite のメモアプリを、Spring Boot プロジェクトに MFE 方式で統合（09-01、09-17）。2026年9月に撤去
- 詳細：[MFE 統合ステップ1](history/devlog/MFE_Integration_Step1.md)、[ステップ1 追加](history/devlog/MFE_Integration_Step1_Plus.md)

### 2025-07 機能追加と DevOps

- 質問一覧に回答数を表示（07-03）
- タイトル・作成者での検索とページングの連携、検索欄の UI 改善（07-09）
- 画像の添付・削除・ダウンロード（07-10）
- Docker / Docker Compose の構成を追加（07-22）
- CommonMark + jsoup による Markdown の XSS 対策（07-28）

### 2025-06 基本機能の完成

- Spring Boot 3.2.1 → 3.2.12（06-27、CVE-2024-22262 などの DoS 対策を含む）
- 基本機能の開発完了（06-26）
- 添付ファイル機能の改修、質問の二重登録バグの修正

### 2025-05 開発開始

- 開発開始（05-01）。会員登録・ログイン、質問・回答、いいね、ダークモード、郵便番号による住所入力、時間帯に合わせたあいさつ表示を実装
- 開発ログ：[過去の開発記録](history/README.md)
