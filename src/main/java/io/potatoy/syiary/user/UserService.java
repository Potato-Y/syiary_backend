package io.potatoy.syiary.user;

import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.dto.AddUserRequest;
import io.potatoy.syiary.user.entity.UserRepository;
import lombok.RequiredArgsConstructor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final Logger logger = LogManager.getLogger(UserService.class);
    private final UserRepository userRepository;

    public User save(AddUserRequest dto) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user = userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(encoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .build());

        logger.info("save. userId={}, userEmail={}, userNickname={}", user.getId(), user.getEmail(),
                user.getNickname());

        return user;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
    }
}
