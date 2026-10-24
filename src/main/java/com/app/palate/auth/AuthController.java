package com.app.palate.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.app.palate.auth.dto.AccessTokenResponse;
import com.app.palate.auth.dto.AccountRequestDTO;
import com.app.palate.auth.dto.AuthResponseDTO;
import com.app.palate.auth.dto.ChangePasswordRequestDTO;
import com.app.palate.auth.dto.LoginRequestDTO;
import com.app.palate.auth.dto.MeDTO;
import com.app.palate.auth.dto.MessageResponse;
import com.app.palate.auth.dto.RefreshTokenRequest;
import com.app.palate.auth.services.ChangePassword;
import com.app.palate.auth.services.GetCurrentUser;
import com.app.palate.auth.services.LoginAccount;
import com.app.palate.auth.services.RefreshAccessToken;
import com.app.palate.auth.services.RegisterAccount;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/palate/auth")
public class AuthController {
    private final RegisterAccount registerAccount;
    private final LoginAccount loginAccount;
    private final GetCurrentUser getCurrentUser;
    private final ChangePassword changePassword;
    private final RefreshAccessToken refreshAccessToken;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO signup(@RequestBody AccountRequestDTO request) {
        return registerAccount.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        return loginAccount.login(request);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public MeDTO me(@RequestHeader("Authorization") String authHeader) {
        return getCurrentUser.get(authHeader);
    }

    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequestDTO request) {
        return changePassword.change(authHeader, request);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AccessTokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        return refreshAccessToken.refresh(request);
    }
}
