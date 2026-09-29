# アーキテクチャ

## 基本構成

```text
Browser
  ↓
Spring MVC Controller
  ↓
Service
  ↓
Repository
  ↓
PostgreSQL
```

画面は Thymeleaf + Bootstrap を利用します。

2026年9月の構成見直しにより、Vue / ViteベースのMFEは本体から撤去し、Spring Boot側だけで独立してビルド・実行できる構造へ戻しました。

## 質問機能

`QuestionController` は以下のServiceを利用します。

- `QuestionService`: 質問の検索、登録、修正、削除
- `AnswerService`: 質問一覧の回答数取得
- `AttachService`: 添付ファイル一覧の取得
- `LinkCardRenderService`: 安全化済み本文とリンクカードHTMLの生成
- `UserService`: ログインユーザの取得

## 添付ファイル機能

```text
QuestionController / AnswerController
            ↓
       AttachService
        ↙      ↘
 File System   AttachFileRepository
```

質問登録時のファイル保存は `AttachService` の単一路線に統合しています。

利用者からファイルパスを直接受け取るlegacy `AttachController` は撤去しました。

現在のファイル削除はDB上のファイルIDを基準に行い、物理パスはService内部で再構築・正規化します。

## 本文表示とリンクカード

```text
Question.content
      ↓
LinkCardRenderService
   ↙             ↘
通常本文          linkcard shortcode
 ↓                    ↓
CommonMark         ExternalOgService
 ↓                    ↓
jsoup Cleaner      TemplateEngine
   ↘             ↙
    安全化済みHTML
          ↓
   question_detail
```

ユーザー入力の生HTMLを直接 `th:utext` へ渡さない構造としています。

## MFE撤去後

以下は実行アーキテクチャから削除済みです。

- Vue / Viteメモアプリ
- Node.jsビルド定義
- `/memos/**` 静的配信
- MFE専用Resource設定

過去の設計・検証資料は履歴として別途残る場合があります。

## DBに関する制約

0.1では以下を変更していません。

- Entityのテーブル名
- カラム名
- 外部キー
- Entity間リレーション
- PostgreSQL既存データ
