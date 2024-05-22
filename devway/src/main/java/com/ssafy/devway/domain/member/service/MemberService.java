package com.ssafy.devway.domain.member.service;

import com.ssafy.devway.domain.member.dto.request.MemberReqDto;
import com.ssafy.devway.domain.member.entity.Member;
import com.ssafy.devway.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public Long signup(MemberReqDto dto) {
        Member member = memberRepository.findByMemberEmail(dto.getEmail());

        if (member != null) {
            return member.getMemberId();
        }
        Member newMember = Member.builder()
            .memberEmail(dto.getEmail())
            .build();
        memberRepository.save(newMember);
        return newMember.getMemberId();
    }

    public Long signinMember(String email) {
        Member member = memberRepository.findByMemberEmail(email);
        return member.getMemberId();
    }

    public Boolean validMember(String email) {
        Member findedMember = memberRepository.findByMemberEmail(email);
        return findedMember != null;
    }


}

