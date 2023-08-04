package io.potatoy.syiary.user;

import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.dto.AddUserRequest;
import io.potatoy.syiary.user.dto.AddUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserApiController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AddUserResponse> signup(@Validated @RequestBody AddUserRequest request) {
        User user = userService.save(request); // 회원가입 메서드 호출

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddUserResponse(user.getId(), user.getEmail()));
    }
}
