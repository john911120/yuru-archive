package com.yuru.archive.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserSecurityService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("ゆるアーカイブの接続ログです。");
        logger.info("🔍 ログイン試行中のユーザID: {}", username);

        // ユーザ情報をデータベースから取得（存在しない場合は例外を投げる）
        SiteUser siteUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("❌ 認証失敗: ユーザID = {}", username);
                    return new UsernameNotFoundException("ユーザを探すことができません。");
                });

        // 権限の付与（仮実装: ユーザIDが「admin」の場合のみ管理者権限）
        List<GrantedAuthority> authorities = new ArrayList<>();
        if ("admin".equals(username)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        logger.info("✅ ユーザ [{}] の認証情報を取得しました。付与された権限: {}", username, authorities);

        // Spring Security に認証ユーザとして返す
        return new User(siteUser.getUsername(), siteUser.getPassword(), authorities);
    }
}
