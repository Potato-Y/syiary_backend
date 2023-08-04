package io.potatoy.syiary.token.dto;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateRequest {
    @NotNull
    @Email
    private String email;
    @NotNull
    private String password;
}
