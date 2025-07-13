package com.example.RankCat.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * 인증코드 메일 발송
     * @param to 수신자 이메일
     * @param code 인증코드
     * @throws Exception 메일 전송 실패시
     */
    public void sendAuthCodeMail(String to, String code) {
        String subject = "[RankCat] 이메일 인증코드 안내";
        String text = String.format("안녕하세요!\n요청하신 인증코드는 [%s] 입니다.\n5분 이내에 입력해 주세요.", code);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("iwill0324@naver.com");

            mailSender.send(message);
            log.info("이메일 전송 완료: to={}", to);
        } catch (Exception e) {
            log.error("이메일 발송 실패: to={}, code={}, message={}", to, code, e.getMessage(), e);
            // 예: 서비스에서 실패로 인식할 경우, 런타임 예외를 던지거나, 커스텀 예외로 감싸서 던지세요.
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}