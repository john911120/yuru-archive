package com.yuru.archive.attach.controller;

import java.io.File;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.service.AttachService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attach")
@Log4j2
public class AttachController {

    private final AttachService attachService;

    @PostMapping("/upload")
    public ResponseEntity<List<AttachFileDTO>> uploadFile(@RequestParam("uploadFiles") MultipartFile[] uploadFiles) {
        List<AttachFileDTO> result = attachService.uploadFiles(uploadFiles);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/display")
    public ResponseEntity<byte[]> getFile(@RequestParam String fileName, @RequestParam(required = false) String size) {
        try {
            String srcFileName = URLDecoder.decode(fileName, "UTF-8");
            File file = new File(attachService.getUploadPath(), srcFileName);

            if ("1".equals(size)) {
                file = new File(file.getParent(), file.getName().substring(2));
            }

            HttpHeaders header = new HttpHeaders();
            header.add("Content-Type", Files.probeContentType(file.toPath()));
            return new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);
        } catch (Exception e) {
            log.error("getFile error: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<Boolean> removeFile(@RequestParam String fileName) {
        boolean result = attachService.deleteFile(fileName);
        return new ResponseEntity<>(result, result ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
