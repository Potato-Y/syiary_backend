package io.potatoy.syiary.group.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateGroupResponse {
    private Long id;
    private String groupUri;
    private String groupName;
}
