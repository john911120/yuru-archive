package com.yuru.archive.answer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.hibernate.Hibernate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

		  return answerRepository.findById(id)
			        .map(answer -> {
			            // voter collection 初期化
			            answer.getVoter().size();
			            return answer;
			        })
			        .orElseThrow(() -> new DataNotFoundException("回答が見つかりません"));
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
	@Transactional
	public ResponseEntity<String> vote(Answer answer, SiteUser user) {
	   //ユーザをチェックしていいねボタンにセキュリティを強化しました。
	    if (user == null) {
	        throw new UnauthorizedVoteException("ログインしたユーザではないな、あなたは、どちら様ですか、");
	    }

	    // 自分自身のコメントには投票できないようにする
	    if (answer.getAuthor().getId().equals(user.getId())) {
	        throw new IllegalStateException("自分のコメントにはいいねを押せないよ。");
	    }

	    // IDでの重複チェック（equals問題防止）
	    boolean alreadyVoted = answer.getVoter().stream()
	        .anyMatch(v -> v.getId().equals(user.getId()));

	    if (alreadyVoted) {
	    	return ResponseEntity.ok("すでにおすすめしました。");
	    }

	    // 投票処理
	    answer.getVoter().add(user);
	    answerRepository.save(answer);
	    answerRepository.flush();
	    
	    return ResponseEntity.ok("success");
	}
	
	public Answer getAnswerWithVoter(Integer id) {
	    return answerRepository.findWithVoterById(id)
	        .orElseThrow(() -> new DataNotFoundException("回答が見つかりません"));
	}
	
	public void save(Answer answer) {
		this.answerRepository.save(answer);
	}
	
}
