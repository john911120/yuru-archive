# 変更履歴

## 0.1

### リファクタリング

- Controller / Service責務整理
- 質問添付ファイルの重複処理を統合
- リンクカードレンダリングをServiceへ分離
- Repository直接依存を削減
- `QuestionRepository` ID型を `Long` に統一
- AnswerControllerのデバッグコード・旧実装コメントを整理
- いいね処理の重複判定を整理
- DTOの不要なJPAアノテーションを削除
- Markdown共通処理を軽量化

### ビルド・依存関係

- プロジェクトバージョンを0.1へ更新
- Lombok 1.18.46
- 重複依存関係を整理

### ドキュメント

- ルートREADMEを現行仕様中心に再作成
- 過去の大量devlogを要約し、`doc` 配下へ用途別に再構成

### 非対象

- AI機能追加
- DBスキーマ変更
- PostgreSQL既存データ変更
