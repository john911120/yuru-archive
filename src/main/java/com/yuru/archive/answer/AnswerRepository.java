package com.yuru.archive.answer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
	/*
	 * @EntityGraph(attributePaths = {"voter"}) Optional<Answer>
	 * findWithVoterById(Integer id);
	 */
    
    @Query("SELECT a FROM Answer a LEFT JOIN FETCH a.voter WHERE a.id = :id")
    Optional<Answer> findWithVoterById(@Param("id") Integer id);
}
