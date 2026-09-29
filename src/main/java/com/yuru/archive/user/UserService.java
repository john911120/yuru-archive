package com.yuru.archive.user;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yuru.archive.DataNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public SiteUser create(String username, String email, String password, String zipcode, String address1, String address2, String address3, String addressDetail) {
		SiteUser user = new SiteUser();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
	    // ✅ ハイフンを除去して保存
		user.setZipcode(zipcode.replace("-", ""));
		user.setAddress1(address1);
		user.setAddress2(address2);
		user.setAddress3(address3);
		user.setAddressDetail(addressDetail);
		this.userRepository.save(user);
		return user;
	}

	public SiteUser getUser(String username) {
		Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);
		if (siteUser.isPresent()) {
			return siteUser.get();
		} else {
			throw new DataNotFoundException("siteuser not found");
		}
	}

    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        SiteUser user = getUser(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    @Transactional
    public SiteUser updateProfile(String username, UserProfileForm form) {
        SiteUser user = getUser(username);
        user.setEmail(form.getEmail().trim());
        user.setZipcode(normalizeZipcode(form.getZipcode()));
        user.setAddress1(trimToEmpty(form.getAddress1()));
        user.setAddress2(trimToEmpty(form.getAddress2()));
        user.setAddress3(trimToEmpty(form.getAddress3()));
        user.setAddressDetail(trimToEmpty(form.getAddressDetail()));
        return userRepository.save(user);
    }

    public UserProfileForm toProfileForm(SiteUser user) {
        UserProfileForm form = new UserProfileForm();
        form.setEmail(user.getEmail());
        form.setZipcode(user.getZipcode());
        form.setAddress1(user.getAddress1());
        form.setAddress2(user.getAddress2());
        form.setAddress3(user.getAddress3());
        form.setAddressDetail(user.getAddressDetail());
        return form;
    }

    private String normalizeZipcode(String zipcode) {
        if (zipcode == null || zipcode.isBlank()) {
            return null;
        }
        return zipcode.replace("-", "").trim();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
