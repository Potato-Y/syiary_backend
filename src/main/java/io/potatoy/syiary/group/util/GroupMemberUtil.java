package io.potatoy.syiary.group.util;

import org.springframework.stereotype.Component;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMember;
import io.potatoy.syiary.user.entity.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class GroupMemberUtil {

    /**
     * GroupMember 객체를 생성하여 반환한다.
     * 
     * @param user
     * @param group
     * @return
     */
    public GroupMember createGroupMemberEntity(User user, Group group) {
        return GroupMember.builder()
                .user(user)
                .group(group)
                .build();
    }
}
