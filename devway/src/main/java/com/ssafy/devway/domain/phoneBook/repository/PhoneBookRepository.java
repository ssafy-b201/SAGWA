package com.ssafy.devway.domain.phoneBook.repository;

import com.ssafy.devway.domain.phoneBook.dto.response.PhoneBookResDTO;
import com.ssafy.devway.domain.phoneBook.entity.PhoneBook;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneBookRepository extends JpaRepository<PhoneBook, Long> {

    List<PhoneBook> findByMember_MemberId(Long memberId);
    PhoneBook findByMember_MemberIdAndPhoneBookNumber(Long memberId, String phoneBookNumber);
}
