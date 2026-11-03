package com.yuru.archive.attach.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.entity.UploadedFile;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

public interface AttachService {

    void validateFiles(MultipartFile[] uploadFiles);

    void deleteOwnedFiles(List<Long> fileIds, Long questionId, Long userId);

    void deleteFileRecordsByQuestionId(Long questionId);

    /**
     * 2026-11-03 追加: 質問に紐づく添付ファイルのDBレコードと実ファイル（原本・サムネイル）を削除する。
     * 実ファイルはTransactionのCommit後に削除する。
     */
    void deleteFilesByQuestionId(Long questionId);

    List<UploadedFile> getFilesByQuestionId(Long questionId);

    List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles);

    List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question);

    List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question, SiteUser user);
}
