package io.potatoy.syiary.user.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.entity.UserRepository;

public class TestUserUtil {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;

    public TestUserUtil(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
    }

    /**
     * 새로운 테스트 유저를 생성하고 필드에 저장한다.
     * 
     * @param email
     * @param password
     * @return
     */
    public User createTestUser(String email, String password, String nickname) {
        User user = userRepository.save(User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .nickname(nickname == null ? email : nickname)
                .build());

        return user;
    }

}
