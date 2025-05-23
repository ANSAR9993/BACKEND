package com.dgapr.demo.Controller;

import com.dgapr.demo.Dto.AuthDto.AuthRequest;
import com.dgapr.demo.Dto.AuthDto.AuthResponse;
import com.dgapr.demo.Service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authenticationService.authenticate(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }

//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody AuthRequest request) {
//        AuthResponse response = authenticationService.authenticate(request);
//        // TODO : ....
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<AuthResponse> logout(@Valid @RequestBody AuthRequest request) {
//        AuthResponse response = authenticationService.authenticate(request);
//        // TODO : ....
//    }


}
