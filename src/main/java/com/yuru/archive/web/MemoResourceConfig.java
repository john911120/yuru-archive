package com.yuru.archive.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver; 

@Configuration
public class MemoResourceConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // /memos/ に来たら index.html を返す
        registry.addViewController("/memos/").setViewName("forward:/memos/index.html");
    }
	
	@Override
	  public void addResourceHandlers(ResourceHandlerRegistry registry) {

	    // 1) ハッシュ付きアセットを先に登録する: ロングキャッシュ
	    //    /memos/assets/** → classpath:/static/memo/assets/ に対応
	    registry.addResourceHandler("/memos/assets/**")
	        .addResourceLocations("classpath:/static/memo/assets/")
	        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());

	    // 2) /memos/** : SPA fallback を有効化
	    //    実ファイルがあればそのまま配信し、
	    //    ディレクトリルートや拡張子無しパスは index.html にフォールバックする
	    var memoHandler = registry.addResourceHandler("/memos/**")
	        .addResourceLocations("classpath:/static/memo/");
	    memoHandler.setCacheControl(CacheControl.noCache()); // キャッシュ無効
	    memoHandler.resourceChain(true)
	        .addResolver(new PathResourceResolver() {
	        	@Override
	        	protected Resource getResource(String resourcePath, Resource location) throws IOException {

	        	  // 実ファイルが存在する場合 → そのまま返却
	        	  if (resourcePath != null && !resourcePath.isBlank() && !".".equals(resourcePath)) {
	        	    Resource requested = location.createRelative(resourcePath);
	        	    if (requested.exists() && requested.isReadable()) {
	        	      return requested;
	        	    }
	        	  }

	        	  // "." や "/" や 空文字 の場合は即座に index.html にフォールバック
	        	  if (resourcePath == null
	        	      || resourcePath.isBlank()
	        	      || ".".equals(resourcePath)
	        	      || resourcePath.endsWith("/")) {
	        	    return location.createRelative("index.html");
	        	  }

	        	  // 拡張子無し (SPA ルーターの仮想パス) → index.html
	        	  if (!resourcePath.contains(".")) {
	        	    return location.createRelative("index.html");
	        	  }

	        	  // それ以外 (拡張子付きで存在しない場合) → 404
	        	  return null;
	        	}
	        });
	  }
}