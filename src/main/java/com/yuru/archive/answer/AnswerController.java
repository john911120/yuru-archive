package com.yuru.archive.answer;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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

import com.yuru.archive.CommonUtil;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.exception.InvalidUploadFileException;
import com.yuru.archive.linkpreview.service.LinkCardRenderService;
import com.yuru.archive.question.Question;
import com.yuru.archive.question.QuestionService;
import com.yuru.archive.user.SiteUser;
import com.yuru.archive.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller
public class AnswerController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;
    private final AttachService attachService;
    private final LinkCardRenderService linkCardRenderService;
    private final CommonUtil commonUtil;
    

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createAnswer(
            Model model,
            @PathVariable("id") Long id,
            @Valid AnswerForm answerForm,
            BindingResult bindingResult,
            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
            Principal principal) {
        Question question = questionService.getQuestion(id);

        if (bindingResult.hasErrors()) {
        	/*
        	  2026-11-03: 入力エラー時も通常の詳細画面と同じモデルを設定する。
              従来は question のみ設定していたため、質問本文・添付画像・回答本文が空で表示されていた。
        	 */
        	List<UploadedFile> uploadedFiles = attachService.getFilesByQuestionId(id);
        	Map<Integer, String> answerHtmlMap = new HashMap<>();
        	question.getAnswerList().forEach(answer -> 
        		answerHtmlMap.put(answer.getId(), commonUtil.markdown(answer.getContent())));
        	model.addAttribute("question", question);
        	model.addAttribute("uploadedFiles", uploadedFiles);
        	model.addAttribute("htmlBody", linkCardRenderService.render(question.getContent()));
        	model.addAttribute("answerHtmlMap", answerHtmlMap);
            return "question_detail";
        }

        // 2026-11-03: 回答への画像添付は受け付けない。
        // 従来は回答者のファイルが「質問」に紐づいて保存され、他人の質問に画像を追加でき、
        // 質問者はそのファイルを削除できなかった。回答用の添付モデルを用意するまでは拒否する。
        // （画面の回答フォームには元々ファイル入力欄がないため、通常操作への影響はない）
        if (hasUploadFiles(uploadFiles)) {
            throw new InvalidUploadFileException("回答への画像添付には対応していません。");
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        Answer answer = answerService.create(question, answerForm.getContent(), siteUser);

        return String.format(
                "redirect:/question/detail/%s#answer_%s",
                question.getId(),
                answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerForm answerForm, @PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(id);
        verifyAuthor(answer, principal);
        answerForm.setContent(answer.getContent());
        return "answer_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(
            @Valid AnswerForm answerForm,
            BindingResult bindingResult,
            @PathVariable("id") Integer id,
            Principal principal) {
        if (bindingResult.hasErrors()) {
            return "answer_form";
        }

        Answer answer = answerService.getAnswer(id);
        verifyAuthor(answer, principal);
        answerService.modify(answer, answerForm.getContent());
        return String.format(
                "redirect:/question/detail/%s#answer_%s",
                answer.getQuestion().getId(),
                answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String answerDelete(Principal principal, @PathVariable("id") Integer id) {
        Answer answer = answerService.getAnswer(id);
        verifyAuthor(answer, principal);
        answerService.delete(answer);
        return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/vote/{id}")
    @ResponseBody
    public ResponseEntity<String> vote(@PathVariable("id") Integer id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("ログインが必要です。");
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        Answer answer = answerService.getAnswer(id);
        try {
            answerService.vote(answer, siteUser);
            return ResponseEntity.ok("success");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void verifyAuthor(Answer answer, Principal principal) {
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("操作権限がありません。");
        }
    }

    private boolean hasUploadFiles(MultipartFile[] uploadFiles) {
        if (uploadFiles == null) {
            return false;
        }
        for (MultipartFile file : uploadFiles) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
