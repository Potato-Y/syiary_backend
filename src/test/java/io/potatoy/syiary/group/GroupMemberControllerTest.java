package io.potatoy.syiary.group;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.potatoy.syiary.group.dto.SecessionGroupRequest;
import io.potatoy.syiary.group.dto.SignupGroupRequest;
import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.entity.GroupRepository;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.entity.UserRepository;
import io.potatoy.syiary.util.TestUtil;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성
@ActiveProfiles("local")
public class GroupMemberControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper; // JSON 직렬화, 역직렬화를 위한 클래스
    @Autowired
    private WebApplicationContext context;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;

    TestUtil testUtil;

    @BeforeEach
    public void mockMvcSetup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
        testUtil = new TestUtil(bCryptPasswordEncoder, userRepository, groupRepository, groupMemberRepository);
    }

    @DisplayName("signupGroup(): 그룹의 멤버를 추가한다.")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successSignupGroup() throws Exception {
        // given 그룹 생성에 필요한 객체들 생성
        final String url = "/api/groups/{groupUri}/members";
        final String groupName = "test_group";
        final User hostUser = testUtil.createTestUser("host@mail.com", "host");
        final User guestUser = testUtil.createTestUser("guest@mail.com", "guest");

        // 새로운 그룹 생성
        Group group = testUtil.createTestGroup(hostUser, groupName);

        // request 객체 생성 및, JSON 직렬화
        final SignupGroupRequest request = new SignupGroupRequest(guestUser.getEmail());
        final String requestBody = objectMapper.writeValueAsString(request);

        // when 게스트 가입 요청
        ResultActions result = mockMvc.perform(post(url.replace("{groupUri}", group.getGroupUri()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then 응답 코드가 204인지 확인한다.
        result
                .andExpect(status().isNoContent());
    }

    @DisplayName("secessionGroup(): 그룹의 멤버를 제거한다.")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successSecessionGroup() throws Exception {
        // given 멤버 제거를 위한 그룹과 멤버들 생성
        final String url = "/api/groups/{groupUri}/members";
        final String groupName = "test_group";
        final User hostUser = testUtil.createTestUser("host@mail.com", "host");
        final User memberUser = testUtil.createTestUser("member@mail.com", "member");

        // 새로운 그룹 생성 및 멤버 추가
        Group group = testUtil.createTestGroup(hostUser, groupName);
        testUtil.createGroupMember(memberUser, group);

        // request 객체 생성 및, JSON 직렬화
        final SecessionGroupRequest request = new SecessionGroupRequest(memberUser.getEmail());
        final String requestBody = objectMapper.writeValueAsString(request);

        // when 멤버 탈퇴 요청
        ResultActions result = mockMvc.perform(delete(url.replace("{groupUri}", group.getGroupUri()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then 응답 코드가 204인지 확인한다.
        result
                .andExpect(status().isNoContent());
    }

    @DisplayName("secessionGroup(): 그룹에서 탈퇴한다.")
    @WithMockUser("member@mail.com")
    @Test
    public void successMemberSecessionGroup() throws Exception {
        // given 멤버 제거를 위한 그룹과 멤버들 생성
        final String url = "/api/groups/{groupUri}/members";
        final String groupName = "test_group";
        final User hostUser = testUtil.createTestUser("host@mail.com", "host");
        final User memberUser = testUtil.createTestUser("member@mail.com", "member");

        // 새로운 그룹 생성 및 멤버 추가
        Group group = testUtil.createTestGroup(hostUser, groupName);
        testUtil.createGroupMember(memberUser, group);

        // request 객체 생성 및, JSON 직렬화
        final SecessionGroupRequest request = new SecessionGroupRequest("");
        final String requestBody = objectMapper.writeValueAsString(request);

        // when 멤버 탈퇴 요청
        ResultActions result = mockMvc.perform(delete(url.replace("{groupUri}", group.getGroupUri()))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then 응답 코드가 204인지 확인한다.
        result
                .andExpect(status().isNoContent());
    }
}
