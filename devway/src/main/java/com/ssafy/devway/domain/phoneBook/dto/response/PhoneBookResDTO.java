package com.ssafy.devway.domain.phoneBook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PhoneBookResDTO {

    private String number;
    private String name;
}
