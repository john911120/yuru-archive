package com.yuru.archive.answer;

import java.security.Principal;

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

import com.yuru.archive.attach.service.AttachService;
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
            model.addAttribute("question", question);
            return "question_detail";
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        if (hasUploadFiles(uploadFiles)) {
            attachService.validateFiles(uploadFiles);
        }

        Answer answer = answerService.create(question, answerForm.getContent(), siteUser);

        if (hasUploadFiles(uploadFiles)) {
            attachService.uploadFiles(uploadFiles, question, siteUser);
        }

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
