package io.potatoy.syiary.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Env {

    LOCAL("local"),
    PROD("prod");

    private String type;

    Env(String type) {
        this.type = type;
    }
}
