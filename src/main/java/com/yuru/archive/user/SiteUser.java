package com.yuru.archive.user;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SiteUser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String username;

	private String password;

	@Column(unique = true)
	private String email;
	
    // 🏠 住所関連
	// 郵便番号
    @Column(length = 10)
    private String zipcode;

    // 都道府県
    @Column(length = 50)
    private String address1;

	// 市区町村
    @Column(length = 50)
    private String address2;

    // 町名
    @Column(length = 50)
    private String address3;
    
    // 詳細住所情報
    @Column(length = 255)
    private String addressDetail;

    @Override
    public boolean equals(Object o) {
    	if (this == o )
    		return true;
    	if(o == null || getClass() != o.getClass())
    		return false;
    	SiteUser siteUser = (SiteUser) o;
    	return Objects.equals(id, siteUser.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }    
    
}
