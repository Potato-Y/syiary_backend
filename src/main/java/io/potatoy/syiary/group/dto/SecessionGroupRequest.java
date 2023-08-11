package io.potatoy.syiary.group.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor // 기본 생성자 추가
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 추가
@Getter
@Setter
public class SecessionGroupRequest {
    // 사용자를 그룹에서 추방시킨다.

    @NotNull
    private String userEmail;
}
