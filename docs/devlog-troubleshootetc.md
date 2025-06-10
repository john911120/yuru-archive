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