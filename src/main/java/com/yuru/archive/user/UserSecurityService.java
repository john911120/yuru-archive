package com.yuru.archive.user;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yuru.archive.util.LogSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("ログイン試行: user={}", LogSanitizer.clean(username));

        SiteUser siteUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("認証失敗: user={}", LogSanitizer.clean(username));
                    return new UsernameNotFoundException("ユーザを探すことができません。");
                });

        // 2026-11-03: ユーザー名が "admin" かどうかで ROLE_ADMIN を付与する処理を撤去。
        // 会員登録が公開されているため、誰でも "admin" を登録して管理者権限を得られる状態だった。
        // 現在 ROLE_ADMIN を必要とする機能は存在しないため、全員 ROLE_USER とする。
        // 管理機能を追加する際は、SiteUser に role カラムを追加し、DBの値から権限を決定すること。
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(UserRole.USER.getValue()));

        log.info("認証情報を取得しました: user={}, authorities={}",
                LogSanitizer.clean(siteUser.getUsername()), authorities);
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
}
