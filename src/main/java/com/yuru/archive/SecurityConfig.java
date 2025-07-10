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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
		http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
				// ✅ 認証が必要なDirectory
				.requestMatchers(
					    "/question/create",
					    "/question/modify/**",
					    "/question/delete/**",
					    "/answer/create/**",
					    "/answer/vote/**"
					).authenticated()
					// ✅ 他のDirectoryはすべて許容。
					.anyRequest().permitAll()
				)
				.csrf((csrf) -> csrf.ignoringRequestMatchers(
						new AntPathRequestMatcher("/h2-console/**"),
						new AntPathRequestMatcher("/answer/create/**"),
						new AntPathRequestMatcher("/answer/debu-upload")))
				.headers((headers) -> headers.addHeaderWriter(
						new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
				.formLogin((formLogin) -> formLogin.loginPage("/user/login").defaultSuccessUrl("/"))
				.logout((logout) -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
						.logoutSuccessUrl("/").invalidateHttpSession(true))
				// ログインしないユーザが /answer/vote/**をリクエストした時
				// 403 エラーコード + "ログインが必要です。" メッセージがリターンできるように設定
				.exceptionHandling((exceptionHandling) -> exceptionHandling
			            .authenticationEntryPoint((request, response, authException) -> {
			                // Ajax リクエストした場合は、 401 + メッセージリターン
			                if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			                    response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
			                    response.setContentType("text/plain;charset=UTF-8");
			                    response.getWriter().write("ログインが必要です。");
			                } else {
			                    // 一般のリクエストはログインページにリダイレクトします。
			                    response.sendRedirect("/user/login");
			                }
			            })
			            .accessDeniedHandler((request, response, accessDeniedException) -> {
			                response.setStatus(HttpStatus.FORBIDDEN.value());
			                response.setContentType("text/plain;charset=UTF-8");
			                response.getWriter().write("アクセスが拒否されました。");
			            })
					);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
/*
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
*/
	
	@Bean
	AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		return http.getSharedObject(AuthenticationManagerBuilder.class)
			.userDetailsService(userSecurityService)
			.passwordEncoder(passwordEncoder())
			.and()
			.build();
	}
}
