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
    folder_path VARCHAR(100),
    created_at TIMESTAMP DEFAULT now()
);
```

### 🛠️ 2025/06/04 添付ファイル機能の一部ロジックを変更しました

📁 `question_form.html` に添付ファイルアップロードフォームを追加

```html
<!-- 添付ファイル機能を追加 -->
<div class="mb-3">
  <label for="uploadFiles" class="form-label">ファイル選択</label>
  <div class="input-group">
    <input type="file" class="form-control" id="uploadFiles" name="uploadFiles" multiple style="max-width: 100%;">
    <button class="btn btn-primary" type="submit">Upload</button>
  </div>
</div>
```
🧼 UploadedFile.java 内の不要なフィールドを削除
// 削除されたフィールド
@Column(nullable = false)
private String githubUrl;


🔄 AttachServiceImpl.java のロジックを修正
generateGitHubUrl() メソッドを削除（個人情報を含むため）

.githubUrl(...) のビルダー設定行も削除

残った処理は以下のように整理されています：
UploadedFile entity = UploadedFile.builder()
    .userId(1L) // 実際にはログイン中のユーザーIDを使用予定
    .fileName(fileName)
    .folderPath(folderPath)
    .build();
attachFileRepository.save(entity);

📥 question_detail.html に添付ファイル表示機能を追加
<div th:if="${uploadedFiles != null and !uploadedFiles.isEmpty()}">
  <p>添付ファイル一覧:</p>
  <ul>
    <li th:each="file : ${uploadedFiles}">
      <a th:href="@{/attach/download/{id}(id=${file.id})}" th:text="${file.fileName}">添付ファイル</a>
    </li>
  </ul>
</div>
<div th:if="${uploadedFiles == null or uploadedFiles.isEmpty()}">
  <p>添付ファイルはありません。</p>
</div>


🧩 機能追加に伴うその他の変更
AttachFileRepository に findByQuestionId(Long id)を追加

QuestionController に詳細ページ (questionDetail) を追加し、添付ファイル一覧を連携

今後ログインユーザーと連動して userIdを処理する予定

✅ 備考
今後は以下の点を順次対応予定：
ファイルの削除機能
アップロード容量制限
拡張子フィルタリング
編集画面での再添付ロジック


✅ 添付ファイル機能の連携実装(250605)
📌 概要
質問投稿と連携する形で、画像ファイルの添付および保存処理を実装しました。
詳細ページでは関連するファイル一覧を取得し、表示できるようになっています。

🔧 主な修正点
- AttachService：
 uploadFiles(MultipartFile[], Question) メソッド追加（オーバーロード対応）

- AttachServiceImpl：
 添付ファイルと質問の連携保存ロジックを追加

- AttachFileRepository：
 findByQuestionId(Long) メソッド定義でクエリ対応

- QuestionController：
 
  /question/detail/{id} にて添付ファイルの取得ロジックを追加
 
  @PathVariable("id") の型を Long で統一

- Question エンティティ：
 @Builder, @AllArgsConstructor を追加してテストコードに対応

🧪 テスト
AttachServiceTest.java にて uploadFiles メソッドの単体テストを作成・検証済み。

💡 備考
Question の ID 型を Integer → Long に統一。

実際の運用時は userId のハードコーディングを外し、認証ユーザーの ID を連携予定。

### 20250612 添付ファイル機能を修正作業
 - 細かく分析した後、QuestionControllerの方にロジックに関連コートがないことを判断し、
 　ロジックを作成中。