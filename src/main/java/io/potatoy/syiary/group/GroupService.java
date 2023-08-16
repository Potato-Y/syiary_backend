package io.potatoy.syiary.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.potatoy.syiary.group.entity.GroupMember;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.entity.GroupRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.user.dto.UserResponse;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.enums.State;
import io.potatoy.syiary.group.dto.CreateGroupRequest;
import io.potatoy.syiary.group.dto.CreateGroupResponse;
import io.potatoy.syiary.group.dto.DeleteGroupRequest;
import io.potatoy.syiary.group.dto.GroupInfoResponse;
import io.potatoy.syiary.group.exception.GroupException;
import io.potatoy.syiary.group.exception.GroupMemberException;
import io.potatoy.syiary.group.util.GroupMemberUtil;
import io.potatoy.syiary.post.PostService;
import io.potatoy.syiary.security.util.SecurityUtil;
import io.potatoy.syiary.util.UriMaker;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final Logger logger = LogManager.getLogger(GroupService.class);
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PostService postService;
    private final GroupMemberUtil groupMemberUtil;
    private final SecurityUtil securityUtil;

    /**
     * 새로운 그룹 생성
     * 
     * @param dto
     * @return
     */
    public CreateGroupResponse createGroup(CreateGroupRequest dto) {
        // User 정보 가져오기
        User user = securityUtil.getCurrentUser();

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

        // group 정보 저장
        Group group = groupRepository.save(
                Group.builder()
                        .groupUri(groupUri)
                        .groupName(dto.getGroupName())
                        .hostUser(user)
                        .state(State.ACTIVE)
                        .build());
        // group member에 만든 본인 추가
        groupMemberRepository.save(
                groupMemberUtil.createGroupMemberEntity(user, group));

        logger.info("createGroup. userId={}, groupId={}, groupUri={}, groupName={}", user.getId(), group.getId(),
                group.getGroupUri(), group.getGroupName());

        return new CreateGroupResponse(group.getId(), group.getGroupUri(), group.getGroupName());
    }

    /**
     * User가 속해있는 group들의 정보를 반환한다.
     * 
     * @return
     */
    public List<GroupInfoResponse> loadGroups() {
        // User 정보 가져오기
        User user = securityUtil.getCurrentUser();

        // User 정보를 통해 속해있는 group 리스트 가져오기
        List<GroupMember> inMembers = groupMemberRepository.findByUser(user);

        ArrayList<GroupInfoResponse> groups = new ArrayList<>();

        for (GroupMember member : inMembers) {
            Group group = member.getGroup();

            UserResponse userResponse = new UserResponse(group.getHostUser().getId(), group.getHostUser().getEmail(),
                    group.getHostUser().getNickname());
            GroupInfoResponse groupInfoResponse = new GroupInfoResponse(group.getId(), group.getGroupUri(),
                    group.getGroupName(), group.getCreatedAt(), userResponse);

            groups.add(groupInfoResponse);
        }

        logger.info("loadGroups. userId={}", user.getId());
        return groups;
    }

    public GroupInfoResponse loadGroupInfo(String groupUri) {
        // User 정보 가져오기
        User user = securityUtil.getCurrentUser();

        // Uri를 통해 그룹을 불러오고 그룹이 있는지 확인한다.
        Optional<Group> _group = groupRepository.findByGroupUri(groupUri);
        if (_group.isEmpty()) {
            String message = "Group not found.";
            logger.warn("loadGroupInfo:GroupException. userId={}, groupUri={}\nmessage={}", user.getId(), groupUri,
                    message);

            throw new GroupException(message);
        }
        Group group = _group.get();

        // 사용자가 멤버로 속해있는지 확인한다.
        Optional<GroupMember> _groupMember = groupMemberRepository.findByUserAndGroup(user, group);
        if (_groupMember.isEmpty()) {
            String message = "There are no users in the member list.";
            logger.warn("loadGroupInfo:GroupMemberException. userId={}, groupId={}\nmessage={}",
                    user.getId(), group.getId(), message);

            throw new GroupMemberException(message);
        }

        // 그룹에 속해있으면 그룹 정보를 반환한다.
        UserResponse userResponse = new UserResponse(group.getHostUser().getId(), group.getHostUser().getEmail(),
                group.getHostUser().getNickname());
        return new GroupInfoResponse(group.getId(), group.getGroupUri(), group.getGroupName(), group.getCreatedAt(),
                userResponse);
    }

    /**
     * 그룹 삭제 / group host만 가능하다.
     * 
     * @param groupUri
     * @param dto
     */
    public void deleteGroup(String groupUri, DeleteGroupRequest dto) {
        // User 정보 가져오기
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();

        Optional<Group> _loadGroup = groupRepository.findByGroupUri(groupUri);
        if (_loadGroup.isEmpty()) {
            String message = "Group not found.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        Group loadGroup = _loadGroup.get();

        /**
         * 요청한 사람이 group의 host인지 확인하고 처리한다.
         * 1. 유저 id와 host id가 동일한지 확인한다.
         * 2. user가 작성한 sign과 group 이름과 동일한지 확인한다.
         */
        if (!userId.equals(loadGroup.getHostUser().getId())) {
            // host id와 요청자의 id가 동일하지 않음
            String message = "The group host and the requester's id are not the same.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }
        if (!dto.getGroupNameSign().equals(loadGroup.getGroupName())) {
            // 사용자가 입력한 그룹 이름과 실제 그룹 이름이 같지 않음.
            String message = "The group name entered by the user and the actual group name are not the same.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        // 그룹에 해당되는 멤버를 모두 삭제
        List<GroupMember> groupMember = groupMemberRepository.findByGroup(loadGroup);
        groupMemberRepository.deleteAll(groupMember);

        // 그룹에 업로드한 포스트들 모두 삭제
        postService.deleteAllPost(loadGroup);

        logger.info("deleteGroup. userId={}, groupId={}", userId, loadGroup.getId());

        groupRepository.delete(loadGroup);
    }

}
