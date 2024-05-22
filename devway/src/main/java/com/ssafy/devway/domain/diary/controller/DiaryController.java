package com.ssafy.devway.domain.diary.controller;

import com.ssafy.devway.domain.diary.dto.request.DiaryCUReqDto;
import com.ssafy.devway.domain.diary.dto.response.DiaryDetailResDto;
import com.ssafy.devway.domain.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/sagwa/api/diary")
@RequiredArgsConstructor
@Tag(name = "일기", description = "Diary API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "일기 생성")
    public ResponseEntity<String> postDiary(Long memberId, String date,
        @RequestPart(value = "imgFile") MultipartFile imgFile,
        @RequestPart(value = "dto") DiaryCUReqDto dto) {
        return ResponseEntity.ok(diaryService.insertDiary(memberId, date, imgFile, dto));
    }

    @GetMapping("/{date}")
    @Operation(summary = "선택한 날짜의 일기 조회")
    public ResponseEntity<DiaryDetailResDto> getDiaryOne(Long memberId, @PathVariable String date) {
        return ResponseEntity.ok(diaryService.selectDiaryOne(memberId, date));
    }

    @DeleteMapping("/{diaryId}")
    @Operation(summary = "선택한 날짜의 일기 삭제")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok().build();
    }

}
