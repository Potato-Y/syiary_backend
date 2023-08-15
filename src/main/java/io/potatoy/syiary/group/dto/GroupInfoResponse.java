package io.potatoy.syiary.group.dto;

import java.time.LocalDateTime;

import io.potatoy.syiary.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor // 기본 생성자 추가
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 추가
@Getter
public class GroupInfoResponse {

    private Long id;
    private String groupUri;
    private String groupName;
    private LocalDateTime createAt;
    private UserResponse hostUser;
}
