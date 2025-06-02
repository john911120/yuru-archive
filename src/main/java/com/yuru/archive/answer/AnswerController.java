package com.yuru.archive.answer;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.yuru.archive.question.Question;
import com.yuru.archive.question.QuestionService;
import com.yuru.archive.user.SiteUser;
import com.yuru.archive.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


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


@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller
public class AnswerController {

	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;
	
	private final AnswerRepository answerRepository;

    // ↓ 以下のコンストラクタは @RequiredArgsConstructor により自動生成されるため省略
    /*
    public AnswerController(QuestionService questionService, AnswerService answerService, UserService userService) {
        this.questionService = questionService;
        this.answerService = answerService;
        this.userService = userService;
    }
    */
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, @PathVariable("id") Integer id, @Valid AnswerForm answerForm,
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
	public ResponseEntity<String> vote(@PathVariable("id") Integer id, Principal principal) {
	    if (principal == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	        		.header("Content-Type", "text/plain; charset=UTF-8")
	        		.body("ログインが必要です。");
	    }

	    SiteUser siteUser = this.userService.getUser(principal.getName());
	    Answer answer = this.answerService.getAnswer(id);

	    // 自分自身のコメントにはいいね不可
	    if (answer.getAuthor().equals(siteUser)) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	        		.header("Content-Type", "text/plain; charset=UTF-8")
	        		.body("自分のコメントにはいいねを押せないよ。");
	    }

	    if (answer.getVoter().contains(siteUser)) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	        	.header("Content-Type", "text/plain; charset=UTF-8")
	            .body("すでにおすすめしました。");
	    }
	    
	    answer.getVoter().add(siteUser);
	    answerRepository.save(answer);
	    
	    // いいね処理（重複チェック含む）は Service に移譲
	    try {
	        answerService.vote(answer, siteUser);  // ロジックの中で、重複検査を遂行
	    } catch (IllegalStateException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    }

	    return ResponseEntity.ok("success");
	}
	
    // REST API追加：いいね数を取得するためのエンドポイント
    @ResponseBody
    @GetMapping("/voter-count/{id}")
    public Map<String, Integer> getVoterCount(@PathVariable("id") Integer id) {
       // Answer answer = answerService.getAnswer(id);
        Answer answer = answerRepository.findById(id).orElseThrow();
    	return Collections.singletonMap("count", answer.getVoter().size());
    }


}
