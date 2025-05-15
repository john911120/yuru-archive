package com.yuru.archive.config;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

/*
 * WebConfig クラス
 * アプリケーション全体に適用される Web MVC の設定クラスです。
 * 今回は国際化（i18n）のために LocaleResolver を設定しています。
 *
 * - デフォルトの言語を日本語に設定（ブラウザ設定に依存せず固定）
 * - Spring の WebMvcConfigurer を継承して追加設定することも可能
 */

@Configuration
public class WebConfig {

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
	
}
