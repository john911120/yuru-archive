package com.yuru.archive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.yuru.archive.user.LoginAttemptService;
import com.yuru.archive.user.LoginRateLimitFilter;
import com.yuru.archive.user.UserSecurityService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserSecurityService userSecurityService;
    private final LoginAttemptService loginAttemptService;
    private final LoginRateLimitFilter loginRateLimitFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        PathPatternRequestMatcher.Builder matcher = PathPatternRequestMatcher.withDefaults();

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        "/question/create",
                        "/question/modify/**",
                        "/question/delete/**",
                        "/answer/create/**",
                        "/answer/modify/**",
                        "/answer/delete/**",
                        "/answer/vote/**",
                        "/user/logout",
                        "/user/password",
                        "/user/password/**",
                        "/user/profile",
                        "/user/profile/**")
                .authenticated()
                .anyRequest().permitAll());

        // 状態変更系の通常エンドポイントではCSRF保護を有効にする。
        // H2 Consoleはローカル検証用のため、従来どおり例外扱いとする。
        http.csrf(csrf -> csrf.ignoringRequestMatchers(
                matcher.matcher("/h2-console/**")));

        http.headers(headers -> headers.addHeaderWriter(
                new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)));

        http.formLogin(form -> form
                .loginPage("/user/login")
                .successHandler((request, response, authentication) -> {
                    loginAttemptService.loginSucceeded(
                            request.getParameter("username"),
                            request.getRemoteAddr());
                    response.sendRedirect(request.getContextPath() + "/");
                })
                .failureHandler((request, response, exception) -> {
                    boolean blocked = loginAttemptService.loginFailed(
                            request.getParameter("username"),
                            request.getRemoteAddr());
                    String suffix = blocked ? "?blocked" : "?error";
                    response.sendRedirect(request.getContextPath() + "/user/login" + suffix);
                }));

        http.logout(logout -> logout
                .logoutRequestMatcher(matcher.matcher(HttpMethod.POST, "/user/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID"));

        // 同一アカウントの複数セッション自体は許可する。
        // パスワード変更時にはUserSessionServiceから他セッションのみ失効させる。
        http.sessionManagement(session -> session
                .maximumSessions(-1)
                .sessionRegistry(sessionRegistry)
                .expiredUrl("/user/login?expired"));

        http.addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("text/plain;charset=UTF-8");
                        response.getWriter().write("ログインが必要です。");
                    } else {
                        response.sendRedirect("/user/login");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("アクセスが拒否されました。");
                }));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userSecurityService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

}
