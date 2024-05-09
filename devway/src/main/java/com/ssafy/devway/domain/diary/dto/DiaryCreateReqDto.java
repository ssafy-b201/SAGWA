package com.ssafy.devway.domain.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DiaryCreateReqDto {
    private String title;
    private String content;
    private String img;
}
