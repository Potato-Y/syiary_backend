package io.potatoy.syiary.token.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    private String accessToken;
    private String refreshToken;
}
