package io.potatoy.syiary.group;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import io.potatoy.syiary.group.dto.SecessionGroupRequest;
import io.potatoy.syiary.group.dto.SignupGroupRequest;
import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMember;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.entity.GroupRepository;
import io.potatoy.syiary.group.exception.GroupException;
import io.potatoy.syiary.group.exception.GroupMemberException;
import io.potatoy.syiary.group.util.GroupMemberUtil;
import io.potatoy.syiary.security.util.SecurityUtil;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.entity.UserRepository;
import io.potatoy.syiary.user.exception.NotFoundUserException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GroupMemberService {

    private final Logger logger = LogManager.getLogger(GroupMemberService.class);
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberUtil groupMemberUtil;

    /**
     * Host가 사용자를 group에 추가한다.
     * 
     * @param groupUri
     * @param dto
     */
    public void signupGroup(String groupUri, SignupGroupRequest dto) {
        // 유저 정보를 가져온다.
        User user = securityUtil.getCurrentUser();

        // 그룹 정보를 불러온다.
        Optional<Group> _group = groupRepository.findByGroupUri(groupUri);
        if (_group.isEmpty()) {
            String message = "Group not found.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        Group group = _group.get();

        // 요청자가 host가 맞는지 확인한다.
        if (!group.getHostUser().getId().equals(user.getId())) {
            // Group의 Host User와 User가 동일하지 않습니다.
            String message = "Group Host User and User are not the same.";
            logger.warn("signupGroup:GroupException. userId={}, groupId={}\nmessage={}", user.getId(), group.getId(),
                    message);

            throw new GroupException(message);
        }

        // 초대하고자 하는 유저가 존재하는지 확인하고, 존재하면 불러온다.
        Optional<User> guestUser = userRepository.findByEmail(dto.getUserEmail());
        if (guestUser.isEmpty()) {
            String message = "User not found.";
            logger.warn("signupGroup:NotFoundUserEmailException. message={}", message);

            throw new NotFoundUserException(message);
        }

        // 이미 추가되어 있는 유저인지 확인한다.
        Optional<GroupMember> member = groupMemberRepository.findByUserAndGroup(guestUser.get(), group);
        if (member.isPresent()) {
            // 유저가 이미 그룹에 있음.
            String message = "User is already in a group.";
            logger.warn("signupGroup:GroupException. groupId={}, userId={}, guestUserId={}", group.getId(),
                    user.getId(), guestUser.get().getId());

            throw new GroupException(message);
        }

        // 유저 추가 및 저장
        groupMemberRepository.save(
                groupMemberUtil.createGroupMemberEntity(guestUser.get(), group));
    }

    /**
     * host가 group에 속해있는 특정 user를 탈퇴시킨다.
     * 혹은 자신이 group에서 탈퇴한다.
     * 
     * @param groupUri
     * @param dto
     */
    public void secessionGroup(String groupUri, SecessionGroupRequest dto) {
        // 유저 정보를 가져온다.
        User user = securityUtil.getCurrentUser();

        // 그룹 정보를 불러온다.
        Optional<Group> _group = groupRepository.findByGroupUri(groupUri);
        if (_group.isEmpty()) {
            String message = "Group not found.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        Group group = _group.get();

        // 특정 유저를 탈퇴시키려 하면 host 유저가 맞는지 확인
        if (!dto.getUserEmail().isBlank()) {
            // 요청자가 host가 맞는지 확인한다.
            if (!group.getHostUser().getId().equals(user.getId())) {
                // Group의 Host User와 User가 동일하지 않습니다.
                String message = "Group Host User and User are not the same.";
                logger.warn("secessionGroup:GroupException. userId={}, groupId={}\nmessage={}", user.getId(),
                        group.getId(),
                        message);

                throw new GroupException(message);
            }
        }

        // 탈퇴하고자 하는 유저가 존재하는지 확인하고, 존재하면 불러온다.
        User leaveUser;
        if (dto.getUserEmail().isEmpty()) {
            // 자기 자신을 탈퇴하려는 경우
            leaveUser = user;
        } else {
            Optional<User> _leaveUser = userRepository.findByEmail(dto.getUserEmail());
            if (_leaveUser.isEmpty()) {
                String message = "User not found.";
                logger.warn("secessionGroup:NotFoundUserEmailException. userId={}, leaveUserEmail={}\nmessage={}",
                        user.getId(), dto.getUserEmail(), message);

                throw new NotFoundUserException(message);
            }
            leaveUser = _leaveUser.get();
        }

        // leaveUser가 host 유저인지 확인
        if (group.getHostUser().getEmail().equals(leaveUser.getEmail())) {
            String message = "A host cannot leave.";
            logger.warn("secessionGroup:GroupMemberException. userId={}, groupId={}, leaveUserId={}\nmessage={}",
                    user.getId(), group.getId(), leaveUser.getId(), message);

            throw new GroupMemberException(message);
        }

        // 멤버 디비에서 불러온다.
        Optional<GroupMember> memberUser = groupMemberRepository.findByUserAndGroup(leaveUser, group);
        if (memberUser.isEmpty()) {
            String message = "There are no users in the member list.";
            logger.warn("secessionGroup:GroupMemberException. userId={}, groupId={}, leaveUserId={}\nmessage={}",
                    user.getId(), group.getId(), leaveUser.getId(), message);

            throw new GroupMemberException(message);
        }

        groupMemberRepository.delete(memberUser.get());
    }
}
