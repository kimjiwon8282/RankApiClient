package com.example.RankCat.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class HmacSHA256Util {
    private static final String ALG = "HmacSHA256"; //해시 알고리즘 이름

    public static String sign(String secretKey, String timestamp,
                              String method, String path) {//네이버에서 요구하는 서명 문자열
        try {
            String msg = String.join(".", timestamp, method, path);
            Mac mac = Mac.getInstance(ALG);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALG)); //비밀키로 HMAC초기화
            return Base64.getEncoder().encodeToString(
                    mac.doFinal(msg.getBytes(StandardCharsets.UTF_8))); //서명을 Base64 인코딩해 헤더에 넣을 문자열로 반환
        } catch (Exception e) {
            throw new IllegalStateException("Signature error", e);
        }
    }

    private HmacSHA256Util() {} //생성자 private -> 인스턴스 생성 방지
}