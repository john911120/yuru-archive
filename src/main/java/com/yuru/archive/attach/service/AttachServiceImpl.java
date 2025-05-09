package com.yuru.archive.attach.service;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.attach.repository.AttachFileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachServiceImpl implements AttachService {
	
    @Value("${com.yuru.archive.upload.path}")
	private String uploadPath;
    
    private final AttachFileRepository attachFileRepository;

	//添付ファイルをアップロードロジックを処理します。
	@Override
	public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles) {
		List<AttachFileDTO> resultDTOList = new ArrayList<>();
		
		for(MultipartFile uploadFile : uploadFiles) {
			if (!uploadFile.getContentType().startsWith("image")) {
				log.warn("イメージファイルではありません。");
				continue;
			}
			
			String originalName = uploadFile.getOriginalFilename();
			String fileName = originalName.substring(originalName.lastIndexOf("\\")+1);
			String folderPath = makeFolder();
			String uuid = UUID.randomUUID().toString();
			String saveName = uploadPath + File.separator + folderPath + File.separator + uuid + "_" + fileName;
			Path savePath = Paths.get(saveName);
			
			try {
				uploadFile.transferTo(savePath);
				// サムネールを作る。
                String thumbnailSaveName = uploadPath + File.separator + folderPath + File.separator +
                        "s_" + uuid + "_" + fileName;
                Thumbnailator.createThumbnail(savePath.toFile(), new File(thumbnailSaveName), 100, 100);
                
                // 結果DTOを作る。
                AttachFileDTO dto = new AttachFileDTO(fileName, uuid, folderPath);
                resultDTOList.add(new AttachFileDTO(fileName, uuid, folderPath));
                
                //DBにセーフする
                UploadedFile entity = UploadedFile.builder()
                		.userId(1L) //実際に構築する場合は、ローグインしたユーザIDを使用します。
                		.fileName(fileName)
                		.githubUrl(generateGitHubUrl(folderPath, uuid, fileName))
                		.folderPath(folderPath)
                		.build();
                attachFileRepository.save(entity);
                
			} catch (IOException e) {
				log.error("File Upload Failed" + e);
			} 
		}
		return resultDTOList;
	}

    private String generateGitHubUrl(String folderPath, String uuid, String fileName) {
        try {
            return "https://john911120.github.io/yuru-archive/assets/uploads/" +
                    URLEncoder.encode(folderPath + "/" + uuid + "_" + fileName, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

	//添付したファイルを削除するロジックを実行します。
	@Override
	public boolean deleteFile(String fileName) {
        try {
            File file = new File(uploadPath + File.separator + fileName);
            boolean result = file.delete();
            File thumbnail = new File(file.getParent(), "s_" + file.getName());
            thumbnail.delete();
            return result;
        } catch (Exception e) {
            log.error("File deletion failed", e);
            return false;
        }
	}
	
	/*
	 *  uploadPathをサービスの内側で、インジェクションされて、
	 *  コントローラーがそのValueだけを照会できるように
	 *	getUploadPath()を追加しました。
	 */
	
	@Override
	public String getUploadPath() {
		return uploadPath;
	}

	// ファイルが存在しない場合は、このクラスで、ファイルを作ります。
    private String makeFolder() {
        String str = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String folderPath = str.replace("/", File.separator);
        File uploadPathFolder = new File(uploadPath, folderPath);
        if (!uploadPathFolder.exists()) {
            uploadPathFolder.mkdirs();
        }
        return folderPath;
    }

	
}
