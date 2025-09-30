package com.example.RankCat.service.user;

import com.example.RankCat.dto.user.AddUserRequest;
import com.example.RankCat.model.User;
import com.example.RankCat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService { //회원가입 서비스
    private final UserRepository userRepository;

    public Long save(AddUserRequest dto){ //애플리케이션 내부 회원가입 로직
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        return userRepository.save(User.builder()
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .password(encoder.encode(dto.getPassword()))
                .build()).getId();
    }
    public User findById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("Unexpected user"));
    }
    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("Unexpected user"));
    }
    // 이메일 존재 여부 반환
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}