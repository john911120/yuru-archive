package com.yuru.archive.attach.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yuru.archive.attach.entity.UploadedFile;

public interface AttachFileRepository extends JpaRepository<UploadedFile, Long> {

    List<UploadedFile> findByUserId(Long userId);

    List<UploadedFile> findByQuestion_Id(Long questionId);

    List<UploadedFile> findByIdInAndQuestion_IdAndUserId(List<Long> ids, Long questionId, Long userId);

    void deleteByQuestion_Id(Long questionId);
}
