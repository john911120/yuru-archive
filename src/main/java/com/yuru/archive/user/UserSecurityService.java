package com.yuru.archive.user;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("ログイン試行: user={}", username);

        SiteUser siteUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("認証失敗: user={}", username);
                    return new UsernameNotFoundException("ユーザを探すことができません。");
                });

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(
                "admin".equals(username) ? "ROLE_ADMIN" : "ROLE_USER"));

        log.info("認証情報を取得しました: user={}, authorities={}", username, authorities);
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
}
