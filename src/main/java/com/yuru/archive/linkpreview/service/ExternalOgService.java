package com.yuru.archive.linkpreview.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.benmanes.caffeine.cache.Cache;
import com.yuru.archive.linkpreview.dto.OgDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExternalOgService {
	
	private final WebClient microlinkClient;
	private final Cache<String, OgDto> ogCache;
	
	@Value("${app.cloudinary.cloud-name}") String cloudName;
	
	// URL スキムとホストライトガード (最初)
	private static void validateUrl(String raw) {
		if (raw == null) throw new IllegalArgumentException("URL required");
		URI url = URI.create(raw.trim());
		String scheme = url.getScheme();
	    if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
	        throw new IllegalArgumentException("Only http/https allowed");
	    }
		//内部網とループバックを遮るロジックを追加
		try {
			InetAddress addr = Inet4Address.getByName(url.getHost());
			if(addr.isLoopbackAddress() || addr.isSiteLocalAddress() || addr.isLinkLocalAddress()) {
	            throw new IllegalArgumentException("Private/loopback host not allowed");
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Host resolution failed", e);
		}
		
	}
	
	public OgDto fetch(String targetUrl) {
		validateUrl(targetUrl);
		OgDto cached = ogCache.getIfPresent(targetUrl);
		if(cached != null) return cached;
		
	    /*
	     * onStatus(...) の第1引数は Predicate<HttpStatusCode>。
	     * HttpStatus::isError（Predicate<HttpStatus）ではタイプが合わないため、
	     * HttpStatusCode::isError を使用します。
	     * （Spring 6 / Boot 3 からの型変更に対応）
	     */
		Map<String, Object> body = microlinkClient.get()
				.uri(url -> url.queryParam("url", targetUrl).build())
				.retrieve()
		        .onStatus(HttpStatusCode::isError, resp -> resp.createException())
		        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
		        .timeout(Duration.ofSeconds(5))
		        .block();
		
		if(body == null || !(body.get("data") instanceof Map<?, ?> data)) {
			// 必要の際にplaceholderで、キャッシュ＆リターンする。
			return fallbackAndCache(targetUrl);
		}
		
	    // Microlink response parching (nessary Field Only)
	  //  Map data = (Map) body.getOrDefault("data", Map.of());
	    String url = safeStr(data.get("url"), 1024);
	    String title = safeStr(data.get("title"), 200);
	    String desc = safeStr(data.get("description"), 500);
	    String image = null;
	    if (data.get("image") instanceof Map img) {
	      image = safeStr(img.get("url"), 1024);
	    }

	    // 必須値をfallbackする
	    if (title == null || title.isBlank()) title = url != null ? url : targetUrl;
	    if (image == null || image.isBlank()) image = "https://via.placeholder.com/1200x630.png?text=No+Image";

	    // Cloudinary fetch → JPG force
	    // String jpg = cloudFetchJpg(image);
	    String jpg = image;
	    
	    OgDto dto = new OgDto(url != null ? url : targetUrl, title, desc, jpg);
	    ogCache.put(targetUrl, dto);
	    return dto;
	  }
	  	private static String safeStr(Object v, int max) {
		    if (v == null) return null;
		    String s = String.valueOf(v).trim();
		    return s.length() > max ? s.substring(0, max) : s;
		  }
	  	
	    private OgDto fallbackAndCache(String targetUrl) {
	        OgDto dto = new OgDto(targetUrl, targetUrl, "", "https://via.placeholder.com/1200x630.png?text=No+Image");
	        ogCache.put(targetUrl, dto);
	        return dto;
	      }
	    /*
		  private String cloudFetchJpg(String originalUrl) {
			if (originalUrl == null || originalUrl.isBlank() || originalUrl.startsWith("data:")) {
			    return "https://via.placeholder.com/1200x630.png?text=No+Image";
			}
		    String enc = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
		    // 大きさの最適化が必要としたら、/c_fill,w_1200,h_630 等々のパラメーター追加も可能。
		    return "https://res.cloudinary.com/" + cloudName + "/image/fetch/f_jpg/" + enc;
		  }
		  */
	}
