package io.potatoy.syiary.group.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupUri(String groupUri);
}
