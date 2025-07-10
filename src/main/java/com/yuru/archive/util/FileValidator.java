package com.yuru.archive.util;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class FileValidator {
	/*
		ファイルの許容拡張子有効性を検証するクラス
		許容する拡張子はイメージファイルのみに制限します。
		TXTファイルは、保安のため許容できません。🙇
	*/
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp", "gif");
	
	public static boolean isValidExtension(MultipartFile file) {
		
		String fileName = file.getOriginalFilename();
		
		if(fileName == null || fileName.isBlank())
		return false;
		
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1 ).toLowerCase();
		return ALLOWED_EXTENSIONS.contains(extension);
	}
}
