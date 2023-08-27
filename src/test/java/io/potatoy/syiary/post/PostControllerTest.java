package io.potatoy.syiary.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import io.potatoy.syiary.post.dto.FixPostRequest;
import io.potatoy.syiary.post.entity.Post;
import io.potatoy.syiary.post.entity.PostFile;
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

    @DisplayName("createPost(): 새로운 포스트 작성하기")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successPostCreate() throws Exception {
        /// given post 업로드에 필요한 객체들 생성 ///
        final String url = "/api/groups/{groupUri}/posts";
        final String groupName = "test_group";

        // host 생성
        final String hostEmail = "host@mail.com";
        final String hostPassword = "host";
        User hostUser = testUserUtil.createTestUser(hostEmail, hostPassword);

        // group 생성 및 멤버 추가
        Group group = testGroupUtil.createTestGroup(hostUser, groupName);

        // post에 사용할 내용 추가
        final String postContent = "test post";

        // post에 전송할 첨부파일 추가하기
        String absolutePath = TestFileHandler.getAbsolutePath();
        MockMultipartFile file1 = new MockMultipartFile("files", "test1_image.jpeg", "image/jpeg", new FileInputStream(
                absolutePath + "src/test/java/io/potatoy/syiary/post/assets/test1_image.jpeg"));
        MockMultipartFile file2 = new MockMultipartFile("files", "test3_image.png", "image/png", new FileInputStream(
                absolutePath + "src/test/java/io/potatoy/syiary/post/assets/test3_image.png"));

        /// when post 요청 ///
        ResultActions result = mockMvc.perform(multipart(url.replace("{groupUri}", group.getGroupUri()))
                .file(file1)
                .file(file2)
                .param("content", postContent));

        /// then 결과 확인 ///
        // 검증에 필요한 데이터 불러오기
        List<Post> posts = postRepository.findAll();
        Post post = posts.get(0);
        List<PostFile> postFiles = postFileRepository.findAllByPost(post);

        result
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.content").value(post.getContent()));

        // 파일이 정상적으로 저장되어 있는지 확인
        for (PostFile postFile : postFiles) {
            String filePath = absolutePath + "files_local/" + group.getId() + "/" + post.getId() + "/"
                    + postFile.getFileName();
            File file = new File(filePath);

            if (!file.exists()) {
                // 파일이 없을 경우 예외 발생
                throw new Error("파일이 없음");
            }
        }
    }

    @DisplayName("fixPost(): 포스트 수정하기")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successFixPost() throws Exception {
        /// given post 수정에 필요한 객체들 생성 ///
        final String url = "/api/groups/{groupUri}/posts/";
        final String groupName = "test_group";

        // host user 생성
        final String hostEmail = "host@mail.com";
        final String hostPassword = "host";
        User hostUser = testUserUtil.createTestUser(hostEmail, hostPassword);

        // group 생성 및 멤버 추가
        Group group = testGroupUtil.createTestGroup(hostUser, groupName);

        // post에 사용할 내용 추가
        final String postContent = "test post";
        final String fixPostContent = "fix post";

        // 새로운 포스트 추가
        Post post = testPostUtil.createPost(group, hostUser, postContent, null);

        // request 객체 생성 및 JSON 직렬화
        FixPostRequest request = new FixPostRequest();
        request.setContent(fixPostContent);

        final String requestBody = objectMapper.writeValueAsString(request);

        /// when post 수정 요청 ///
        ResultActions result = mockMvc.perform(patch(url.replace("{groupUri}", group.getGroupUri()) + post.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(requestBody));

        /// then 응답 코드가 204인지 확인 후 값들을 확인한다. ///
        result
                .andExpect(status().isNoContent());

        post = postRepository.findById(post.getId()).get();
        if (!post.getContent().equals(fixPostContent)) {
            throw new Error("수정에 실패하였습니다.\npost content:" + post.getContent());
        }
    }

    @DisplayName("deletePost():[member] 포스트 삭제하기")
    @WithMockUser(username = "member@mail.com")
    @Test
    public void successRemovePostForMember() throws Exception {
        /// given 그룹 생성에 필요한 객체들 생성 ///
        final String url = "/api/groups/{groupUri}/posts/";
        final String groupName = "test_group";

        // host 와 member user 생성
        final User hostUser = testUserUtil.createTestUser("host@mail.com", "host");
        final User memberUser = testUserUtil.createTestUser("member@mail.com", "member");

        // group 생성 및 멤버 추가
        Group group = testGroupUtil.createTestGroup(hostUser, groupName);
        testGroupUtil.createGroupMember(group, memberUser);

        // 새로운 포스트 추가
        Post post = testPostUtil.createPost(group, memberUser, "test post", null);

        /// when post 삭제 요청 ///
        ResultActions result = mockMvc
                .perform(delete(url.replace("{groupUri}", group.getGroupUri()) + Long.toString(post.getId())));

        /// then 응답 코드가 204인지 확인하고 필드에서도 없어졌는지 확인한다. ///
        result.andExpect(status().isNoContent());

        Optional<Post> _post = postRepository.findById(1L);
        if (!_post.isEmpty()) {
            throw new Error(String.format("post가 있습니다. postId=%d", _post.get().getId()));
        }
    }

    @DisplayName("deletePost():[HOST] 타인 포스트 삭제하기")
    @WithMockUser(username = "host@mail.com")
    @Test
    public void successRemovePostForHostAdmin() throws Exception {
        /// given 그룹 생성에 필요한 객체들 생성 ///
        final String url = "/api/groups/{groupUri}/posts/";
        final String groupName = "test_group";

        // host 와 member user 생성
        final User hostUser = testUserUtil.createTestUser("host@mail.com", "host");
        final User memberUser = testUserUtil.createTestUser("member@mail.com", "member");

        // group 생성 및 멤버 추가
        Group group = testGroupUtil.createTestGroup(hostUser, groupName);
        testGroupUtil.createGroupMember(group, memberUser);

        // 새로운 포스트 추가
        Post post = testPostUtil.createPost(group, memberUser, "test post", null);

        /// when host 계정으로 post 삭제 요청 ///
        ResultActions result = mockMvc
                .perform(delete(url.replace("{groupUri}", group.getGroupUri()) + Long.toString(post.getId())));

        /// then 응답 코드가 204인지 확인하고 필드에서도 없어졌는지 확인한다. ///
        result.andExpect(status().isNoContent());

        Optional<Post> _post = postRepository.findById(1L);
        if (!_post.isEmpty()) {
            throw new Error(String.format("post가 있습니다. postId=%d", _post.get().getId()));
        }
    }

}
