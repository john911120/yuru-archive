package com.yuru.archive.answer;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.question.Question;
import com.yuru.archive.question.QuestionService;
import com.yuru.archive.user.SiteUser;
import com.yuru.archive.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/*
{@code AnswerController}
Spring Bootにおける依存性注入（DI）の正しい実装方法に基づくコントローラです。

【設計意図】
{@code final}フィールドはコンストラクタインジェクションの対象です。
{@code @RequiredArgsConstructor}により、必要なコンストラクタはLombokが自動生成します。
Java 17以降では、{@code final}フィールドの未初期化は禁止されています。
{@code new}演算子でServiceを手動生成すると、SpringのDI管理対象外となり、
{@code @Service}, {@code @Repository}等の注釈が無効になります。
このため、DIコンテナに登録されたBeanを安全かつ確実に注入するには、
コンストラクタインジェクションを使用するのが最適です。 
*/

@Slf4j
@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller
public class AnswerController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;
	private final AttachService attachService;

    // ↓ 以下のコンストラクタは @RequiredArgsConstructor により自動生成されるため省略
    /*
    public AnswerController(QuestionService questionService, AnswerService answerService, UserService userService) {
        this.questionService = questionService;
        this.answerService = answerService;
        this.userService = userService;
    }
    */
	/*
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, @PathVariable("id") Long id, @Valid AnswerForm answerForm,
			BindingResult bindingResult, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		if (bindingResult.hasErrors()) {
			model.addAttribute("question", question);
			return "question_detail";
		}
		Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
	}
	*/
	
		@PreAuthorize("isAuthenticated()")
		@PostMapping("/create/{id}")
		public String createAnswer(Model model, 
				@PathVariable("id") Long id,
		    @Valid AnswerForm answerForm, 
		    BindingResult bindingResult,
		    @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
		    Principal principal, HttpServletRequest request) {
	
		    Question question = this.questionService.getQuestion(id);
		    SiteUser siteUser = this.userService.getUser(principal.getName());
		    
		    // Multipart Request log
		    if(request instanceof MultipartHttpServletRequest) {
		    	log.info("✅ MultipartHttpServletRequestに 認識している。");
		    } else {
		        log.warn("❌ Multipart リクエストではない (MultipartResolver 未作動可能性あり)");
		    }
		    
		    // Attach File Log
		    if (uploadFiles != null) {
		        log.info("🧪 uploadFiles.length = {}", uploadFiles.length);
		        for (int i = 0; i < uploadFiles.length; i++) {
		            MultipartFile f = uploadFiles[i];
		            log.info("🧪 file[{}] => isEmpty={}, originalName={}, size={} bytes",
		                i, f.isEmpty(), f.getOriginalFilename(), f.getSize());
		        }
		    } else {
		        log.info("⚠️ uploadFiles is null");
		    }	    
		    
		    // 📂  attached file upload check and log check
		    if (uploadFiles != null && uploadFiles.length > 0 && !uploadFiles[0].isEmpty()) {
	
		        // 📎 attach file Service called 
		        attachService.uploadFiles(uploadFiles, question); 
		    }
		    
		    if (bindingResult.hasErrors()) {
		        model.addAttribute("question", question);
		        return "question_detail";
		    }
	
		    Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
	
		    // ⬇ 添付ファイルをセーフする
		    if (uploadFiles != null && uploadFiles.length > 0 && !uploadFiles[0].isEmpty()) {
		        attachService.uploadFiles(uploadFiles, question); 
		    }
	
		    return String.format("redirect:/question/detail/%s#answer_%s",
		        question.getId(), answer.getId());
		}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String answerModify(AnswerForm answerForm, @PathVariable("id") Integer id, Principal principal) {
		Answer answer = this.answerService.getAnswer(id);
		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "修正権限がありません。");
		}
		answerForm.setContent(answer.getContent());
		return "answer_form";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
			@PathVariable("id") Integer id, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "answer_form";
		}
		Answer answer = this.answerService.getAnswer(id);
		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "修正権限がありません。");
		}
		this.answerService.modify(answer, answerForm.getContent());
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String answerDelete(Principal principal, @PathVariable("id") Integer id) {
		Answer answer = this.answerService.getAnswer(id);
		if (!answer.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "削除権限がありません。");
		}
		this.answerService.delete(answer);
		return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
	}
	
	// いいね機能追加
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    @ResponseBody
    public ResponseEntity<String> vote(@PathVariable("id") Integer id, Principal principal, RedirectAttributes redirectAttributes) {
    	//ログインしていないときにはいいねを押せません
        if (principal == null) {
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインが必要です。");
        }
    	SiteUser siteUser = userService.getUser(principal.getName());
        Answer answer = answerService.getAnswer(id);
        /*
          AnswerService.vote()から、Exceptionしたのに、そのExceptionがコントローラーがら見つからなかったので、500 えらーに繋がれたことを修正しました。
        */
        try {
			answerService.vote(answer, siteUser);  // いいねロジック
		} catch (IllegalStateException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
        //return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
        return ResponseEntity.ok("success");
    }
	

}
