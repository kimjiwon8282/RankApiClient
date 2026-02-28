package com.example.RankCat.service.user.interfaces;

public interface EmailAuthService {
    /** 인증코드 생성 후 메일 발송 및 저장소에 저장 */
    void sendAuthCode(String email);

    /** 사용자가 입력한 인증코드를 검증 (실패 시 예외 발생) */
    void verifyAuthCode(String email, String code);
}
