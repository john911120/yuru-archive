package com.yuru.archive.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.beans.factory.annotation.Value;

/*
 * WebConfig クラス
 * アプリケーション全体に適用される Web MVC の設定クラスです。
 * 今回は国際化（i18n）のために LocaleResolver を設定しています。
 *
 * - デフォルトの言語を日本語に設定（ブラウザ設定に依存せず固定）
 * - Spring の WebMvcConfigurer を継承して追加設定することも可能
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

	/*
	  クラスは外部モジュールからのアクセスを許可するために public に指定します。
	  ただし、@Bean メソッドは Spring によって内部的に管理されるため、
	  public でなくても動作に支障はありません。
	 */
	/*
	  デフォルトのロケールを日本語に設定する。
	  LocaleChangeInterceptor を使わず、常に日本語で表示させたい場合に使用。
	 */
	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.JAPANESE);
		return slr;
	}
	
    @Value("${com.yuru.archive.upload.path}")
    private String uploadPath;
	
	// WebMvcConfigurerを利用した静的リソース設定
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// access to /upload/** Show the Actual Local Upload Directory Files
		registry.addResourceHandler("/upload/**")
			.addResourceLocations("file:///" + uploadPath + "/")
			.setCachePeriod(3600); // allow the caching
	}
	
	
	// 添付ファイルの機能を追加します。
	@Bean
	public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver();
	}
	
}
