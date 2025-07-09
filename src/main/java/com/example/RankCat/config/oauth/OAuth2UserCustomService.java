package com.example.RankCat.config.oauth;

import com.example.RankCat.model.User;
import com.example.RankCat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class OAuth2UserCustomService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String nickname;

        // 네이버: response 내부에 email, nickname이 있음
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            email = response == null ? null : (String) response.get("email");
            nickname = response == null ? null : (String) response.get("nickname");
        } else {
            // 구글 등 기타
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name"); // 필요에 따라 "nickname"으로
        }

        if (email == null || nickname == null) {
            throw new IllegalArgumentException("소셜 로그인에서 email 또는 nickname을 제공하지 않았습니다. 동의 항목을 확인하세요.");
        }

        // DB에 저장하거나 업데이트
        User user = userRepository.findByEmail(email)
                .map(e -> e.update(nickname))
                .orElse(User.builder()
                        .email(email)
                        .nickname(nickname)
                        .build());

        userRepository.save(user);

        return oAuth2User;
    }
}

