package com.yuru.archive.answer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return answerRepository.save(answer);
    }

    @Transactional(readOnly = true)
    public Answer getAnswer(Integer id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("answer not found"));
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getAnswerCountMap(List<Question> questions) {
        Map<Long, Integer> answerCountMap = new HashMap<>();
        for (Question question : questions) {
            answerCountMap.put(question.getId(), answerRepository.countByQuestion(question));
        }
        return answerCountMap;
    }

    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        answerRepository.save(answer);
    }

    public void delete(Answer answer) {
        answerRepository.delete(answer);
    }

    public void vote(Answer answer, SiteUser user) {
        if (user == null) {
            throw new UnauthorizedVoteException("ログインが必要です。");
        }
        if (answer.getVoter().contains(user)) {
            throw new DuplicateVoteException("すでにいいねを押しました。");
        }
        if (answer.getAuthor().equals(user)) {
            throw new IllegalStateException("自分のコメントにはいいねを押せません。");
        }

        answer.getVoter().add(user);
        answerRepository.save(answer);
    }
}
