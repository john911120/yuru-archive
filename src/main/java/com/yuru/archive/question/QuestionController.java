package com.yuru.archive.question;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import com.yuru.archive.answer.AnswerForm;
import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.attach.repository.AttachFileRepository;
import com.yuru.archive.attach.service.AttachService;
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

	private final AttachFileRepository attachFileRepository;
	private final AttachService attachService;
	private final QuestionService questionService;
	private final UserService userService;
		
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {
		log.info("page:{}, kw:{}", page, kw);
		Page<Question> paging = this.questionService.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		return "question_list";
	}

	@GetMapping(value = "/detail/{id}")
	public String detail(Model model, @PathVariable("id") Long id, AnswerForm answerForm) {
		Question question = this.questionService.getQuestion(id);
		List<UploadedFile> uploadedFiles = this.attachFileRepository.findByQuestionId(id);
		
		model.addAttribute("question", question);
		model.addAttribute("uploadedFiles", uploadedFiles);
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
			@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal,
			@RequestParam(value = "uploadFiles", required = false) MultipartFile[] uploadFiles) {
		if (bindingResult.hasErrors()) {
			return "question_form";
		}
		SiteUser siteUser = this.userService.getUser(principal.getName());
	
		// ファイルセーフ処理ロジック
		List<AttachFileDTO> attachFileList = new ArrayList<>();
		for(MultipartFile file : uploadFiles) {
			if(!file.isEmpty()) {
				try {
					String originalFileName = file.getOriginalFilename();
					String uuid = UUID.randomUUID().toString();
					String folderPath = LocalDate.now().toString();
					Path uploadPath = Paths.get("C:/upload",folderPath);
					Files.createDirectories(uploadPath);
					
					Path savePath = uploadPath.resolve(uuid + "_" + originalFileName);
					file.transferTo(savePath.toFile());
					
					AttachFileDTO attach = new AttachFileDTO(originalFileName, uuid, folderPath, siteUser.getId());
					attach.setFileName(originalFileName);
					attach.setUuid(uuid);
					attach.setFolderPath(folderPath);
					attach.setUserId(siteUser.getId());
					
					// 必要の時に質問と連結します。
					// attach.setQuestion(question);
					
					attachFileList.add(attach);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// 質問をセーフした後、リターンされたQuestionをもらう。
		Question savedQuestion = this.questionService.create(
				questionForm.getSubject(),
				questionForm.getContent(),
				siteUser, attachFileList
				);
		
		
		// ファイルが存在すれば、添付ファイルもセーフ
	//	if (uploadFiles != null && uploadFiles.length > 0) {
		if (uploadFiles != null && Arrays.stream(uploadFiles).anyMatch(f -> !f.isEmpty())) {	
			attachService.uploadFiles(uploadFiles, savedQuestion, siteUser);
		}
		
		return "redirect:/question/list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String questionModify(QuestionForm questionForm, @PathVariable("id") Long id, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		if (!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "修正権限がありません。");
		}
		questionForm.setId(question.getId());
		questionForm.setSubject(question.getSubject());
		questionForm.setContent(question.getContent());
		return "question_form";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal,
			@PathVariable("id") Long id) {
		if (bindingResult.hasErrors()) {
			return "question_form";
		}	
		Question question = this.questionService.getQuestion(id);
		if (!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "修正権限がありません。");
		}
		this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
		return String.format("redirect:/question/detail/%s", id);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String questionDelete(Principal principal, @PathVariable("id") Long id) {
		Question question = this.questionService.getQuestion(id);
		if (!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "削除権限がありません。");
		}
		//this.questionService.delete(question);
		
		questionService.deleteQuestionWithFiles(id);
		return "redirect:/";
	}
	// 投票用エンドポイントは不要のため削除済み
	


	
	
}
