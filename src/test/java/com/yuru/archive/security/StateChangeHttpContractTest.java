package com.yuru.archive.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yuru.archive.CommonUtil;
import com.yuru.archive.answer.AnswerController;
import com.yuru.archive.answer.AnswerService;
import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.exception.GlobalExceptionHandler;
import com.yuru.archive.linkpreview.service.LinkCardRenderService;
import com.yuru.archive.question.Question;
import com.yuru.archive.question.QuestionController;
import com.yuru.archive.question.QuestionService;
import com.yuru.archive.user.SiteUser;
import com.yuru.archive.user.UserService;

class StateChangeHttpContractTest {

    private QuestionService questionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        questionService = mock(QuestionService.class);
        UserService userService = mock(UserService.class);
        AnswerService answerService = mock(AnswerService.class);
        AttachService attachService = mock(AttachService.class);
        LinkCardRenderService linkCardRenderService = mock(LinkCardRenderService.class);
        CommonUtil commonUtil = mock(CommonUtil.class);

        QuestionController questionController = new QuestionController(
                questionService,
                userService,
                answerService,
                attachService,
                linkCardRenderService,
                commonUtil);
        AnswerController answerController = new AnswerController(
                questionService,
                answerService,
                userService,
                attachService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(questionController, answerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void questionDeleteRejectsGetWith405() throws Exception {
        mockMvc.perform(get("/question/delete/63"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void answerDeleteRejectsGetWith405() throws Exception {
        mockMvc.perform(get("/answer/delete/63"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void answerVoteRejectsGetWith405() throws Exception {
        mockMvc.perform(get("/answer/vote/63"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void malformedQuestionIdReturns400InsteadOf500() throws Exception {
        mockMvc.perform(get("/question/detail/not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownPathReturns404InsteadOf500() throws Exception {
        mockMvc.perform(get("/definitely-not-existing-path"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingAnotherUsersQuestionReturns403() throws Exception {
        SiteUser owner = new SiteUser();
        owner.setUsername("owner");
        Question question = new Question();
        question.setId(63L);
        question.setAuthor(owner);
        when(questionService.getQuestion(63L)).thenReturn(question);

        Principal attacker = () -> "attacker";
        mockMvc.perform(post("/question/delete/63").principal(attacker))
                .andExpect(status().isForbidden());
    }
}
