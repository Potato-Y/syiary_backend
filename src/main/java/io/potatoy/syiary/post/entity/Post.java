package io.potatoy.syiary.post.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @CreatedDate // 엔티티가 생성될 때 생성 시간 저장
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // update 시간은 특성상 별도로 관리
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "group_id", nullable = false, updatable = false)
    private Group group; // post를 올릴 group

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user; // post를 올리는 user

    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostFile> files = new ArrayList<>();

    @Builder
    public Post(Group group, User user, String content, List<PostFile> postFiles) {
        this.group = group;
        this.user = user;
        this.content = content;
        this.files = postFiles;
    }

    public Post updateFile(List<PostFile> postFiles) {
        this.files = postFiles;

        return this;
    }

    public Post updateContent(String content) {
        this.content = content;

        // 변경 시간 추가
        this.updatedAt = LocalDateTime.now();

        return this;
    }
}
