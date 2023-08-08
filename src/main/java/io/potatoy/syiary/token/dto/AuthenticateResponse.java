package io.potatoy.syiary.token.dto;

import io.potatoy.syiary.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthenticateResponse {
    private String accessToken;
    private String refreshToken;

    private UserResponse user;
}
