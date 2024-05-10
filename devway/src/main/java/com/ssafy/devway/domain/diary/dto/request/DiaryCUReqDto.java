package com.ssafy.devway.domain.diary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@Builder
public class DiaryCUReqDto {

    private String title;
    private String content;

}
