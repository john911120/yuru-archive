# アーキテクチャ

## 基本構成

```text
Browser
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
PostgreSQL
```

ControllerはHTTP入力、画面遷移、Model構築を担当し、保存・更新・削除などの業務処理はServiceへ委譲します。

## 質問機能

`QuestionController` は以下のServiceを利用します。

- `QuestionService`: 質問の検索、登録、修正、削除
- `AnswerService`: 質問一覧の回答数取得
- `AttachService`: 添付ファイル一覧の取得
- `LinkCardRenderService`: 本文内リンクカードのHTML生成
- `UserService`: ログインユーザの取得

0.1では、Controllerが直接ファイルを保存したりRepositoryを操作したりする箇所を削減しました。

## 添付ファイル機能

```text
QuestionController / AnswerController
            ↓
       AttachService
        ↙      ↘
 File System   AttachFileRepository
```

質問登録時のファイル保存は `AttachService` の単一路線に統合しています。

## リンクカード

```text
QuestionController
       ↓
LinkCardRenderService
       ↓
ExternalOgService
       ↓
Microlink API
```

外部APIの障害が画面全体へ波及しないよう、取得失敗時は通常リンクへフォールバックします。

## DBに関する制約

0.1では以下を変更していません。

- Entityのテーブル名
- カラム名
- 外部キー
- Entity間リレーション
- PostgreSQL既存データ

`QuestionRepository` のジェネリックID型のみ、Entityの `Long` と一致するようJava側で修正しています。DBスキーマ変更ではありません。
