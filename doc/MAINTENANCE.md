# 保守方針

## 基本原則

- DBスキーマを安易に変更しない
- 既存URLを極力変更しない
- Controllerから業務処理・ファイルI/Oを減らす
- Repositoryへの直接アクセスはServiceへ寄せる
- 解決済みのデバッグコードを残さない
- 未使用の公開エンドポイントを残さない
- ユーザー入力をHTMLとして出力する場合は必ずサニタイズ経路を通す
- READMEは入口として整理し、詳細は `doc` へ分離する

## フロントエンド

2026年9月にMFEを撤去したため、現在の本体プロジェクトは Node.js / Vue / Vite を必要としません。

以下は本体のビルド要件ではありません。

- `package.json`
- `package-lock.json`
- `node_modules`
- Vue / Viteビルド

## 生成物

以下は配布用ソースZIPやGit管理対象へ含めない方針です。

- `.git`
- `.gradle`
- `build`
- `bin`
- `target`
- `out`

## 依存関係

主要な基準:

- Java 21
- Spring Boot 4.1.1
- Spring AI BOM 2.0.1
- jsoup 1.23.2
- Lombok 1.18.46

脆弱性情報だけで判断せず、実際の利用条件を確認したうえで、保守可能な範囲では修正版へ更新します。

## AI開発

0.1ではAI実装を行いません。

Spring AI依存関係は将来の検証用として残しますが、現時点では `spring.ai.model.chat=none` で自動構成を停止します。
