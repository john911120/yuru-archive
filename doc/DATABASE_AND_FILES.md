# DB・添付ファイル

## PostgreSQL

ローカル版は既存のPostgreSQL DBを使用します。

```properties
spring.jpa.hibernate.ddl-auto=none
```

0.1のリファクタリングではDBスキーマおよび既存データを変更していません。

## 主なEntity

- `SiteUser`
- `Question`
- `Answer`
- `UploadedFile`

## 添付ファイル

保存先は以下のプロパティで指定します。

```properties
com.yuru.archive.upload.path=C:/Upload
```

`AttachService` が以下を一元管理します。

- 拡張子・Content-Typeの確認
- 日付別フォルダ生成
- UUID付きファイル名で保存
- サムネイル作成
- `uploaded_file` レコード登録
- 投稿修正時の添付削除

## 0.1で解消した重複

以前は質問登録時に `QuestionController` がファイルを直接保存した後、`AttachService` でも同じMultipartFileを処理する経路が存在しました。

0.1ではController側の直接ファイルI/OとDTO経由の重複保存経路を削除し、`AttachService` のみで保存する構造へ統合しました。

## 注意

質問削除時のDBレコード削除については既存動作を維持しています。物理ファイルを含めた削除方式の変更はDB・運用影響を確認したうえで別フェーズにします。
