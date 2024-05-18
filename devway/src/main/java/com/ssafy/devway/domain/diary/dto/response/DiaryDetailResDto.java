package com.ssafy.devway.domain.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class DiaryDetailResDto {

    private Long diaryId;
    private String diaryTitle;
    private String diaryImg;
    private String diaryContent;
    private String diaryWeather;

}
