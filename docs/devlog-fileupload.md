## 250509
# 📎 添付ファイルアップロード機能（File Upload Module）

## 🔧 パッケージ構成

```
com.yuru.archive.attach
├── controller
│   └── AttachController.java
├── dto
│   └── AttachFileDTO.java
├── entity
│   └── UploadedFile.java
├── repository
│   └── AttachFileRepository.java
├── service
    ├── AttachService.java
    └── AttachServiceImpl.java
```

---

## ✅ 主な機能

<table border="1" cellspacing="0" cellpadding="6" style="border-collapse: collapse; width: 100%; text-align: left;">
  <thead style="background-color: #f2f2f2;">
    <tr>
      <th style="width: 20%;">🛠 機能</th>
      <th>💡 説明</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>画像アップロード</td>
      <td><code>/attach/upload</code> に <code>MultipartFile[]</code> を送信すると、サーバーのローカルディスク（C:/upload）に保存され、サムネイルが自動生成されます</td>
    </tr>
    <tr>
      <td>サムネイルパス生成</td>
      <td>アップロード時に <code>s_</code> 接頭辞付きの100x100サムネイル画像を生成</td>
    </tr>
    <tr>
      <td>アップロード情報のDB保存</td>
      <td>アップロードされたファイルは <code>UploadedFile</code> エンティティとしてDBに保存され、<code>userId</code> および <code>questionId</code> と紐付け可能</td>
    </tr>
    <tr>
      <td>画像表示</td>
      <td><code>/attach/display?fileName=...</code> リクエストで画像を取得して表示可能</td>
    </tr>
    <tr>
      <td>ファイル削除</td>
      <td><code>/attach/remove?fileName=...</code> リクエストで元ファイルとサムネイルの両方を削除</td>
    </tr>
    <tr>
      <td>アップロードパス管理</td>
      <td><code>application.properties</code> で <code>com.yuru.archive.upload.path</code> の値を設定して制御</td>
    </tr>
    <tr>
      <td>GitHub Pages URL対応</td>
      <td>保存されたファイルに対してGitHub Pagesを基準にURLを生成し、<code>github_url</code> フィールドに保存可能</td>
    </tr>
  </tbody>
</table>



---

## 💾 設定例（`application.properties`）

```properties
com.yuru.archive.upload.path=C:/upload
```

---

## 📌 DBテーブル構造の概要

```sql
CREATE TABLE uploaded_file (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    question_id INTEGER, -- （オプション：質問投稿との紐付け）
    file_name VARCHAR(255) NOT NULL,
    github_url TEXT NOT NULL,
    folder_path VARCHAR(100),
    created_at TIMESTAMP DEFAULT now()
);
```