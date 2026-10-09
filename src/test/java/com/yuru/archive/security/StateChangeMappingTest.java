package com.yuru.archive.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.yuru.archive.answer.AnswerController;
import com.yuru.archive.question.QuestionController;

class StateChangeMappingTest {

    @Test
    void questionDeleteIsPostOnly() throws Exception {
        Method method = QuestionController.class.getMethod(
                "questionDelete", java.security.Principal.class, Long.class);
        assertNotNull(method.getAnnotation(PostMapping.class));
        assertNull(method.getAnnotation(GetMapping.class));
    }

    @Test
    void answerDeleteIsPostOnly() throws Exception {
        Method method = AnswerController.class.getMethod(
                "answerDelete", java.security.Principal.class, Integer.class);
        assertNotNull(method.getAnnotation(PostMapping.class));
        assertNull(method.getAnnotation(GetMapping.class));
    }

    @Test
    void answerVoteIsPostOnly() throws Exception {
        Method method = AnswerController.class.getMethod(
                "vote", Integer.class, java.security.Principal.class);
        assertNotNull(method.getAnnotation(PostMapping.class));
        assertNull(method.getAnnotation(GetMapping.class));
    }
}
