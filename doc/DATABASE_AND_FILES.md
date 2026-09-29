# DB・添付ファイル

## PostgreSQL

ローカル版は既存のPostgreSQL DBを使用します。

```properties
spring.jpa.hibernate.ddl-auto=none
```

0.1のリファクタリングおよび2026年9月のセキュリティ改善では、DBスキーマおよび既存データを変更していません。

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
- 元ファイル名のbasename化
- 日付別フォルダ生成
- UUID付きファイル名で保存
- 保存先パスの正規化
- アップロード領域外パスの拒否
- サムネイル作成
- `uploaded_file` レコード登録
- 投稿修正時の添付削除

## legacy endpointの撤去

以前存在した `/attach/display` / `/attach/remove` / `/attach/upload` 用の `AttachController` は、現在の画面処理で利用されていなかったため撤去しました。

ファイル削除は利用者からファイル名を直接受け取らず、DB上のファイルIDを基準に `AttachService.deleteFileById()` で処理します。

## DB保護

今回の改善では以下を変更していません。

- PostgreSQLテーブル
- カラム
- 外部キー
- Entityのマッピング
- 既存レコード
