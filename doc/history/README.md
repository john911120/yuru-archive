# 過去の開発記録

開発の過程で残してきた記録を、当時の内容のまま保存しています。

> ここにある資料は当時の記録です。バージョン・手順・機能が現在と異なる場合があります。現在の仕様は [ドキュメント一覧](../README.md) から `doc/dev/` の資料を参照してください。

2026年11月の資料整理で、旧 `docs/` と `doc/` の日付付き記録をこのフォルダに集めました。整理の際に行った変更は次のとおりです（本文の内容は変えていません）。

- 冒頭に「履歴資料」の注記を追加
- 移動に合わせて画像・SQL へのリンクを修正
- `devops.md` に残っていた Git のコンフリクトマーカーを解消（詳細版の記述を採用）
- 各ファイル末尾のライセンス表記を削除（ルートの README のライセンスが適用されます）

## 作業記録（records）

| 資料 | 時期 | 内容 |
|---|---|---|
| [2026-11-03 コード点検・セキュリティ修正記録](records/2026-11-03_code_review.md) | 2026-11 | 12件の課題（反射型 XSS、Decompression bomb、権限昇格のおそれなど）の内容と修正 |
| [2026-09 セキュリティ改善記録](records/2026-09_security_improvements.md) | 2026-09 | legacy `AttachController` の撤去、Stored XSS 対策、リンクカード URL 検証、依存関係の更新 |
| [2026-09 検証記録](records/2026-09_verification.md) | 2026-09 | 改善後の静的確認、追加テスト、回帰確認項目 |
| [0.1 リファクタリング記録](records/2026-0.1_refactoring.md) | 2026 | Controller / Service の整理、添付処理の二重実行の解消、行数の比較 |
| [開発履歴の概要](records/development_history_summary.md) | 2026-09 | 2025〜2026年の開発の流れの要約 |

## 開発ログ（devlog）

### 全体・振り返り

| 資料 | 時期 | 内容 |
|---|---|---|
| [2025年版 README](devlog/README_2025.md) | 2025 | 初期公開時の README（開発期間、当時の技術スタック、開発ログの一覧） |
| [2025年の締めくくり](devlog/closing_2025.md) | 2025-12 | 2025年の区切りと2026年の目標 |

### 機能の開発ログ

| 資料 | 時期 | 内容 |
|---|---|---|
| [添付ファイル](devlog/devlog-fileupload.md) | 2025-05〜06 | 添付ファイル機能の構成・仕様と改修の記録 |
| [リンクカード](devlog/devlog-SNS.md) | 2025-11〜12 | リンクカード機能の開発、UI 改善、一時停止の経緯 |
| [日本住所 API](devlog/devlog-Japan_Address_API.md) | 2025-05 | ZipCloud による郵便番号からの住所入力 |
| [ダークモード](devlog/devlog-darkmode.md) | 2025-05 | Bootstrap 5.3 のテーマ切り替え |
| [時間帯のあいさつ](devlog/devlog-greeting_Japan.md) | 2025-05 | ログイン時のあいさつ表示 |
| [データベース設計](devlog/devlog-database.md) | 2025-05〜10 | ER 図、テーブル変更の履歴 |
| [Spring Security 認証](devlog/devlog-springSecurity.md) | 2025-05 | 認証設定とログ出力の改善 |

### トラブル対応

| 資料 | 時期 | 内容 |
|---|---|---|
| [「いいね」機能の不具合](devlog/devlog-like-troubleshoot.md) | 2025-05〜06 | いいね数が反映されない問題の調査と対応 |
| [バグと修正履歴](devlog/devlog-troubleshootetc.md) | 2025-06 | 質問の二重登録、マルチパートの調査 |

### 環境・DevOps

| 資料 | 時期 | 内容 |
|---|---|---|
| [JDK 21 への移行](devlog/devlog-updateJDK21log.md) | 2026-01〜07 | JDK 17 → 21、Spring Security の非推奨 API 対応、Spring Boot 4 移行方針 |
| [DevOps 記録](devlog/devops.md) | 2025-06〜2026-07 | 回答数表示、検索改善、添付機能、XSS 対策、Spring Boot 3.5.x の更新 |
| [DevOps アップグレード](devlog/devops_upgrade.md) | 2025-10〜2026-10 | Spring Boot・JDK・DB 構成の更新履歴 |
| [Docker 設定](devlog/devops_Docker.md) | 2025-07〜2026-06 | Docker Compose での実行方法 |
| [Lombok 設定（Ubuntu）](devlog/devlog-lombok.md) | 2025 | Ubuntu 環境での Lombok と STS の設定 |
| [PostgreSQL リモート接続](devlog/devlog-postgres-remote.md) | 2025 | Windows ファイアウォールの設定 |

### MFE（2026年9月に撤去済み）

| 資料 | 時期 | 内容 |
|---|---|---|
| [MFE 統合ステップ1](devlog/MFE_Integration_Step1.md) | 2025-09 | Vue 3 + Vite のメモアプリを MFE 方式で統合 |
| [MFE 統合ステップ1 追加](devlog/MFE_Integration_Step1_Plus.md) | 2025-09 | 開発フローと `/memos/` での動作確認 |

MFE が存在した最後の状態は、Git の履歴に残っています。撤去の経緯は [変更履歴](../CHANGELOG.md#2026-09-構成セキュリティ改善mfe-の撤去) を参照してください。
