# 0.1 1次リファクタリング記録

## 目的

オリジナル版を保存したままローカル開発版を分離し、今後の機能追加に耐えられるコード構造へ整理することを目的とします。

## Controller / Service

### QuestionController

以下の処理をControllerから分離しました。

- `Files` / `Path` を利用した直接ファイル保存
- 添付DTOの手動生成
- `AnswerRepository` による直接回答数照会
- `AttachFileRepository` による直接添付照会
- Thymeleafを利用したリンクカード生成ロジック

質問の登録・修正と添付処理は `QuestionService` と `AttachService` を経由します。

### AnswerController

- 過去実装の大規模コメントアウトを削除
- Multipart判定・ファイル詳細出力などの一時デバッグログを削除
- 入力エラー時に添付ファイルだけ保存される流れを修正
- 投稿者確認処理を共通メソッド化

## 添付ファイル

質問登録時に存在していた二重処理を解消しました。

旧構造:

```text
QuestionControllerで物理保存
    ↓
DTOからDB登録
    ↓
AttachServiceでも物理保存・DB登録
```

0.1:

```text
QuestionController
    ↓
QuestionService
    ↓
AttachService
    ├─ 物理保存
    ├─ サムネイル作成
    └─ DB登録
```

`AttachFileDTO` から不要なJPAアノテーションと重複Lombokアノテーションも削除しました。

## Repository

`Question.id` は `Long` ですが、RepositoryのジェネリックID型が `Integer` になっていたため、Java側を `Long` に統一しました。

DBのIDカラム自体は変更していません。

## いいね

`AnswerService.vote()` 内に重複していた「すでにいいね済み」の判定を1箇所へ統合しました。

## 共通処理

Markdown Parser / Rendererをリクエストごとに再生成せず、`CommonUtil` 内で再利用する構造へ変更しました。

## 依存関係

- CommonMarkの重複定義を削除
- Spring MVCは `spring-boot-starter-webmvc` に統一
- リンクカード用HTTPクライアントは `spring-boot-starter-webflux` から `spring-boot-starter-webclient` へ整理
- JUnitの重複明示依存を整理
- Lombokを1.18.46へ統一
- Spring Boot 4.1.0 / Java 21を現行基準として文書化

## DB保護

以下は変更していません。

- EntityのDBマッピング
- PostgreSQLテーブル
- カラム
- FK
- 既存データ
- `ddl-auto=none` のローカル設定

## 整理結果（行数）

原本と0.1を比較した結果です。

- メインJavaソース: **2,470行 → 2,114行（356行削減）**
- Markdown: **2,071行 → 659行（1,412行削減）**
- テストコード: **90行 → 230行**（回帰確認用テストを追加したため増加）

行数削減そのものを目的とせず、既存ロジックを維持しながらControllerの責務、重複処理、過去メモを整理した結果です。
