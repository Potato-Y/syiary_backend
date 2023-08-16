package io.potatoy.syiary.group.dto;

import java.util.List;

import io.potatoy.syiary.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor // 기본 생성자 추가
@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자 추가
@Getter
public class GroupMemberResponse {

    private UserResponse hostUser;
    private List<UserResponse> memberUser;
}
