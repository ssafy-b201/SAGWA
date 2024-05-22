package com.ssafy.devway.domain.phoneBook.entity;

import com.ssafy.devway.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PhoneBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long phoneBookId;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    private String phoneBookNumber;

    @NotNull
    private String phoneBookName;

    public void updatePhoneBook(String phoneBookName) {
        this.phoneBookName = phoneBookName;
    }

}
