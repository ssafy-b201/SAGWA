package com.ssafy.devway.domain.member.dto.request;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MemberReqDto {
    @JsonProperty("email")
    private String email;
}