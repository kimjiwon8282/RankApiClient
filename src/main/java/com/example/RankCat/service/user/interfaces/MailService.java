package com.example.RankCat.service.user.interfaces;

public interface MailService {
    /**
     * 인증코드 메일 발송
     *
     * @param to 수신자 이메일
     * @param code 인증코드
     */
    void sendAuthCodeMail(String to, String code);
}
