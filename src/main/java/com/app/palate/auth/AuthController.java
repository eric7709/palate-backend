package com.app.palate.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/palate/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO signup(@RequestBody AccountRequestDTO request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MeDTO me(@RequestHeader("Authorization") String authHeader) {
        return authService.me(authHeader);
    }
    
    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequestDTO request) {
        return authService.changePassword(authHeader, request);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AccessTokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }
}