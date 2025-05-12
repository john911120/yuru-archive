package com.yuru.archive.question;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.yuru.archive.answer.Answer;
import com.yuru.archive.user.SiteUser;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "questions") // テーブルエンティティ修正します。
@Entity
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// titleカーラムとマーピングしました。
	@Column(name = "title")
	private String subject;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(name = "created_at")
	private LocalDateTime createDate;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private SiteUser author;

	@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
	private List<Answer> answerList;

	// DBには存在しませんが、後ほどの機能追加のために残しました。
	// @Column(nullable = true)
	// エンティティの内部では使いますが、マーピングされないようにしょりするあのてーしょんです。
	@Transient
	private LocalDateTime modifyDate;
	
	// 投票機能を削除したため、voterフィールドは除外しました

}
