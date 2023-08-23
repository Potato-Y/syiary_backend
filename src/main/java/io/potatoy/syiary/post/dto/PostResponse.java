package io.potatoy.syiary.post.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.potatoy.syiary.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostResponse {

    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserResponse createUser;
    private String content;
    private List<byte[]> files;
}
