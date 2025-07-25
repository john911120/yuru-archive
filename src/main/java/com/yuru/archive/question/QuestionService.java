package com.yuru.archive.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yuru.archive.DataNotFoundException;
import com.yuru.archive.answer.Answer;
import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.repository.AttachFileRepository;
import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.user.SiteUser;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Service
public class QuestionService {

    private final AttachFileRepository attachFileRepository;

	private final QuestionRepository questionRepository;
	private final AttachService attachService;
	
	@SuppressWarnings("unused")
	private Specification<Question> search(String kw) {
		return new Specification<>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
				query.distinct(true); // 重複を排除します。
				Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
				Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
				Join<Answer, SiteUser> u2 = a.join("author", JoinType.LEFT);
				return cb.or(cb.like(q.get("subject"), "%" + kw + "%"), // コンテンツ
						cb.like(q.get("content"), "%" + kw + "%"), // 内容
						cb.like(u1.get("username"), "%" + kw + "%"), // 質問作成者
						cb.like(a.get("content"), "%" + kw + "%"), // 答えの内容
						cb.like(u2.get("username"), "%" + kw + "%")); // 答え作成者
			}
		};
	}

	public Page<Question> getList(int page, String kw, String type) {
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add(Sort.Order.desc("createDate"));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		if(kw == null || kw.trim().isEmpty()) {
			return this.questionRepository.findAll(pageable);
		}
		// 条件検索の分岐点
		if("author".equals(type)) {
			return questionRepository.findByAuthor_UsernameContaining(kw, pageable);
		} else {
			return questionRepository.findBySubjectContaining(kw, pageable);
		}
		
	//	return this.questionRepository.findAllByKeyword(kw, pageable);
	}

	public Question getQuestion(Long id) {
		// fetch join 方式
		/*
		Optional<Question> question = this.questionRepository.findById(id);
		if (question.isPresent()) {
			return question.get();
		} else {
			throw new DataNotFoundException("question not found");
		}
		*/
		return questionRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("Question not found"));
	}

	public Question create(String subject, String content, SiteUser user) {
		Question q = new Question();
		q.setSubject(subject);
		q.setContent(content);
		q.setCreateDate(LocalDateTime.now());
		q.setAuthor(user);
		
		Question saved = questionRepository.save(q);
		
		log.info("[Questionservice] save : id={}, subject={}, user={}",saved.getId(), subject, user.getUsername());
		return saved;
	}

	public Question create(String subject, String content, SiteUser user, List<AttachFileDTO> attachFileList) {

		Question saved = this.create(subject, content, user);
		
		// attachFileListを処理
		if(attachFileList != null && !attachFileList.isEmpty()) {
			attachService.uploadFilesFromDTOs(attachFileList, saved, user);
		}
		
		return saved;
	}
	
	@Transactional
	public void modify(Question question, String subject, String content) {
		question.setSubject(subject);
		question.setContent(content);
		question.setModifyDate(LocalDateTime.now());
		this.questionRepository.save(question);
	}

	public void delete(Question question) {
		this.questionRepository.delete(question);
	}

	@Transactional
	public void deleteQuestionWithFiles(Long questionId) {
		// 添付ファイルを先に削除します。
		attachFileRepository.deleteByQuestion_Id(questionId);
		
		// 質問を削除します。
		Question question = questionRepository.findById(questionId)
				.orElseThrow(() -> new DataNotFoundException("該当の質問が見つかりませんでした"));
		
		questionRepository.delete(question);
	}

}
