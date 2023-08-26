package io.potatoy.syiary.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupMemberRepository;
import io.potatoy.syiary.group.entity.GroupRepository;
import io.potatoy.syiary.group.util.TestGroupUtil;
import io.potatoy.syiary.post.entity.Post;
import io.potatoy.syiary.post.entity.PostFileRepository;
import io.potatoy.syiary.post.entity.PostRepository;
import io.potatoy.syiary.post.handler.TestFileHandler;
import io.potatoy.syiary.post.util.TestPostUtil;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.user.entity.UserRepository;
import io.potatoy.syiary.user.util.TestUserUtil;

@SpringBootTest // 테스트용 애플리케이션 컨텍스트
@AutoConfigureMockMvc // MockMvc 생성
@ActiveProfiles("local")
public class PostControllerTest {

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
    @Autowired
    PostRepository postRepository;
    @Autowired
    PostFileRepository postFileRepository;

    TestUserUtil testUserUtil;
    TestGroupUtil testGroupUtil;
    TestPostUtil testPostUtil;

    @BeforeEach
    public void mockMvcSetup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        postFileRepository.deleteAll();
        postRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupRepository.deleteAll();
        userRepository.deleteAll();
        this.testUserUtil = new TestUserUtil(bCryptPasswordEncoder, userRepository);
        this.testGroupUtil = new TestGroupUtil(groupRepository, groupMemberRepository);
        this.testPostUtil = new TestPostUtil(postRepository, postFileRepository);
    }

    @DisplayName("getPostList(): 목록 불러오기")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successPostList() throws Exception {
        /// given post 생성에 필요한 객체들 생성 ///
        final String url = "/api/groups/{groupUri}/posts";
        final String groupName = "test_group";

        // host 생성
        final String hostEmail = "host@mail.com";
        final String hostPassword = "host";
        User hostUser = testUserUtil.createTestUser(hostEmail, hostPassword);

        // member 생성
        final String memberEmail = "member@mail.com";
        final String memberPassword = "member";
        User memberUser = testUserUtil.createTestUser(memberEmail, memberPassword);

        // group 생성 및 멤버 추가
        Group group = testGroupUtil.createTestGroup(hostUser, groupName);
        testGroupUtil.createGroupMember(group, memberUser);

        // post 생성
        final String postContent = "test post";
        final List<File> files = new ArrayList<>();
        String absolutePath = TestFileHandler.getAbsolutePath();
        files.add(new File(absolutePath + "src/test/java/io/potatoy/syiary/post/assets/test1_image.jpeg"));
        files.add(new File(absolutePath + "src/test/java/io/potatoy/syiary/post/assets/test3_image.png"));

        Post post1 = testPostUtil.createPost(group, hostUser, postContent, files);
        Post post2 = testPostUtil.createPost(group, memberUser, postContent, files);

        /// when post 목록 요청 ///
        ResultActions result = mockMvc.perform(get(url.replace("{groupUri}",
                group.getGroupUri())));

        /// then 결과 확인
        result
                .andExpect(status().isOk())
                // 포스트는 등록 역순이다.
                .andExpect(jsonPath("$[0].postId").value(post2.getId()))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[0].updatedAt").isEmpty())
                .andExpect(jsonPath("$[0].createUser.userId").value(memberUser.getId()))
                .andExpect(jsonPath("$[0].createUser.email").value(memberUser.getEmail()))
                .andExpect(jsonPath("$[0].createUser.nickname").value(memberUser.getNickname()))
                .andExpect(jsonPath("$[0].createUser.userId").value(memberUser.getId()))
                .andExpect(jsonPath("$[0].content").value(post2.getContent()))
                .andExpect(jsonPath("$[0].files[0]").isNotEmpty())
                .andExpect(jsonPath("$[0].files[1]").isNotEmpty())

                .andExpect(jsonPath("$[1].postId").value(post1.getId()))
                .andExpect(jsonPath("$[1].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].updatedAt").isEmpty())
                .andExpect(jsonPath("$[1].createUser.userId").value(hostUser.getId()))
                .andExpect(jsonPath("$[1].createUser.email").value(hostUser.getEmail()))
                .andExpect(jsonPath("$[1].createUser.nickname").value(hostUser.getNickname()))
                .andExpect(jsonPath("$[1].createUser.userId").value(hostUser.getId()))
                .andExpect(jsonPath("$[1].content").value(post1.getContent()))
                .andExpect(jsonPath("$[1].files[0]").isNotEmpty())
                .andExpect(jsonPath("$[1].files[1]").isNotEmpty());
    }
}
