package io.potatoy.syiary.token;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.potatoy.syiary.config.jwt.JwtProperties;
import io.potatoy.syiary.config.jwt.TokenProvider;
import io.potatoy.syiary.token.entity.RefreshToken;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.token.dto.AuthenticateRequest;
import io.potatoy.syiary.token.dto.TokenRequest;
import io.potatoy.syiary.token.entity.RefreshTokenRepository;
import io.potatoy.syiary.user.entity.UserRepository;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성
@ActiveProfiles("local")
public class TokenApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper; // JSON 직렬화, 역직렬화를 위한 클래스
    @Autowired
    private WebApplicationContext context;
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    TokenProvider tokenProvider;

    @BeforeEach
    public void mockMvcSetup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @DisplayName("authenticate(): 로그인 성공")
    @Test
    public void successAuthentication() throws Exception {
        // given 로그인에 필요한 객체들 생성
        final String url = "/api/authenticate";
        final String email = "user@mail.com";
        final String password = "test";

        userRepository.save(User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .build());

        AuthenticateRequest request = new AuthenticateRequest();
        request.setEmail(email);
        request.setPassword(password);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(request);

        // when 로그인에 요청
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then 응답 코드가 200인지 확인, 값들이 전부 잘 들어왔는지 확인.
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    // @DisplayName("authenticate(): 로그인 실패")
    // @Test
    // public void failAuthentication() throws Exception {
    // // given 로그인에 필요한 객체들 생성
    // final String url = "/api/authenticate";
    // final String email = "user@mail.com";
    // final String password = "test";

    // userRepository.save(User.builder()
    // .email(email)
    // .password(bCryptPasswordEncoder.encode(password))
    // .build());

    // AuthenticateRequest request = new AuthenticateRequest();
    // request.setEmail(email);
    // request.setPassword(password + "_"); // 틀린 비밀번호 넣기

    // // 객체 JSON으로 직렬화
    // final String requestBody = objectMapper.writeValueAsString(request);

    // // when 로그인에 요청
    // ResultActions result = this.mockMvc.perform(post(url)
    // .contentType(MediaType.APPLICATION_JSON_VALUE)
    // .content(requestBody));

    // result.andExpect(status().isForbidden());
    // }

    @DisplayName("token(): refresh token을 통해 새로운 access token 발급")
    @Test
    public void token() throws Exception {
        // given 토큰을 통한 로그인에 필요한 객체들 생성
        final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
        final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);

        final String url = "/api/token";
        final String email = "user@mail.com";
        final String password = "test";

        User testUser = userRepository.save(User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .build());

        // token set 생성
        final String accessToken = tokenProvider.generateToken(testUser, ACCESS_TOKEN_DURATION);
        final String refreshToken = tokenProvider.generateToken(testUser, REFRESH_TOKEN_DURATION);

        refreshTokenRepository.save(new RefreshToken(testUser.getId(), refreshToken)); // refresh token 저장

        final TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setAccessToken(accessToken);
        tokenRequest.setRefreshToken(refreshToken);

        // 객체 JSON 직렬화
        final String requestBody = objectMapper.writeValueAsString(tokenRequest);

        // when 새로운 access token 요청을 보낸다.
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then 응답코드 확인
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }
}
