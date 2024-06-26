package io.potatoy.syiary.user.entity;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMember;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class User implements UserDetails { // UserDetails를 상속받아 인증 객체로 사용

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", updatable = false)
  private Long id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @OneToMany(mappedBy = "hostUser", cascade = CascadeType.REMOVE)
  private List<Group> groups = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
  private List<GroupMember> groupMembers = new ArrayList<>();

  @Builder
  public User(String email, String password, String nickname) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("user"));
  }

  @Override // 사용자 id 반환
  public String getUsername() {
    return email;
  }

  @Override // 사용자 패스워드 반환
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isAccountNonExpired() {
    // 만료되었는지 확인하는 로직
    return true; // true -> 만료되지 않음
  }

  @Override
  public boolean isAccountNonLocked() {
    // 계정이 잠금되었는지 확인하는 로직
    return true; // true -> 잠금되지 않음
  }

  @Override
  public boolean isCredentialsNonExpired() {
    // 패스워드가 만료되었는지 확인하는 로직
    return true; // true -> 만료되지 않음
  }

  // 계정 사용 가능 여부 반환

  @Override
  public boolean isEnabled() {
    // 계정이 사용 가능한지 확인하는 로직
    return true; // true -> 사용 가능
  }
}
