package io.potatoy.syiary.group;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.potatoy.syiary.group.dto.SecessionGroupRequest;
import io.potatoy.syiary.group.dto.SignupGroupRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/groups")
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    @PostMapping("/{groupUri}/members") // 그룹에 유저 추가
    public ResponseEntity<String> signupGroup(@PathVariable String groupUri,
            @Validated @RequestBody SignupGroupRequest request) {
        groupMemberService.signupGroup(groupUri, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @DeleteMapping("/{groupUri}/members") // 그룹에서 멤버 탈퇴
    public ResponseEntity<String> secessionGroup(@PathVariable String groupUri,
            @Validated @RequestBody SecessionGroupRequest request) {
        groupMemberService.secessionGroup(groupUri, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
