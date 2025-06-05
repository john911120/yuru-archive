package com.yuru.archive.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.yuru.archive.answer.Answer;
import com.yuru.archive.user.SiteUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Table(name = "questions") // テーブルエンティティ修正します。
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
	private List<Answer> answerList = new ArrayList<>();

	// DBには存在しませんが、後ほどの機能追加のために残しました。
	// @Column(nullable = true)
	// エンティティの内部では使いますが、マーピングされないようにしょりするあのてーしょんです。
	@Transient
	private LocalDateTime modifyDate;
	
	// 投票機能を削除したため、voterフィールドは除外しました

}
