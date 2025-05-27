package com.yuru.archive.answer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yuru.archive.DataNotFoundException;
import com.yuru.archive.exception.DuplicateVoteException;
import com.yuru.archive.exception.UnauthorizedVoteException;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {

	private final AnswerRepository answerRepository;
	
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
	
	// いいねを押したユーザは重複されないようにSetがその役目を遂行します。
	public void vote(Answer answer, SiteUser user) {
    	//ユーザをチェックしていいねボタンにセキュリティを強化しました。
	    if (user == null) {
	    	System.out.println("[⚠️] ログインしたユーザからいいねをリクエストした!");
	        throw new UnauthorizedVoteException("ログインしたユーザではないな、あなたは、どちら様ですか、");
	    }
	    if (answer.getVoter().contains(user)) {
	        throw new DuplicateVoteException("すでにいいねを押しました。");
	    }
		
		if (answer.getAuthor().equals(user)) {
	        throw new IllegalStateException("自分のコメントにはいいねを押せないよ。");
	    }
		if(answer.getVoter().contains(user)) {
			throw new IllegalStateException("いいねを一回押しましたね。重複いいねはできないよ。");
		}
		answer.getVoter().add(user);
		answerRepository.save(answer);
	}

}
