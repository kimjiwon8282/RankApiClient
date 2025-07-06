package com.example.RankCat.config.oauth;

import com.example.RankCat.model.User;
import com.example.RankCat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        // 1) OAuth2 프로바이더(Google, Kakao 등)에서 사용자 정보(post-login)를 가져온다
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2) 로컬 DB에 저장하거나 업데이트
        saveOrUpdate(oAuth2User);

        // 3) 스프링 시큐리티가 사용할 OAuth2User 객체를 반환
        return oAuth2User;
    }

    private User saveOrUpdate(OAuth2User oAuth2User) {
        // ① 프로바이더가 넘겨준 사용자 속성(Attributes) 꺼내기
        Map<String, Object> attrs = oAuth2User.getAttributes();
        String email = (String) attrs.get("email");   // ex. user@gmail.com
        String name  = (String) attrs.get("name");    // ex. 홍길동

        // ② DB에 이메일로 검색해 기존 사용자가 있으면 닉네임만 업데이트, 없으면 새로 생성
        User user = userRepository.findByEmail(email)
                .map(e -> e.update(name))  // 이미 가입된 유저 → 닉네임만 변경
                .orElse(User.builder()      // 신규 유저 → 이메일·닉네임 세팅
                        .email(email)
                        .nickname(name)
                        .build());

        // ③ 변경 사항을 DB에 저장(INSERT or UPDATE)
        return userRepository.save(user);
    }
}

