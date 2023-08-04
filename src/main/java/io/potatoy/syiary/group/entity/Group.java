package io.potatoy.syiary.group.entity;

import io.potatoy.syiary.enums.State;
import io.potatoy.syiary.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "groups")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id; // 자동 생성 고유 ID

    @Column(name = "group_uri", nullable = false, unique = true, updatable = false)
    private String groupUri; // 외부에 사용될 고유 id

    @Column(name = "group_name", nullable = false)
    private String groupName; // 그룹 이름

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User hostUser; // 방장 user id

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Builder
    public Group(String groupUri, String groupName, User hostUser, State state) {
        this.groupUri = groupUri;
        this.groupName = groupName;
        this.hostUser = hostUser;
        this.state = state;
    }

    /**
     * group state 변경
     * 
     * @param state
     * @return
     */
    public Group updateState(State state) {
        this.state = state;

        return this;
    }
}
