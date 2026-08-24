package com.yuru.archive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.yuru.archive.user.UserSecurityService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserSecurityService userSecurityService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder matcher = PathPatternRequestMatcher.withDefaults();

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        "/question/create",
                        "/question/modify/**",
                        "/question/delete/**",
                        "/answer/create/**",
                        "/answer/vote/**")
                .authenticated()
                .anyRequest().permitAll());

        http.csrf(csrf -> csrf.ignoringRequestMatchers(
                matcher.matcher("/h2-console/**"),
                matcher.matcher("/answer/create/**"),
                matcher.matcher("/answer/debu-upload")));

        http.headers(headers -> headers.addHeaderWriter(
                new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)));

        http.formLogin(form -> form
                .loginPage("/user/login")
                .defaultSuccessUrl("/"));

        http.logout(logout -> logout
                .logoutRequestMatcher(matcher.matcher("/user/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true));

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
}
