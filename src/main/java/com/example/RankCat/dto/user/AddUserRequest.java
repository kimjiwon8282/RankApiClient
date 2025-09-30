package com.example.RankCat.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
    String email;
    String nickname;
    String password;
}
