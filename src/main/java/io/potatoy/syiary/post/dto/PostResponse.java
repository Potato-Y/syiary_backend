package io.potatoy.syiary.post.dto;

import io.potatoy.syiary.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
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
