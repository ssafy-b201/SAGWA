package com.ssafy.devway.domain.diary.entity;

import com.ssafy.devway.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long diaryId;

    @Column(name = "diary_title")
    @NotNull
    private String diaryTitle;

    @Column(name = "diary_img")
    @NotNull
    private String diaryImg;

    @Column(name = "diary_content")
    @NotNull
    private String diaryContent;

    @Column(name = "diary_date")
    @NotNull
    private String diaryDate;

    @Column(name = "diary_weather")
    @NotNull
    private String diaryWeather;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @NotNull
    private Member member;

    public void updateDiary(String title, String img, String content) {
        this.diaryTitle = title;
        this.diaryImg = img;
        this.diaryContent = content;
    }

}
