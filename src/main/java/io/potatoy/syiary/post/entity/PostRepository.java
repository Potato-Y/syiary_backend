package io.potatoy.syiary.post.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.potatoy.syiary.group.entity.Group;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByGroup(Group group);
}
