package io.potatoy.syiary.group.util;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMember;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.exception.GroupMemberException;
import io.potatoy.syiary.user.entity.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class GroupUtil {

    private final Logger logger = LogManager.getLogger(GroupUtil.class);
    private final GroupMemberRepository groupMemberRepository;

    /**
     * 그룹 유저가 맞는지 확인
     * 
     * @param user
     * @param group
     */
    public void checkGroupUser(User user, Group group) {
        Optional<GroupMember> groupMember = groupMemberRepository.findByUserAndGroup(user, group);

        if (groupMember.isEmpty()) {
            // 멤버에 없을 경우 예외 발생
            String message = "There are no users in the member list.";
            logger.warn("secessionGroup:GroupMemberException. userId={}, groupId={}\nmessage={}",
                    user.getId(), group.getId(), message);

            throw new GroupMemberException(message);
        }
    }
}
