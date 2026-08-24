package com.yuru.archive.attach.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

public interface AttachService {

    boolean deleteFile(String fileName);

    boolean deleteFileById(Long fileId);

    void deleteFileRecordsByQuestionId(Long questionId);

    String getUploadPath();

    List<UploadedFile> getFilesByQuestionId(Long questionId);

    List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles);

    List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question);

    List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question, SiteUser user);
}
