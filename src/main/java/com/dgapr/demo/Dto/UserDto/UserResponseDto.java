package com.dgapr.demo.Dto.UserDto;

import com.dgapr.demo.Model.User.Role;
import com.dgapr.demo.Model.User.UserStatu;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class UserResponseDto {

    private UUID id;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private String idNumber;
    private UserStatu status;
    private Instant createdAt = Instant.now();
    private String createdBy;
    private Instant updatedAt = Instant.now();
    private String updatedBy;
    private Role role;

}
