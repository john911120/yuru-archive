package com.yuru.archive.attach.dto;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachFileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fileName;
    private String uuid;
    private String folderPath;
    private Long userId;

    public AttachFileDTO(String fileName, String uuid, String folderPath) {
        this(fileName, uuid, folderPath, null);
    }

    public AttachFileDTO(String fileName, String uuid, String folderPath, Long userId) {
        this.fileName = fileName;
        this.uuid = uuid;
        this.folderPath = folderPath;
        this.userId = userId;
    }

    public String getImageURL() {
        return URLEncoder.encode(folderPath + "/" + uuid + "_" + fileName, StandardCharsets.UTF_8);
    }

    public String getThumbnailURL() {
        return URLEncoder.encode(folderPath + "/s_" + uuid + "_" + fileName, StandardCharsets.UTF_8);
    }
}
