package com.ssafy.sagwa.api.Diary;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class DiaryCUReqDto {
    private String title;
    private String content;
}
