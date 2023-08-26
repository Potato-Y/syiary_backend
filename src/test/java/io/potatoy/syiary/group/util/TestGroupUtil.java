package io.potatoy.syiary.group.util;

import java.util.Optional;

import io.potatoy.syiary.enums.State;
import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMember;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.entity.GroupRepository;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.util.UriMaker;

public class TestGroupUtil {

    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;

    public TestGroupUtil(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    /**
     * 새로운 테스트 그룹 생성하고 필드에 저장한다.
     * 
     * @param hostUser
     * @param groupName
     * @return
     */
    public Group createTestGroup(User hostUser, String groupName) {
        UriMaker groupUriMaker = new UriMaker(); // 그룹 id를 만들기 위해
        String groupUri;

        while (true) {
            groupUri = groupUriMaker.createName(); // 새로운 그룹 id 생성

            // 생성한 그룹 id가 이미 존재하는지 확인
            Optional<Group> loadGroup = groupRepository.findByGroupUri(groupUri);
            if (loadGroup.isEmpty()) {
                // 없을 경우 멈추기
                break;
            }

            // 이미 존재할 경우 다시 반복
        }

        Group group = groupRepository.save(
                Group.builder()
                        .groupUri(groupUri)
                        .groupName(groupName)
                        .hostUser(hostUser)
                        .state(State.ACTIVE)
                        .build());

        // 그룹 멤버 추가
        groupMemberRepository.save(createGroupMember(group, hostUser));

        return group;
    }

    /**
     * 그룹에 새로운 멤버를 추가하고 필드에 저장한다.
     * 
     * @param group
     * @param user
     * @return
     */
    public GroupMember createGroupMember(Group group, User user) {
        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(user)
                .build();

        return groupMemberRepository.save(groupMember);
    }
}
