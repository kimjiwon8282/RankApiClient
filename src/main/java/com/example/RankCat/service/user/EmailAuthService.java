package com.example.RankCat.service.user;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthService {

    private final StringRedisTemplate redisTemplate; // Redis 연동용 빈
    private final MailService mailService;
    private static final long EXPIRE_MINUTES = 5;    // 인증코드 유효기간(5분)

    /**
     * 1. 인증코드를 생성해서 Redis에 저장(5분 만료) + 실제 이메일로 발송
     * @param email 인증코드를 받을 대상 이메일
     */
    public void sendAuthCode(String email) {
        String code = generateCode(); // 6자리 인증코드 생성
        log.info("code: {}", code);
        // Redis에 "emailauth:{이메일}" → code, TTL 5분 설정
        redisTemplate.opsForValue().set("emailauth:" + email, code, EXPIRE_MINUTES, TimeUnit.MINUTES);

        mailService.sendAuthCodeMail(email, code);
    }

    /**
     * 2. 인증코드 검증(입력값이 Redis에 저장된 코드와 일치하는지, 만료되지 않았는지)
     * @param email 인증을 요청하는 이메일
     * @param code 사용자가 입력한 인증코드
     * @return true: 인증 성공(일치), false: 실패(불일치 또는 만료)
     */
    public boolean verifyAuthCode(String email, String code) {
        String key = "emailauth:" + email;
        String value = redisTemplate.opsForValue().get(key);
        boolean match = value != null && value.equals(code);
        if (match) {
            redisTemplate.delete(key); // 성공 시 코드 삭제(1회용)
        }
        return match;
    }

    /**
     * 3. 인증코드(6자리 숫자) 생성
     * @return 랜덤 6자리 숫자(문자열)
     */
    private String generateCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(900000) + 100000; // 100000~999999 (6자리)
        return String.valueOf(number);
    }
}
