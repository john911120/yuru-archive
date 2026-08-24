package com.yuru.archive.attach.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Override
    public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question, SiteUser user) {
        List<AttachFileDTO> result = new ArrayList<>();
        if (uploadFiles == null) {
            return result;
        }

        for (MultipartFile uploadFile : uploadFiles) {
            if (uploadFile == null || uploadFile.isEmpty()) {
                continue;
            }

            String originalName = uploadFile.getOriginalFilename();
            if (originalName == null || !isAllowedExtension(originalName)) {
                log.warn("許可されていない拡張子です: {}", originalName);
                continue;
            }

            String contentType = uploadFile.getContentType();
            if (contentType == null || !contentType.startsWith("image")) {
                log.warn("画像ファイルではありません: {}", originalName);
                continue;
            }

            try {
                String fileName = originalName.substring(originalName.lastIndexOf('\\') + 1);
                String folderPath = makeFolder();
                String uuid = UUID.randomUUID().toString();
                String folderForDisk = folderPath.replace("/", File.separator);
                Path savePath = Paths.get(uploadPath, folderForDisk, uuid + "_" + fileName);

                uploadFile.transferTo(savePath);

                File thumbnail = Paths.get(uploadPath, folderForDisk, "s_" + uuid + "_" + fileName).toFile();
                createThumbnailOrFallback(savePath, thumbnail, originalName);

                Long userId = user != null ? user.getId() : null;
                AttachFileDTO dto = new AttachFileDTO(fileName, uuid, folderPath, userId);
                result.add(dto);

                if (question != null && user != null) {
                    UploadedFile entity = UploadedFile.builder()
                            .userId(user.getId())
                            .fileName(fileName)
                            .folderPath(folderPath)
                            .uuid(uuid)
                            .question(question)
                            .build();
                    attachFileRepository.save(entity);
                    log.info("添付ファイルを保存しました: fileName={}, questionId={}", fileName, question.getId());
                }
            } catch (IOException e) {
                log.error("添付ファイルの保存に失敗しました: {}", originalName, e);
            }
        }

        return result;
    }

    @Override
    public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles) {
        return uploadFiles(uploadFiles, null, null);
    }

    @Override
    public List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question) {
        SiteUser user = question != null ? question.getAuthor() : null;
        return uploadFiles(uploadFiles, question, user);
    }

    @Override
    public List<UploadedFile> getFilesByQuestionId(Long questionId) {
        return attachFileRepository.findByQuestion_Id(questionId);
    }

    @Override
    public void deleteFileRecordsByQuestionId(Long questionId) {
        attachFileRepository.deleteByQuestion_Id(questionId);
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            File file = new File(uploadPath + File.separator + fileName);
            boolean deleted = file.delete();
            File thumbnail = new File(file.getParent(), "s_" + file.getName());
            if (thumbnail.exists() && !thumbnail.delete()) {
                log.warn("サムネイルを削除できませんでした: {}", thumbnail.getAbsolutePath());
            }
            return deleted;
        } catch (Exception e) {
            log.error("添付ファイルの削除に失敗しました: {}", fileName, e);
            return false;
        }
    }

    @Override
    public boolean deleteFileById(Long fileId) {
        UploadedFile file = attachFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("ファイルが存在しません: " + fileId));

        String folderForDisk = file.getFolderPath().replace("/", File.separator);
        File original = Paths.get(uploadPath, folderForDisk, file.getUuid() + "_" + file.getFileName()).toFile();
        File thumbnail = Paths.get(uploadPath, folderForDisk, "s_" + file.getUuid() + "_" + file.getFileName()).toFile();

        deleteIfExists(original);
        deleteIfExists(thumbnail);
        attachFileRepository.delete(file);
        return true;
    }

    @Override
    public String getUploadPath() {
        return uploadPath;
    }

    private String makeFolder() {
        String folderPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String folderPathForDisk = folderPath.replace("/", File.separator);
        File directory = new File(uploadPath, folderPathForDisk);
        if (!directory.exists() && !directory.mkdirs()) {
            log.warn("アップロード先ディレクトリを作成できませんでした: {}", directory.getAbsolutePath());
        }
        return folderPath;
    }

    private void createThumbnailOrFallback(Path savePath, File thumbnail, String originalName) {
        try {
            Thumbnailator.createThumbnail(savePath.toFile(), thumbnail, 100, 100);
        } catch (IOException thumbnailException) {
            log.warn("サムネイル生成に失敗したため元画像を使用します: {}", originalName, thumbnailException);

            try {
                Files.copy(savePath, thumbnail.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException fallbackException) {
                log.warn(
                        "代替サムネイルの作成にも失敗しました。元画像の保存処理は継続します: {}",
                        originalName,
                        fallbackException);
            }
        }
    }

    private boolean isAllowedExtension(String filename) {
        String lowerName = filename.toLowerCase();
        return lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".gif")
                || lowerName.endsWith(".webp");
    }

    private void deleteIfExists(File file) {
        if (file.exists() && !file.delete()) {
            log.warn("ファイルを削除できませんでした: {}", file.getAbsolutePath());
        }
    }
}
