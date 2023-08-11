package io.potatoy.syiary.post;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.potatoy.syiary.post.dto.CreatePostRequest;
import io.potatoy.syiary.post.dto.CreatePostResponse;
import io.potatoy.syiary.post.dto.FixPostRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/groups")
public class PostController {

    private final PostService postService;

    @PostMapping("/{groupUri}/posts") // 포스트 추가
    public ResponseEntity<CreatePostResponse> createPost(
            @PathVariable String groupUri,
            @Validated @ModelAttribute CreatePostRequest request) throws Exception {

        CreatePostResponse response = postService.createPost(groupUri, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{groupUri}/posts/{postId}") // 포스트 수정
    public ResponseEntity<String> fixPost(
            @PathVariable String groupUri,
            @PathVariable Long postId,
            @Validated @ModelAttribute FixPostRequest request) throws Exception {

        postService.fixPost(groupUri, postId, request);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @DeleteMapping("/{groupUri}/posts/{postId}") // 포스트 삭제
    public ResponseEntity<String> deletePost(
            @PathVariable String groupUri,
            @PathVariable Long postId) {

        postService.deletePost(groupUri, postId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
