package com.ssafy.devway.domain.diary.repository;

import com.ssafy.devway.domain.diary.entity.Diary;
import com.ssafy.devway.domain.member.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    List<Diary> findAllByMember(Member member);

    @Query(nativeQuery = true, value = "select * from diary where diary_date = :date and member_id=:memberId")
    Optional<Diary> findDiaryByDiaryDateandMemberId(@Param("date") String date, @Param("memberId") Long memberId);

}
