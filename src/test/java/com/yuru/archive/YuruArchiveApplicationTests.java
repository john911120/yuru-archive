package com.yuru.archive;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.yuru.archive.question.QuestionService;

// Springコンテキストを起動するため統合テストに分類する（./gradlew integrationTest で実行対象になる）
@Tag("integration")
@Disabled("Create Sample Content - Not Using this TestCode")
@SpringBootTest
class YuruArchiveApplicationTests {

    @Autowired
    private QuestionService questionService;

    @Test
    void testJpa() {
        for (int i = 1; i <= 300; i++) {
            String subject = String.format("this is a test message:[%03d]", i);
            String content = "context is nothing";
            this.questionService.create(subject, content, null);
        }
    }
}
