package io.potatoy.syiary.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import io.potatoy.syiary.group.dto.CreateGroupRequest;
import io.potatoy.syiary.group.dto.DeleteGroupRequest;
import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.entity.GroupRepository;
import io.potatoy.syiary.group.util.TestGroupUtil;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.entity.UserRepository;
import io.potatoy.syiary.user.util.TestUserUtil;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성
@ActiveProfiles("local")
public class GroupControllerTest {

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

    TestUserUtil testUserUtil;
    TestGroupUtil testGroupUtil;

    @BeforeEach
    public void mockMvcSetup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
        this.testUserUtil = new TestUserUtil(bCryptPasswordEncoder, userRepository);
        this.testGroupUtil = new TestGroupUtil(groupRepository, groupMemberRepository);
    }

    @DisplayName("createGroup(): 그룹 만들기 성공")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successCreateGroup() throws Exception {
        // given 그룹 생성에 필요한 객체들 생성
        final String url = "/api/groups";
        final String email = "host@mail.com";
        final String password = "host";
        final String groupName = "test_group";

        testUserUtil.createTestUser(email, password, null);

        CreateGroupRequest request = new CreateGroupRequest();
        request.setGroupName(groupName);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(request);

        // when 로그인 요청
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then 응답 코드가 201인지 확인, 값들이 전부 잘 들어왔는지 확인.
        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.groupUri").isNotEmpty())
                .andExpect(jsonPath("$.groupName").value(groupName));
    }

    @DisplayName("deleteGroup(): 그룹 삭제 성공")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successDeleteGroup() throws Exception {
        // given 그룹 생성에 필요한 객체들 생성
        final String url = "/api/groups";
        final String email = "host@mail.com";
        final String password = "host";
        final String groupName = "test_group";

        // 새로운 유저 생성
        User user = testUserUtil.createTestUser(email, password, null);

        // 새로운 그룹 생성
        Group group = testGroupUtil.createTestGroup(user, groupName);

        final DeleteGroupRequest request = new DeleteGroupRequest(groupName);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(request);

        // when 그룹 삭제 요청
        ResultActions result = mockMvc.perform(delete(url + "/" + group.getGroupUri())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        // then 응답 코드가 204인지 확인한다.
        result
                .andExpect(status().isNoContent());

        // TODO 그룹 포스트가 있는 상태에서도 그룹 삭제가 정상적으로 작동하는지 확인
        assertThat(groupRepository.findAll().size()).isZero();
    }

    @DisplayName("getGroupInfo(): 그룹 정보 가져오기")
    @WithMockUser("host@mail.com")
    @Test
    public void successGetGroupInfo() throws Exception {
        // given 그룹 정보를 받아오기 위한 그룹 생성
        final String url = "/api/groups/{groupUri}";
        final String groupName = "test_group";
        final User hostUser = testUserUtil.createTestUser("host@mail.com", "host", null);

        // 새로운 그룹 생성
        Group group = testGroupUtil.createTestGroup(hostUser, groupName);

        // when 요청
        ResultActions result = mockMvc.perform(get(url.replace("{groupUri}", group.getGroupUri())));

        // then 결과 확인
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.groupUri").value(group.getGroupUri()))
                .andExpect(jsonPath("$.groupName").value(group.getGroupName()))
                .andExpect(jsonPath("$.createAt").isNotEmpty())
                .andExpect(jsonPath("$.hostUser.userId").value(hostUser.getId()))
                .andExpect(jsonPath("$.hostUser.email").value(hostUser.getEmail()))
                .andExpect(jsonPath("$.hostUser.nickname").value(hostUser.getNickname()));

    }

    @DisplayName("getGroupList(): 가입한 그룹의 리스트를 모두 가져온다.")
    @WithMockUser("member@mail.com")
    @Test
    public void successGetGroupList() throws Exception {
        // given 그룹 정보를 불러오기 위한 객체들 생성
        final String url = "/api/groups";
        final String groupName1 = "test_group_1";
        final String groupName2 = "test_group_2";
        final User hostUser = testUserUtil.createTestUser("host@mail.com", "host", null);
        final User memberUser = testUserUtil.createTestUser("member@mail.com", "member", null);

        // 새로운 그룹 생성 및 멤버 추가
        final Group group1 = testGroupUtil.createTestGroup(hostUser, groupName1);
        final Group group2 = testGroupUtil.createTestGroup(hostUser, groupName2);

        // 그룹에 가입
        testGroupUtil.createGroupMember(group1, memberUser);
        testGroupUtil.createGroupMember(group2, memberUser);

        // when 요청
        ResultActions result = mockMvc.perform(get(url));

        // then 결과 확인, 가입한 순으로 표시된다.
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(group1.getId()))
                .andExpect(jsonPath("$[0].groupName").value(group1.getGroupName()))
                .andExpect(jsonPath("$[0].hostUser.userId").value(hostUser.getId()))
                .andExpect(jsonPath("$[0].hostUser.email").value(hostUser.getEmail()))
                .andExpect(jsonPath("$[0].hostUser.nickname").value(hostUser.getNickname()))
                .andExpect(jsonPath("$[1].id").value(group2.getId()))
                .andExpect(jsonPath("$[1].groupName").value(group2.getGroupName()))
                .andExpect(jsonPath("$[1].hostUser.userId").value(hostUser.getId()))
                .andExpect(jsonPath("$[1].hostUser.email").value(hostUser.getEmail()))
                .andExpect(jsonPath("$[1].hostUser.nickname").value(hostUser.getNickname()));
    }
}
