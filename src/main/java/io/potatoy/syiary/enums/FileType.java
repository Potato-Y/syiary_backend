package io.potatoy.syiary.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileType {

    IMAGE("image"), // 사진
    VIDEO("video"); // 영상

    private String type;

    FileType(String type) {
        this.type = type;
    }
}
