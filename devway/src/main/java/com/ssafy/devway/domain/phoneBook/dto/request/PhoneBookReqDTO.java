package com.ssafy.devway.domain.phoneBook.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PhoneBookReqDTO {

    private Long memberId;
    private String phoneBookNumber;
    private String phoneBookName;
}
