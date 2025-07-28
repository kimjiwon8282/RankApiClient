package com.example.RankCat.service.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthService {

    // 메모리 기반 저장소: 이메일 → 인증코드 + 만료시간 저장
    private final Map<String, CodeEntry> authCodeStore = new ConcurrentHashMap<>();

    private final MailService mailService; // 이메일 전송 서비스

    private static final long EXPIRE_MILLIS = 5 * 60 * 1000L; // 인증코드 유효기간 5분 (밀리초)

    /**
     * 인증코드 생성 후 메일 발송 및 메모리 저장소에 저장
     *
     * @param email 인증코드를 받을 이메일 주소
     */
    public void sendAuthCode(String email) {
        String code = generateCode(); // 랜덤 6자리 숫자 생성
        log.info("인증코드 생성: {}", code);

        // 만료 시간 계산: 현재 시간 + 유효기간(5분)
        long expiryTime = System.currentTimeMillis() + EXPIRE_MILLIS;

        // 메모리에 저장 (email → code + expiryTime)
        authCodeStore.put(email, new CodeEntry(code, expiryTime));

        // 실제 이메일 발송
        mailService.sendAuthCodeMail(email, code);
    }

    /**
     * 사용자가 입력한 인증코드를 검증
     * - 저장된 코드와 일치하는지
     * - 만료되지는 않았는지
     *
     * @param email 이메일 주소
     * @param code 사용자가 입력한 인증코드
     * @return true: 인증 성공, false: 실패 또는 만료
     */
    public boolean verifyAuthCode(String email, String code) {
        CodeEntry entry = authCodeStore.get(email);

        // 저장된 값이 없거나, 유효시간 초과 시 인증 실패
        if (entry == null || System.currentTimeMillis() > entry.expiryTime) {
            authCodeStore.remove(email); // 만료된 코드는 삭제
            return false;
        }

        boolean match = entry.code.equals(code);

        if (match) {
            authCodeStore.remove(email); // 인증 성공 시 코드 삭제 (1회용)
        }

        return match;
    }

    /**
     * 6자리 숫자 인증코드 생성
     *
     * @return "123456" 형태의 문자열
     */
    private String generateCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(900000) + 100000; // 100000~999999 (6자리)
        return String.valueOf(number);
    }

    /**
     * 인증코드와 만료시간을 함께 저장하기 위한 내부 클래스
     */
    private static class CodeEntry {
        final String code;
        final long expiryTime;

        CodeEntry(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}