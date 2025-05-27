package com.yuru.archive.answer;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//テーブル名 "answers" に合わせて明示的に指定（デフォルトは "answer" になるため）
@Table(name = "answers") 
@Entity
public class Answer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "created_at")
	private LocalDateTime createDate;

	@ManyToOne
	private Question question;

	@ManyToOne
	@JoinColumn(name ="user_id", nullable = false)
	private SiteUser author;

	@Column(name = "updated_at")
	private LocalDateTime modifyDate;
	
	@ManyToMany
	@JoinTable(
	    name = "answers_voter",
	    joinColumns = @JoinColumn(name = "answer_id"),
	    inverseJoinColumns = @JoinColumn(name = "voter_id")
	)
	private Set<SiteUser> voter = new HashSet<>();
	
}
