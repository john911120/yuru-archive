package com.yuru.archive.answer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yuru.archive.question.Question;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {

	// コメントの数照会メソッド
	int countByQuestion(Question q);

}
