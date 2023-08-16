package io.potatoy.syiary.group.entity;

import io.potatoy.syiary.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup(Group group);

    List<GroupMember> findAllByGroup(Group group);

    Optional<GroupMember> findByUserAndGroup(User user, Group group);

    List<GroupMember> findByUser(User user);
}
