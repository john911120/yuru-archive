package com.yuru.archive.answer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yuru.archive.exception.DuplicateVoteException;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @InjectMocks
    private AnswerService answerService;

    @Test
    void duplicateVoteIsRejectedWithoutSaving() {
        SiteUser author = user(1L, "author");
        SiteUser voter = user(2L, "voter");
        Answer answer = new Answer();
        answer.setAuthor(author);
        answer.setQuestion(new Question());
        answer.getVoter().add(voter);

        assertThrows(DuplicateVoteException.class, () -> answerService.vote(answer, voter));
        verify(answerRepository, never()).save(answer);
    }

    private SiteUser user(Long id, String username) {
        SiteUser user = new SiteUser();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
