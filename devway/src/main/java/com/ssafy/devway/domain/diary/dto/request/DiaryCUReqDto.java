package com.ssafy.devway.domain.diary.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DiaryCUReqDto {

    private String title;
    private String content;
    private String img;

}
