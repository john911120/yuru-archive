package com.yuru.archive.answer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yuru.archive.DataNotFoundException;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {

	private final AnswerRepository answerRepository;
	
	/**
	 * AnswerService
	 * 
	 * 【修正理由】
	 * - Java17以降では、final変数はコンストラクタで必ず初期化する必要があります。
	 * - Java19から導入されたrecord型は自動初期化されますが、通常のクラスは対象外です。
	 * - new演算子を使ってServiceを生成すると、SpringのDIコンテナ管理外になり、@Serviceや@Repositoryの機能が正常に動作しません。
	 * - そのため、Springが管理するBeanをコンストラクタインジェクションで受け取る必要があります。
	 */
	public AnswerService(AnswerRepository answerRepository) {
		this.answerRepository = answerRepository;
	}

	public Answer create(Question question, String content, SiteUser author) {
		Answer answer = new Answer();
		answer.setContent(content);
		answer.setCreateDate(LocalDateTime.now());
		answer.setQuestion(question);
		answer.setAuthor(author);
		this.answerRepository.save(answer);
		return answer;
	}

	public Answer getAnswer(Integer id) {
		Optional<Answer> answer = this.answerRepository.findById(id);
		if (answer.isPresent()) {
			return answer.get();
		} else {
			throw new DataNotFoundException("answer not found");
		}
	}

	public void modify(Answer answer, String content) {
		answer.setContent(content);
		answer.setModifyDate(LocalDateTime.now());
		this.answerRepository.save(answer);
	}

	public void delete(Answer answer) {
		this.answerRepository.delete(answer);
	}

	public void vote(Answer answer, SiteUser siteUser) {
		answer.getVoter().add(siteUser);
		this.answerRepository.save(answer);
	}
}
