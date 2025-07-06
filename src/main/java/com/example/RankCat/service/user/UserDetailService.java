package com.example.RankCat.service.user;

import com.example.RankCat.model.User;
import com.example.RankCat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service  // Spring Bean으로 등록하여 DI 컨테이너 관리 대상이 됨
public class UserDetailService implements UserDetailsService {  // Spring Security가 인증 시 사용자 조회용으로 사용하는 서비스
    private final UserRepository userRepository;  // 사용자 조회를 위한 JPA 리포지토리 주입

    /**
     * 인증 처리 시 호출되는 메서드.
     * @param email 로그인 시 입력된 식별자(이메일)
     * @return UserDetails를 구현한 User 엔티티 반환
     * @throws IllegalArgumentException 해당 이메일로 사용자를 찾지 못하면 예외 발생
     */
    @Override
    public User loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(email + " 해당 사용자를 찾을 수 없습니다.")
                );
    }
}
