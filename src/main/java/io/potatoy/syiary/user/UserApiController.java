package io.potatoy.syiary.user;

import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.security.util.SecurityUtil;
import io.potatoy.syiary.user.dto.AddUserRequest;
import io.potatoy.syiary.user.dto.AddUserResponse;
import io.potatoy.syiary.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {
    private final Logger logger = LogManager.getLogger(UserApiController.class);
    private final UserService userService;
    private final SecurityUtil securityUtil;

    @PostMapping("/signup")
    public ResponseEntity<AddUserResponse> signup(@Validated @RequestBody AddUserRequest request) {
        User user = userService.save(request); // 회원가입 메서드 호출

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddUserResponse(user.getId(), user.getEmail(), user.getNickname()));
    }

    @GetMapping("/user")
    public ResponseEntity<UserResponse> user() {
        User user = securityUtil.getCurrentUser();

        logger.info("user. userId={}, userEmail={}, userNickname={}", user.getId(), user.getEmail(),
                user.getNickname());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new UserResponse(user.getId(), user.getEmail(), user.getNickname()));
    }
}
