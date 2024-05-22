package com.ssafy.devway.domain.diary.service;

import com.ssafy.devway.domain.diary.dto.request.DiaryCUReqDto;
import com.ssafy.devway.domain.diary.dto.response.DiaryDetailResDto;
import com.ssafy.devway.domain.diary.entity.Diary;
import com.ssafy.devway.domain.diary.repository.DiaryRepository;
import com.ssafy.devway.domain.member.entity.Member;
import com.ssafy.devway.domain.member.repository.MemberRepository;
import com.ssafy.devway.image.ImageBlock;
import com.ssafy.devway.weather.WeatherBlock;
import com.ssafy.devway.weather.WeatherCountry;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    @Value("${api.weather.key}")
    private String apiWeatherKey;
    @Value(("${img.path.db}"))
    private String imgPathDB;
    @Value(("${img.path.user}"))
    private String imgPathUser;


    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final ImageBlock imageBlock = new ImageBlock();


    /*
     * 일기 생성
     * */
    public String insertDiary(Long memberId, String date, MultipartFile imgFile,
        DiaryCUReqDto dto) {
        Optional<Member> optMember = memberRepository.findById(memberId);
        if (optMember.isEmpty()) {
            throw new NoSuchElementException("해당 사용자가 없습니다.");
        }

        // 날씨
        LocalDate today = LocalDate.now();
        String weather = "";
        if (date.equals(String.valueOf(today))) {
            WeatherBlock weatherBlock = new WeatherBlock(apiWeatherKey);
            weatherBlock.todayWeather("", WeatherCountry.DAEJEON);
            weather = weatherBlock.getWeather();
            if (weather == null || weather.isEmpty()) {
                throw new NoSuchElementException("날씨를 받아오지 못했습니다.");
            }
        } else {
            weather = "none";
        }

        // 이미지
        String fileOriginName = imgFile.getOriginalFilename();
        System.out.println("fileOriginName: " + fileOriginName);
        String manualPath =
            "img/" + String.valueOf(memberId) + "/" + date + "/";
        String directoryPath = imgPathUser + manualPath; // 파일 저장 경로
        String filePath = directoryPath + fileOriginName; // 파일 저장 경로의 파일

        try {
            // 파일 저장 디렉토리 생성
            Path saveDirectoryPath = Paths.get(directoryPath);
            Files.createDirectories(saveDirectoryPath);

            // 파일 저장
            Files.copy(imgFile.getInputStream(), Paths.get(filePath),
                StandardCopyOption.REPLACE_EXISTING);

            String resultPath = imgPathDB + manualPath + fileOriginName;

//            System.out.println("dto: "+dto+","+dto.getClass());
            Diary diary = Diary.builder()
                .diaryTitle(dto.getTitle())
                .diaryImg(resultPath)
                .diaryContent(dto.getContent())
                .diaryDate(date)
                .diaryWeather(weather)
                .member(optMember.get())
                .build();

            diaryRepository.save(diary);
            return resultPath;
        } catch (IOException e) {
            throw new RuntimeException(e.getClass() + " : " + fileOriginName + "이 업로드되지 않았습니다.");
        }

    }


    /*
     * 선택한 날짜의 일기 조회
     * */
    @Transactional(readOnly = true)
    public DiaryDetailResDto selectDiaryOne(Long memberId, String date) {
        try {
            LocalDate inputDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new RuntimeException(e.getClass() + " : 날짜 형식이 올바르지 않습니다.");
        }

        Optional<Diary> optDiary = diaryRepository.findDiaryByDiaryDateandMemberId(date, memberId);
        if (optDiary.isEmpty()) {
            throw new NoSuchElementException("해당 날짜의 일기가 없습니다.");
        }

        Diary diary = optDiary.get();
        DiaryDetailResDto resDto = DiaryDetailResDto.builder()
            .diaryId(diary.getDiaryId())
            .diaryTitle(diary.getDiaryTitle())
            .diaryImg(diary.getDiaryImg())
            .diaryContent(diary.getDiaryContent())
            .diaryWeather(diary.getDiaryWeather())
            .build();

        return resDto;

    }

    /*
     * 일기 삭제
     * */
    public void deleteDiary(Long diaryId) {
        diaryRepository.deleteById(diaryId);
    }
}
