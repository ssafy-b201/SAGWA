package com.ssafy.devway.domain.member.controller;

import com.ssafy.devway.domain.member.dto.request.MemberReqDto;
import com.ssafy.devway.domain.member.entity.Member;
import com.ssafy.devway.domain.member.service.MemberService;
import com.ssafy.devway.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sagwa/api")
@Tag(name = "회원", description = "member API")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public ApiResponse<Long> signup(@RequestBody MemberReqDto dto){
        return ApiResponse.ok(memberService.signup(dto));
    }

    @GetMapping("/signin")
    public ApiResponse<Long> signin(String email){
        return ApiResponse.ok(memberService.signinMember(email));
    }

    @GetMapping("/valid")
    public ApiResponse<Boolean> valid(String email){
        return ApiResponse.ok(memberService.validMember(email));
    }

}

