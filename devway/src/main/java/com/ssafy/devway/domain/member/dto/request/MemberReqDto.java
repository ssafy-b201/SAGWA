package com.ssafy.devway.domain.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
public class MemberReqDto {
    String email;
}
