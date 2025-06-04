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
### 250604 添付ファイル機能のロジックを一部変更しました。
question_form.htmlの中で、添付ファイルを追加するロジックを追加しました。

UploadedFile.javaの中で、

	@Column(nullable = false)
	private String githubUrl;

エンティティを削除しました。

AttachServiceImpl.javaファイルの中のロジックを変更しました。

	//添付ファイルをアップロードロジックを処理します。
	@Override
	public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles) {
		List<AttachFileDTO> resultDTOList = new ArrayList<>();
		
		for(MultipartFile uploadFile : uploadFiles) {
			if (!uploadFile.getContentType().startsWith("image")) {
				log.warn("イメージファイルではありません。");
				continue;
			}
			
			String originalName = uploadFile.getOriginalFilename();
			String fileName = originalName.substring(originalName.lastIndexOf("\\")+1);
			String folderPath = makeFolder();
			String uuid = UUID.randomUUID().toString();
			String saveName = uploadPath + File.separator + folderPath + File.separator + uuid + "_" + fileName;
			Path savePath = Paths.get(saveName);
			
			try {
				uploadFile.transferTo(savePath);
				// サムネールを作る。
                String thumbnailSaveName = uploadPath + File.separator + folderPath + File.separator +
                        "s_" + uuid + "_" + fileName;
                Thumbnailator.createThumbnail(savePath.toFile(), new File(thumbnailSaveName), 100, 100);
                
                // 結果DTOを作る。
                AttachFileDTO dto = new AttachFileDTO(fileName, uuid, folderPath);
                resultDTOList.add(new AttachFileDTO(fileName, uuid, folderPath));
                
                //DBにセーフする
                UploadedFile entity = UploadedFile.builder()
                		.userId(1L) //実際に構築する場合は、ローグインしたユーザIDを使用します。
                		.fileName(fileName)
                		.githubUrl(generateGitHubUrl(folderPath, uuid, fileName))　<- このロジックを削除しました。
                		.folderPath(folderPath)
                		.build();
                attachFileRepository.save(entity);
                
			} catch (IOException e) {
				log.error("File Upload Failed" + e);
			} 
		}
		return resultDTOList;
	}
private generatedGitHubUrl()クラスを削除しました。
 - 不要になったコードや個人情報の問題のあるので、削除しました。
 
## 添付ファイル昨日を追加 250604

question_form.htmlに機能を追加
		<!-- 添付ファイル機能を追加 -->
		<div class="mb-3">
		  <label for="uploadFiles" class="form-label">ファイル選択</label>
		  <div class="input-group">
		    <input type="file" class="form-control" id="uploadFiles" name="uploadFiles" multiple style="max-width: 100%;">
		    <button class="btn btn-primary" type="submit">Upload</button>
		  </div>
		</div>
		
question_detail.htmlに添付ファイルが見えるようにしました。
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

		