# 過去の開発履歴概要

本ファイルは、旧 `docs` 配下に蓄積されていた開発ログを現行保守向けに要約したものです。

## 2025年

- Spring Boot / PostgreSQLを利用したQ&A機能を開発
- ユーザ認証、質問、回答、いいねを実装
- 画像添付とサムネイル生成を追加
- 日本向け住所入力支援を追加
- ダークモード、レスポンシブUIを改善
- SNSリンクプレビュー / OGP取得を導入
- Docker / DevOps検証を実施
- Vue 3 / TypeScriptによるMFEメモ機能を統合

## 2026年

- Java 21へ更新
- Spring Boot 4.1.1へ更新
- Spring Securityの新APIへ追従
- PostgreSQLローカル環境を継続利用
- Lombok / Gradle / STS環境を更新
- オリジナル版とローカル開発版を分離
- 0.1として1次リファクタリングを開始
- WebP添付表示不具合修正

## 旧開発ログの扱い

旧文書には、解決済みの障害調査、バージョン移行時の試行錯誤、デバッグログ、過去設定が大量に含まれていました。

0.1では現在の仕様確認を優先し、必要な内容を以下へ統合しています。

- `SETUP_AND_BUILD.md`
- `DATABASE_AND_FILES.md`
- `SECURITY.md`
- `MAINTENANCE.md`
- `REFACTORING_0.1.md`

詳細な過去履歴が必要な場合はオリジナル版またはGit履歴を参照します。
