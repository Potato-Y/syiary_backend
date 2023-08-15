package io.potatoy.syiary.post.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {
    List<PostFile> findAllByPost(Post post);
}
