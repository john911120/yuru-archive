package com.yuru.archive.linkpreview.service;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.github.benmanes.caffeine.cache.Cache;
import com.yuru.archive.linkpreview.dto.OgDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExternalOgService {

    private final WebClient microlinkClient;
    private final Cache<String, OgDto> ogCache;

    public OgDto fetch(String targetUrl) {
        // 不正なスキームやローカルネットワーク宛てURLは、
        // フォールバック表示へ流さずここで拒否します。
        validateUrl(targetUrl);

        OgDto cached = ogCache.getIfPresent(targetUrl);
        if (cached != null) {
            return cached;
        }

        try {
            Map<String, Object> body = microlinkClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/")
                            .queryParam("url", targetUrl)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> response.createException())
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (body == null || !(body.get("data") instanceof Map<?, ?> data)) {
                return fallbackAndCache(targetUrl);
            }

            String url = safeString(data.get("url"), 1024);
            String title = safeString(data.get("title"), 200);
            String description = safeString(data.get("description"), 500);
            String image = null;

            if (data.get("image") instanceof Map<?, ?> imageData) {
                image = safeString(imageData.get("url"), 1024);
            }

            if (title == null || title.isBlank()) {
                title = url != null ? url : targetUrl;
            }
            if (image == null || image.isBlank()) {
                image = "https://via.placeholder.com/1200x630.png?text=No+Image";
            }

            // 画面上のリンク先には、事前検証済みの要求URLのみを使用します。
            OgDto dto = new OgDto(targetUrl, title, description, image);
            ogCache.put(targetUrl, dto);
            return dto;
        } catch (WebClientResponseException e) {
            return fallbackAndCache(targetUrl);
        } catch (Exception e) {
            return fallbackAndCache(targetUrl);
        }
    }

    private static void validateUrl(String rawUrl) {
        if (rawUrl == null) {
            throw new IllegalArgumentException("URL required");
        }

        URI url = URI.create(rawUrl.trim());
        String scheme = url.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only http/https allowed");
        }
        if (url.getHost() == null || url.getHost().isBlank() || url.getUserInfo() != null) {
            throw new IllegalArgumentException("Valid public host required");
        }

        try {
            for (InetAddress address : InetAddress.getAllByName(url.getHost())) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isSiteLocalAddress()
                        || address.isLinkLocalAddress()) {
                    throw new IllegalArgumentException("Private/loopback host not allowed");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Host resolution failed", e);
        }
    }

    private static String safeString(Object value, int maxLength) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private OgDto fallbackAndCache(String url) {
        OgDto dto = new OgDto(
                url,
                url,
                "",
                "https://via.placeholder.com/1200x630.png?text=No+Image");
        ogCache.put(url, dto);
        return dto;
    }
}
