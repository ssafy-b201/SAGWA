package com.ssafy.devway.domain.phoneBook.service;

import com.ssafy.devway.domain.phoneBook.dto.request.PhoneBookReqDTO;
import com.ssafy.devway.domain.phoneBook.dto.response.PhoneBookResDTO;
import com.ssafy.devway.domain.phoneBook.entity.PhoneBook;
import com.ssafy.devway.domain.phoneBook.repository.PhoneBookRepository;
import com.ssafy.devway.domain.member.entity.Member;
import com.ssafy.devway.domain.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PhoneBookService {

    private final PhoneBookRepository phoneBookRepository;
    private final MemberRepository memberRepository;

    public PhoneBook insertPhoneBook(PhoneBookReqDTO dto) {
        PhoneBook p = phoneBookRepository.findByMember_MemberIdAndPhoneBookNumber(dto.getMemberId(),
            dto.getPhoneBookNumber());
        if (p != null) {
            p.updatePhoneBook(dto.getPhoneBookName());
            return p;
        }
        Member member = memberRepository.findByMemberId(dto.getMemberId());
        PhoneBook phoneBook = PhoneBook.builder()
            .member(member)
            .phoneBookNumber(dto.getPhoneBookNumber())
            .phoneBookName(dto.getPhoneBookName())
            .build();
        phoneBookRepository.save(phoneBook);

        return phoneBook;
    }

    @Transactional(readOnly = true)
    public List<PhoneBookResDTO> selectPhoneBook(Long memberId) {
        List<PhoneBook> list = phoneBookRepository.findByMember_MemberId(memberId);

        return list.stream()
            .map(this::convertToDto)
            .toList();
    }

    public Long deletePhoneBook(Long phoneBookId) {
        phoneBookRepository.deleteById(phoneBookId);
        return phoneBookId;
    }

    private PhoneBookResDTO convertToDto(PhoneBook phoneBook) {
        return new PhoneBookResDTO(
            phoneBook.getPhoneBookId(), phoneBook.getPhoneBookNumber(), phoneBook.getPhoneBookName()
        );
    }

}
