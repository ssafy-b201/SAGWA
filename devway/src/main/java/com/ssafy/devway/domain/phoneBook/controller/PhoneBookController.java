package com.ssafy.devway.domain.phoneBook.controller;

import com.ssafy.devway.domain.phoneBook.dto.request.PhoneBookReqDTO;
import com.ssafy.devway.domain.phoneBook.dto.response.PhoneBookResDTO;
import com.ssafy.devway.domain.phoneBook.entity.PhoneBook;
import com.ssafy.devway.domain.phoneBook.service.PhoneBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sagwa/api/phoneBook")
@Tag(name = "통화", description = "Call API")
public class PhoneBookController {

    private final PhoneBookService phoneBookService;

    @PostMapping
    @Operation(summary = "통화 대상 추가")
    public ResponseEntity<PhoneBook> postPhoneBook(@RequestBody PhoneBookReqDTO dto) {
        return ResponseEntity.ok(phoneBookService.insertPhoneBook(dto));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "통화 대상 조회")
    public ResponseEntity<List<PhoneBookResDTO>> getPhoneBookList(@PathVariable Long memberId){
        return ResponseEntity.ok(phoneBookService.selectPhoneBook(memberId));
    }

    @DeleteMapping("/{phoneBookId}")
    @Operation(summary = "통화 대상 삭제")
    public ResponseEntity<Long> deletePhoneBook(@PathVariable Long phoneBookId){
        return ResponseEntity.ok(phoneBookService.deletePhoneBook(phoneBookId));
    }
}
