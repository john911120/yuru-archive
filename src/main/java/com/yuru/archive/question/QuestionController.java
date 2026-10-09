package com.yuru.archive.question;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
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
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.CommonUtil;
import com.yuru.archive.answer.AnswerForm;
import com.yuru.archive.answer.AnswerService;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.linkpreview.service.LinkCardRenderService;
import com.yuru.archive.user.SiteUser;
import com.yuru.archive.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;
    private final AttachService attachService;
    private final LinkCardRenderService linkCardRenderService;
    private final CommonUtil commonUtil;

    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw,
            @RequestParam(value = "type", defaultValue = "subject") String type) {
        log.info("page:{}, kw:{}", page, kw);

        Page<Question> paging = questionService.getList(page, kw, type);
        Map<Long, Integer> answerCountMap = answerService.getAnswerCountMap(paging.getContent());

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("answerCountMap", answerCountMap);
        model.addAttribute("type", type);
        return "question_list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id, AnswerForm answerForm) {
        Question question = questionService.getQuestion(id);
        List<UploadedFile> uploadedFiles = attachService.getFilesByQuestionId(id);
        String htmlBody = linkCardRenderService.render(question.getContent());

        Map<Integer, String> answerHtmlMap = new HashMap<>();
        question.getAnswerList().forEach(answer ->
                answerHtmlMap.put(answer.getId(), commonUtil.markdown(answer.getContent())));

        model.addAttribute("question", question);
        model.addAttribute("uploadedFiles", uploadedFiles);
        model.addAttribute("htmlBody", htmlBody);
        model.addAttribute("answerHtmlMap", answerHtmlMap);
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(
            @Valid QuestionForm questionForm,
            BindingResult bindingResult,
            Principal principal,
            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.createWithAttachments(
                questionForm.getSubject(),
                questionForm.getContent(),
                siteUser,
                uploadFiles);

        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(
            QuestionForm questionForm,
            @PathVariable("id") Long id,
            Principal principal,
            Model model) {
        Question question = questionService.getQuestion(id);
        verifyAuthor(question, principal);

        questionForm.setId(question.getId());
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        model.addAttribute("question", question);
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(
            @Valid QuestionForm questionForm,
            BindingResult bindingResult,
            Principal principal,
            @PathVariable("id") Long id,
            @RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles,
            @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds,
            Model model) {
        Question question = questionService.getQuestion(id);
        verifyAuthor(question, principal);

        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question_form";
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.modifyWithAttachments(
                question,
                questionForm.getSubject(),
                questionForm.getContent(),
                uploadFiles,
                deleteFileIds,
                siteUser);

        return String.format("redirect:/question/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Long id) {
        Question question = questionService.getQuestion(id);
        verifyAuthor(question, principal);
        questionService.deleteQuestionWithFiles(id);
        return "redirect:/";
    }

    private void verifyAuthor(Question question, Principal principal) {
        if (!question.getAuthor().getUsername().equals(principal.getName())) {
            throw new AccessDeniedException("操作権限がありません。");
        }
    }
}
