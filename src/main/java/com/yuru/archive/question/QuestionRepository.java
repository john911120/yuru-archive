package com.yuru.archive.question;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
	Question findBySubject(String subject);

	Question findBySubjectAndContent(String subject, String content);

	List<Question> findBySubjectLike(String subject);

	Page<Question> findAll(Pageable pageable);

	Page<Question> findAll(Specification<Question> spec, Pageable pageable);
	
	// findAllByKeyword()Query改善(or条件を改善しました。)
	// :kw IS NULL OR :kw = '' 条件を追加しました。
	@Query("""
			select distinct q from Question q 
			left join SiteUser u1 on q.author = u1 
			left join Answer a on a.question = q 
			left join SiteUser u2 on a.author = u2 
			where (:kw IS NULL OR :kw = '' 
			or q.subject like %:kw% 
			or q.content like %:kw% 
			or u1.username like %:kw% 
			or a.content like %:kw% 
			or u2.username like %:kw%)""")
    Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
}
