package com.example.RankCat.service.user.impl;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.service.user.interfaces.EmailAuthService;
import com.example.RankCat.service.user.interfaces.MailService;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service // 향후 Redis 도입 시 이 어노테이션을 Redis 구현체 쪽으로 옮기거나 @Primary를 사용하면 됩니다.
public class MemoryEmailAuthServiceImpl implements EmailAuthService {

    // 현재는 메모리 기반. 나중에 Redis 구현체에서는 이 부분이 RedisTemplate으로 바뀝니다.
    private final Map<String, CodeEntry> authCodeStore = new ConcurrentHashMap<>();

    // 🔥 중요: SmtpMailServiceImpl이 아니라 인터페이스인 MailService에 의존합니다. (DIP 원칙)
    private final MailService mailService;

    private static final long EXPIRE_MILLIS = 5 * 60 * 1000L;

    @Override
    public void sendAuthCode(String email) {
        String code = generateCode();
        log.info("인증코드 생성: {}", code);
        long expiryTime = System.currentTimeMillis() + EXPIRE_MILLIS;

        authCodeStore.put(email, new CodeEntry(code, expiryTime));
        mailService.sendAuthCodeMail(email, code); // 다형성 활용
    }

    @Override
    public void verifyAuthCode(String email, String code) {
        CodeEntry entry = authCodeStore.get(email);

        if (entry == null || System.currentTimeMillis() > entry.expiryTime) {
            authCodeStore.remove(email);
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE);
        }

        if (!entry.code.equals(code)) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_CODE);
        }

        authCodeStore.remove(email);
    }

    private String generateCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    private static class CodeEntry {
        final String code;
        final long expiryTime;

        CodeEntry(String code, long expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }
}
