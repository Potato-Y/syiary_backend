package io.potatoy.syiary.group;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.potatoy.syiary.group.dto.CreateGroupRequest;
import io.potatoy.syiary.group.dto.CreateGroupResponse;
import io.potatoy.syiary.group.dto.DeleteGroupRequest;
import io.potatoy.syiary.group.dto.GroupInfoResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/groups") // 그룹 생성
    public ResponseEntity<CreateGroupResponse> createGroup(@Validated @RequestBody CreateGroupRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(request));
    }

    @GetMapping("/groups") // 그룹 목록 가져오기
    public ResponseEntity<List<GroupInfoResponse>> getGroupList() {
        List<GroupInfoResponse> groups = groupService.loadGroups();

        return ResponseEntity.status(HttpStatus.OK).body(groups);
    }

    @GetMapping("/groups/{groupUri}") // 그룹 정보 가져오기
    public ResponseEntity<GroupInfoResponse> getGroupInfo(@PathVariable String groupUri) {
        GroupInfoResponse group = groupService.loadGroupInfo(groupUri);

        return ResponseEntity.status(HttpStatus.OK).body(group);
    }

    // @GetMapping("/groups/{groupUri}/members") // 그룹에 참여중인 멤버 가져오기
    // public ResponseEntity<String> getGroupMembers(@PathVariable String groupUri)
    // {

    // }

    @DeleteMapping("/groups/{groupUri}") // 그룹 삭제
    public ResponseEntity<String> deleteGroup(@PathVariable String groupUri,
            @Validated @RequestBody DeleteGroupRequest request) {
        groupService.deleteGroup(groupUri, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
