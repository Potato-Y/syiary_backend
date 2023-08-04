package io.potatoy.syiary.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.potatoy.syiary.token.dto.AuthenticateRequest;
import io.potatoy.syiary.token.dto.AuthenticateResponse;
import io.potatoy.syiary.token.dto.TokenRequest;
import io.potatoy.syiary.token.dto.TokenResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TokenApiController {

    private final TokenService tokenService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(tokenService.createNewTokenSet(request));
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@RequestBody TokenRequest request) {
        String accessToken = tokenService.createNewAccessToken(request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new TokenResponse(accessToken));
    }
}
