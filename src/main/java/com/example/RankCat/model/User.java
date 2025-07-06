package com.example.RankCat.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Entity                                 // JPA 엔티티 지정: users 테이블과 매핑
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // 파라미터 없는 기본 생성자를 protected로 제한
@Table(name = "users")                 // 매핑할 테이블 이름 지정
public class User implements UserDetails {  // Spring Security 인증용 인터페이스 구현
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT 방식
    @Column(name = "id", updatable = false)
    private Long id;                                   // PK, 변경 불가

    @Column(name = "email", nullable = false, unique = true)
    private String email;                               // 로그인 아이디(email)

    @Column(name = "password")
    private String password;                            // 암호화된 비밀번호

    @Column(name = "nickname", unique = true)
    private String nickname;                            // 화면에 표시할 별명

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    /**
     * 인증된 사용자가 가진 권한 목록을 반환
     * @return SimpleGrantedAuthority 리스트 (예: "user")
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    /** 로그인 시 Spring Security가 내부적으로 호출하는 사용자 식별값 */
    @Override
    public String getUsername() {
        return email;
    }

    /** 로그인 시 Spring Security가 내부적으로 호출하는 비밀번호 */
    @Override
    public String getPassword() {
        return password;
    }

    // 아래 네 가지는 계정 상태(만료, 잠금, 자격 만료, 활성화)를 체크하는 메서드
    @Override public boolean isAccountNonExpired()     { return true; } // 만료 여부
    @Override public boolean isAccountNonLocked()      { return true; } // 잠금 여부
    @Override public boolean isCredentialsNonExpired() { return true; } // 자격 만료 여부
    @Override public boolean isEnabled()               { return true; } // 활성화 여부

    /**
     * 프로필 수정 등에서 닉네임만 바꿀 때 사용
     * @param nickname 새로운 닉네임
     * @return 변경된 User 객체
     */
    public User update(String nickname) {
        this.nickname = nickname;
        return this;
    }
}