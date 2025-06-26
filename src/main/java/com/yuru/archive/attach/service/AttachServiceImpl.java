package com.yuru.archive.attach.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.attach.repository.AttachFileRepository;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnailator;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachServiceImpl implements AttachService {
	
    @Value("${com.yuru.archive.upload.path}")
	private String uploadPath;
    
    private final AttachFileRepository attachFileRepository;

	//添付ファイルをアップロードロジックを処理します。
	@Override
	public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question, SiteUser user) {
		List<AttachFileDTO> resultDTOList = new ArrayList<>();
		
		for(MultipartFile uploadFile : uploadFiles) {
			if (!uploadFile.getContentType().startsWith("image")) {
				log.warn("イメージファイルではありません。");
				continue;
			}
			
			String originalName = uploadFile.getOriginalFilename();
			String fileName = originalName.substring(originalName.lastIndexOf("\\")+1);
			String folderPath = makeFolder();
			log.info("✅ folderPath = {}", folderPath);
			String uuid = UUID.randomUUID().toString();
			String folderForDisk = folderPath.replace("/", File.separator); // OSに 合うDirectory (Windows: "\", Unix: "/")
			String saveName = uploadPath + File.separator + folderForDisk + File.separator + uuid + "_" + fileName;
			Path savePath = Paths.get(saveName);
						
			try {
				uploadFile.transferTo(savePath);
				// サムネールを作る。
                String thumbnailSaveName = uploadPath + File.separator + folderPath + File.separator +
                        "s_" + uuid + "_" + fileName;
                Thumbnailator.createThumbnail(savePath.toFile(), new File(thumbnailSaveName), 100, 100);
                
                if (user == null) {
                	log.warn("❌ user is null!! DB登録をスキップします。");
                	continue;
                } else {
                	log.info("✅ user.getId() = {}", user.getId());
                }

                // 結果DTOを作る。
                Long userId = user.getId(); // 👍 OK

                AttachFileDTO dto = new AttachFileDTO(fileName, uuid, folderPath, userId);
                resultDTOList.add(dto);
                
                
                //DBにセーフする
                UploadedFile entity = UploadedFile.builder()
                		.userId(user.getId()) //実際に構築する場合は、ローグインしたユーザIDを使用します。
                		.fileName(fileName)
                		.folderPath(folderPath)
                		.uuid(uuid)
                		.question(question)
                		.build();
                attachFileRepository.save(entity);
                                
                log.info("[attachService] file upload : fileName={}, questionId={}", fileName, question.getId());
			} catch (IOException e) {
				log.error("File Upload Failed" + e);
			} 
		}
		return resultDTOList;
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
        String folderPathForDisk = str.replace("/", File.separator); // OS別 実際にセーフする Directory
        File uploadPathFolder = new File(uploadPath, folderPathForDisk);
        if (!uploadPathFolder.exists()) {
            uploadPathFolder.mkdirs();
        }
        // リターンするときは、URL専用のDirectoryに変換する
        return str;
        
    }
    
	@Override
	public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles) {
		return uploadFiles(uploadFiles, null, null);
	}
    
    // added helper method
	@Override
	public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question) {
		SiteUser user = (question != null) ? question.getAuthor() : null;
		return uploadFiles(uploadFiles, question, user);
	}
    
    // 添付ファイルを追加するメソッドを作成
	@Override
	public void uploadFilesFromDTOs(List<AttachFileDTO> fileDTOs, Question question, SiteUser user) {
		for(AttachFileDTO dto : fileDTOs) {
			UploadedFile entity = new UploadedFile();
			entity.setFileName(dto.getFileName());
			entity.setFolderPath(dto.getFolderPath());
			entity.setUuid(dto.getUuid());
			entity.setQuestion(question);
			entity.setUserId(user.getId());
			attachFileRepository.save(entity);
		}
	}
	
	// 添付ファイルを削除するサービスロジック
	public boolean deleteFileById(Long fileId) {
	    UploadedFile file = attachFileRepository.findById(fileId)
	            .orElseThrow(() -> new RuntimeException("ファイルが存在しません"));

	        File original = new File(uploadPath, file.getFolderPath() + "/" + file.getUuid() + "_" + file.getFileName());
	        File thumbnail = new File(uploadPath, file.getFolderPath() + "/s_" + file.getUuid() + "_" + file.getFileName());

	        if (original.exists()) original.delete();
	        if (thumbnail.exists()) thumbnail.delete();

	        attachFileRepository.delete(file);
	        return true;
	}
	
}
