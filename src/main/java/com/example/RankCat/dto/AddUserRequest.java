package com.example.RankCat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddUserRequest {
    String email;
    String password;
}
