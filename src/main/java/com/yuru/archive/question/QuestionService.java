package com.yuru.archive.question;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.DataNotFoundException;
import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.user.SiteUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AttachService attachService;

    @Transactional(readOnly = true)
    public Page<Question> getList(int page, String kw, String type) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("createDate")));

        if (kw == null || kw.trim().isEmpty()) {
            return questionRepository.findAll(pageable);
        }

        if ("author".equals(type)) {
            return questionRepository.findByAuthor_UsernameContaining(kw, pageable);
        }
        return questionRepository.findBySubjectContaining(kw, pageable);
    }

    @Transactional(readOnly = true)
    public Question getQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Question not found"));
    }

    public Question create(String subject, String content, SiteUser user) {
        Question question = new Question();
        question.setSubject(subject);
        question.setContent(content);
        question.setCreateDate(LocalDateTime.now());
        question.setAuthor(user);

        Question saved = questionRepository.save(question);
        log.info("質問を保存しました: id={}, subject={}, user={}",
                saved.getId(), subject, user != null ? user.getUsername() : null);
        return saved;
    }

    public Question createWithAttachments(
            String subject,
            String content,
            SiteUser user,
            MultipartFile[] uploadFiles) {
        if (hasUploadFiles(uploadFiles)) {
            attachService.validateFiles(uploadFiles);
        }
        Question saved = create(subject, content, user);
        if (hasUploadFiles(uploadFiles)) {
            attachService.uploadFiles(uploadFiles, saved, user);
        }
        return saved;
    }

    public void modify(Question question, String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        questionRepository.save(question);
    }

    public void modifyWithAttachments(
            Question question,
            String subject,
            String content,
            MultipartFile[] uploadFiles,
            List<Long> deleteFileIds,
            SiteUser user) {
        if (hasUploadFiles(uploadFiles)) {
            attachService.validateFiles(uploadFiles);
        }

        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            attachService.deleteOwnedFiles(deleteFileIds, question.getId(), user.getId());
        }

        modify(question, subject, content);

        if (hasUploadFiles(uploadFiles)) {
            attachService.uploadFiles(uploadFiles, question, user);
        }
    }

    public void delete(Question question) {
        questionRepository.delete(question);
    }

    public void deleteQuestionWithFiles(Long questionId) {
        attachService.deleteFileRecordsByQuestionId(questionId);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new DataNotFoundException("該当の質問が見つかりませんでした"));
        questionRepository.delete(question);
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
