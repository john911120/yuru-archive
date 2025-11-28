package com.yuru.archive.util;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.yuru.archive.linkpreview.dto.OgDto;

import org.springframework.context.annotation.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.ConcurrentMap;

@Configuration
public class HttpConfig {
	// Low Level Code Designed
	@Bean
	  WebClient microlinkClient() {
		HttpClient hc = HttpClient.create().responseTimeout(Duration.ofSeconds(5));
		
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(hc))
				.baseUrl("https://api.microlink.io")
				.build();
	}
	/*
	 * Caffeineキャッシュの低レベル実装を直接Bean化しています。
	 * 通常は CacheManager を使いますが、このプロジェクトでは OGP結果のみを単独でキャッシュするため、
	 * より軽量な Cache<String, OgDto> を直接登録しています。
	 *
	 * ※ Spring Cache Abstraction を経由しないため、明示的に get / put / invalidate を呼び出す必要があります。
	 * 　（例: ogCache.getIfPresent(url) / ogCache.put(url, result)）
	 */
	
	@Bean
	com.github.benmanes.caffeine.cache.Cache<String, OgDto> ogCache(){
	    return Caffeine.newBuilder().maximumSize(1000)
	            .expireAfterWrite(Duration.ofMinutes(10)).build();
	}
	
	/*
	import org.springframework.cache.CacheManager;
	import org.springframework.cache.caffeine.CaffeineCacheManager;
	import org.springframework.web.reactive.function.client.WebClient;
	import reactor.netty.http.client.HttpClient;
	import org.springframework.http.client.reactive.ReactorClientHttpConnector;
	 
	 * standard builder version Code
	 * Microlink APIを呼び出すためのWebClient設定です。
	 * Spring公式のWebClient.Builderを使用し、基本URLとタイムアウトのみを指定しています。
	 * （詳細なHttpClient設定は省略し、保守性を優先）
	 *
	 * application.propertiesに下のコードを入れるとタイムアウトを維持できます。
	 * ※ 実際には spring.webclient.* プロパティは標準項目ではないため、
	 * 　 以下の HttpClient 設定でタイムアウトを制御しています。
	 *
	 * spring.webclient.connect-timeout=5000
	 * spring.webclient.read-timeout=5000
	 *
	 * CaffeineCacheManagerを使ってキャッシュを管理します。
	 * ogCache というキャッシュ名で登録され、TTLは10分に設定されています。
	 */
	
	
	/*
	@Bean
	public WebClient microlinkClient(WebClient.Builder builder) {
	    HttpClient hc = HttpClient.create()
	        .responseTimeout(Duration.ofSeconds(5));  // タイムアウト制御
	    return builder
	        .clientConnector(new ReactorClientHttpConnector(hc))
	        .baseUrl("https://api.microlink.io")
	        .build();
	}
	
	@Bean
	public CaffeineCacheManager cacheManager() {
	    CaffeineCacheManager cacheManager = new CaffeineCacheManager("ogCache");
	    cacheManager.setCaffeine(
	        Caffeine.newBuilder()
	            .maximumSize(1000) // 最大1000件をキャッシュ
	            .expireAfterWrite(Duration.ofMinutes(10)) // 10分でキャッシュ失効
	    );
	    return cacheManager;
	}
	 */

}
