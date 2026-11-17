> **履歴資料**：2025年6月の記録です。現在の仕様・手順とは異なる場合があります。現行の資料は [ドキュメント一覧](../../README.md) を参照してください。

### 🚨 開発メモ：バグと修正履歴

- 2025年6月10日：質問投稿画面にて、質問が2回登録されるバグを確認。
  原因は QuestionController で `create()` メソッドが2回呼び出されていたため。
  そのうち一つを削除して、バグを修正済み。
 
  
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public String questionCreate(
			@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal,
			@RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles) {
		if (bindingResult.hasErrors()) {
			return "question_form";
		}
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		// 質問をセーフした後、リターンされたQuestionをもらう。
	    Question savedQuestion = this.questionService.create(
	            questionForm.getSubject(),
	            questionForm.getContent(),
	            siteUser
	    );
		
	    // ファイルが存在すれば、添付ファイルもセーフ
	    if (uploadFiles != null && uploadFiles.length > 0) {
	        attachService.uploadFiles(uploadFiles, savedQuestion);
	    }
	    
	//	this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser); <- こっちのコードを削除しました。
		return "redirect:/question/list";
	}
	
## 📌 マルチパートファイルアップロードの問題調査ログ（2025-06-04）

| チェック項目 | 結果 | 備考 |
|--------------|------|------|
| `application.properties` で `spring.servlet.multipart.enabled=true` 設定 | ✅ 済 |
| HTML `<form>` に `enctype="multipart/form-data"` 指定 | ✅ 済 |
| `@RequestParam MultipartFile[]` コントローラ受信定義 | ✅ 済 |
| `StandardServletMultipartResolver` の `@Bean` 登録 | ✅ 済 |
| `HttpServletRequest instanceof MultipartHttpServletRequest` ログ出力 | ❌ 出力されず |
| Spring Security フィルタチェーンでのブロック | ⭕ 問題なし |

**⚠️ MultipartResolver は正しく登録されていても DispatcherServlet に紐づかず、`uploadFiles == null` になる現象が続いている。**

次のフェーズでは、最低限構成での再現と `DispatcherServlet` の初期化戦略の見直しを実施予定。

[仕様上の制限]
現在、MultipartResolverがDispatcherServletに正しく紐づかない原因により、ファイルアップロードが正常に処理されない問題が発生しています。
再現性のある最小構成でのテストおよびServlet初期化順の調査を次のタスクとします。
