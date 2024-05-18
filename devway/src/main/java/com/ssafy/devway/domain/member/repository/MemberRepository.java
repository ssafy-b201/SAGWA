package com.ssafy.devway.domain.member.repository;

import com.ssafy.devway.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByMemberEmail(String memberEmail);

    Member findByMemberId(Long memberId);
}

