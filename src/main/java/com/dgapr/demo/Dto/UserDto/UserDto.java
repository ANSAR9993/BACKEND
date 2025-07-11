package com.dgapr.demo.Dto.UserDto;

import com.dgapr.demo.Model.User.Role;
import com.dgapr.demo.Model.User.UserStatu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
public class UserDto {

    private String username;
    private String email;
    private String password;
    private String firstname;
    private String lastname;
    private String idNumber;
    private UserStatu status = UserStatu.ACTIVE;
    private Instant createdAt = Instant.now();
    private String createdBy;
    private Instant updatedAt = Instant.now();
    private String updatedBy;
    private Role role;
}