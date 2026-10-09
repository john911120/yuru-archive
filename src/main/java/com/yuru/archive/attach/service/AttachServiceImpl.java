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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.attach.repository.AttachFileRepository;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;
import com.yuru.archive.util.FileValidator;

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
    public void validateFiles(MultipartFile[] uploadFiles) {
        if (uploadFiles == null) {
            return;
        }

        for (MultipartFile uploadFile : uploadFiles) {
            if (uploadFile == null || uploadFile.isEmpty()) {
                continue;
            }
            String fileName = sanitizeOriginalFilename(uploadFile.getOriginalFilename());
            if (fileName == null) {
                throw new com.yuru.archive.exception.InvalidUploadFileException("不正なファイル名です。");
            }
            FileValidator.validateImage(uploadFile, fileName);
        }
    }

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
            String fileName = sanitizeOriginalFilename(originalName);
            if (fileName == null) {
                log.warn("不正なファイル名を拒否しました: {}", originalName);
                continue;
            }

            FileValidator.validateImage(uploadFile, fileName);

            try {
                String folderPath = makeFolder();
                String uuid = UUID.randomUUID().toString();
                String folderForDisk = folderPath.replace("/", File.separator);
                Path targetDirectory = Paths.get(uploadPath, folderForDisk).toAbsolutePath().normalize();
                Path savePath = targetDirectory.resolve(uuid + "_" + fileName).normalize();

                if (!savePath.startsWith(targetDirectory)) {
                    log.warn("アップロード先ディレクトリ外を指すファイル名を拒否しました: {}", originalName);
                    continue;
                }

                uploadFile.transferTo(savePath);

                File thumbnail = targetDirectory.resolve("s_" + uuid + "_" + fileName).toFile();
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
    public void deleteOwnedFiles(List<Long> fileIds, Long questionId, Long userId) {
        if (fileIds == null || fileIds.isEmpty()) {
            return;
        }

        List<Long> requestedIds = fileIds.stream().distinct().toList();
        List<UploadedFile> ownedFiles = attachFileRepository
                .findByIdInAndQuestion_IdAndUserId(requestedIds, questionId, userId);

        if (ownedFiles.size() != requestedIds.size()) {
            throw new AccessDeniedException("削除対象に権限のない添付ファイルが含まれています。");
        }

        // 全件の所有権確認が完了してから実ファイルを削除する。
        // 途中で権限エラーが発生して一部だけ削除される状態を防ぐ。
        for (UploadedFile file : ownedFiles) {
            Path original = resolveStoredFile(file, false);
            Path thumbnail = resolveStoredFile(file, true);
            deleteIfExists(original.toFile());
            deleteIfExists(thumbnail.toFile());
        }
        attachFileRepository.deleteAll(ownedFiles);
    }


    private Path resolveStoredFile(UploadedFile file, boolean thumbnail) {
        String safeFileName = sanitizeOriginalFilename(file.getFileName());
        if (safeFileName == null) {
            throw new IllegalArgumentException("不正な保存ファイル名です: " + file.getId());
        }

        Path uploadRoot = Paths.get(uploadPath).toAbsolutePath().normalize();
        String storedFolder = file.getFolderPath() == null ? "" : file.getFolderPath().replace('\\', '/');
        Path targetDirectory = uploadRoot.resolve(storedFolder).normalize();
        String storedName = (thumbnail ? "s_" : "") + file.getUuid() + "_" + safeFileName;
        Path target = targetDirectory.resolve(storedName).normalize();

        if (!target.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("アップロード領域外のファイル参照を拒否しました: " + file.getId());
        }
        return target;
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


    private String sanitizeOriginalFilename(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return null;
        }

        String normalized = originalName.replace('\\', '/');
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1).trim();

        if (fileName.isBlank() || fileName.equals(".") || fileName.equals("..")) {
            return null;
        }
        return fileName;
    }

    private void deleteIfExists(File file) {
        if (file.exists() && !file.delete()) {
            log.warn("ファイルを削除できませんでした: {}", file.getAbsolutePath());
        }
    }
}
