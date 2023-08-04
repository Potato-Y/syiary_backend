package io.potatoy.syiary.util;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.potatoy.syiary.enums.State;
import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMember;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.entity.GroupRepository;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.entity.UserRepository;

public class TestUtil {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;

    public TestUtil(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    public User createTestUser(String email, String password) {
        User user = userRepository.save(User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .build());

        return user;
    }

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
        groupMemberRepository.save(createGroupMember(hostUser, group));

        return group;
    }

    public GroupMember createGroupMember(User user, Group group) {
        GroupMember groupMember = GroupMember.builder()
                .user(user)
                .group(group)
                .build();

        return groupMemberRepository.save(groupMember);
    }
}
