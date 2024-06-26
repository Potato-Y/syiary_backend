package io.potatoy.syiary.post.entity;

import io.potatoy.syiary.group.entity.Group;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

  List<Post> findAllByGroup(Group group);

  Page<Post> findAllByGroup(Group group, Pageable pageable);
}
