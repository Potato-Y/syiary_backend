package io.potatoy.syiary.post;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.group.entity.GroupRepository;
import io.potatoy.syiary.group.exception.GroupException;
import io.potatoy.syiary.group.util.GroupUtil;
import io.potatoy.syiary.post.dto.CreatePostRequest;
import io.potatoy.syiary.post.dto.CreatePostResponse;
import io.potatoy.syiary.post.dto.FixPostRequest;
import io.potatoy.syiary.post.dto.GetPostListRequest;
import io.potatoy.syiary.post.dto.PostResponse;
import io.potatoy.syiary.post.entity.Post;
import io.potatoy.syiary.post.entity.PostFile;
import io.potatoy.syiary.post.entity.PostFileRepository;
import io.potatoy.syiary.post.entity.PostRepository;
import io.potatoy.syiary.post.exception.PostException;
import io.potatoy.syiary.post.handler.FileHandler;
import io.potatoy.syiary.post.util.PostUtil;
import io.potatoy.syiary.security.util.SecurityUtil;
import io.potatoy.syiary.user.dto.UserResponse;
import io.potatoy.syiary.user.entity.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PostService {

    private final Logger logger = LogManager.getLogger(PostService.class);
    private final GroupRepository groupRepository;
    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final FileHandler fileHandler;
    private final SecurityUtil securityUtil;
    private final GroupUtil groupUtil;
    private final PostUtil postUtil;

    /**
     * 사용자가 그룹의 최신 목록을 불러온다.
     * 
     * @param groupUri
     * @param dto
     * @return
     */
    public List<PostResponse> getList(String groupUri, GetPostListRequest dto) {
        User user = securityUtil.getCurrentUser();
        int page = dto.getPage();

        // 그룹 정보를 불러온다.
        Optional<Group> _group = groupRepository.findByGroupUri(groupUri);
        if (_group.isEmpty()) {
            String message = "Group not found.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        Group group = _group.get();

        groupUtil.checkGroupUser(user, group); // 사용자가 그룹에 포함되어 있는지 확인

        // 요청된 page에 맞추어 post를 불러온다.
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Post> postPage = postRepository.findAllByGroup(group, pageable);
        List<Post> posts = postPage.getContent();

        List<PostResponse> postResponses = new ArrayList<>();
        for (Post post : posts) {
            // 작성자 정보 가져오기
            UserResponse createUser = new UserResponse(post.getUser().getId(), post.getUser().getEmail(),
                    post.getGroup().getGroupUri());

            // file의 데이터 리스트화 시키기
            List<byte[]> files = new ArrayList<>();
            for (PostFile postFile : post.getFiles()) {
                try {
                    byte[] file = fileHandler.getFile(group.getId(), post.getId(), postFile.getFileName());
                    if (file != null) {
                        files.add(file);
                    }
                } catch (Exception e) {
                    logger.warn("PostService:getList. message={}", e.getMessage());
                }
            }

            postResponses.add(new PostResponse(post.getId(), post.getCreatedAt(), post.getUpdatedAt(), createUser,
                    post.getContent(), files));
        }

        logger.info("getList. userId={}, groupId={}", user.getId(), group.getId());
        return postResponses;
    }

    /**
     * 새로운 포스터 작성(추가)
     * 
     * @param postUri
     * @param dto
     * @return
     * @throws Exception
     */
    public CreatePostResponse createPost(String groupUri, CreatePostRequest dto) throws Exception {
        User user = securityUtil.getCurrentUser();

        // 그룹 정보를 불러온다.
        Optional<Group> _group = groupRepository.findByGroupUri(groupUri);
        if (_group.isEmpty()) {
            String message = "Group not found.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        Group group = _group.get();

        groupUtil.checkGroupUser(user, group); // 사용자가 그룹에 포함되어 있는지 확인

        // createPost 시작점 로그
        logger.info("createPost. userId={}, groupId={}", user.getId(), group.getId());

        // post file를 저장하기 위해 먼저 post를 저장
        Post post = Post.builder()
                .group(group)
                .user(user)
                .content(dto.getContent())
                .build();
        postRepository.save(post);

        // PostFile 객체를 받아오고, db에 관련 정보를 저장
        List<PostFile> postFiles = fileHandler.parseFileInfo(user, group, post, dto.getFiles());
        postFileRepository.saveAll(postFiles);

        post.updateFile(postFiles); // post entity에 post file entity 추가
        postRepository.save(post); // 변경사항 저장

        return new CreatePostResponse(post.getId(), post.getContent()); // 응답 반환
    }

    /**
     * 기존 포스터 내용 수정
     * 
     * @param groupUri
     * @param postId
     * @param dto
     */
    public void fixPost(String groupUri, Long postId, FixPostRequest dto) {
        User user = securityUtil.getCurrentUser();

        // 그룹 정보를 불러온다.
        Optional<Group> _group = groupRepository.findByGroupUri(groupUri);
        if (_group.isEmpty()) {
            String message = "Group not found.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        Group group = _group.get();

        Optional<Post> post = postRepository.findById(postId);

        // 포스트가 없는지 확인한다.
        if (post.isEmpty()) {
            String message = "There are no posts.";

            logger.warn("fixPost:PostException. userId={}, groupId={}, postId={}\nmessage={}", user.getId(),
                    group.getId(), postId, message);
            throw new PostException(message);
        }

        // 수정할 권한이 있는 유저인지 확인 후 없을 경우 예외 발생
        Boolean authority = postUtil.checkFixAuthority(user, group, post.get()); // 게시글 작성 권한 확인
        if (authority == false) {
            String message = "Requester is not the author of the post.";

            logger.warn("fixPost:PostException. userId={}, groupId={}, postId={}\nmessage={}", user.getId(),
                    group.getId(), postId, message);
            throw new PostException(groupUri);
        }

        // 내용을 수정하여 저장
        postRepository.save(post.get().updateContent(dto.getContent()));
    }

    public void deletePost(String groupUri, Long postId) {
        User user = securityUtil.getCurrentUser();

        // 그룹 정보를 불러온다.
        Optional<Group> _group = groupRepository.findByGroupUri(groupUri);
        if (_group.isEmpty()) {
            String message = "Group not found.";
            logger.warn("deleteGroup:GroupException. message={}", message);

            throw new GroupException(message);
        }

        Group group = _group.get();

        Optional<Post> post = postRepository.findById(postId);

        // 포스트가 없는지 확인한다.
        if (post.isEmpty()) {
            String message = "There are no posts.";

            logger.warn("fixPost:PostException. userId={}, groupId={}, postId={}\nmessage={}", user.getId(),
                    group.getId(), postId, message);
            throw new PostException(message);
        }

        // 삭제 권한이 있는 유저인지 확인한다.
        Boolean authority = postUtil.checkDeleteAuthority(user, group, post.get());
        if (authority == false) {
            String message = "You do not have permission to delete posts.";

            logger.warn("deletePost:PostException. userId={}, groupId={}, postId={}\nmessage={}", user.getId(),
                    group.getId(), postId, message);
            throw new PostException(groupUri);
        }

        postRepository.delete(post.get());
    }

    /**
     * 그룹에 해당하는 포스터를 모두 지운다.
     * 
     * @param group
     */
    public void deleteAllPost(Group group) {
        List<Post> posts = postRepository.findAllByGroup(group);

        // 포스트를 지우기 전에 파일을 먼저 삭제한다.
        for (Post post : posts) {
            List<PostFile> postFiles = postFileRepository.findAllByPost(post);

            for (PostFile postFile : postFiles) {
                fileHandler.deleteFile(postFile);
            }
        }

    }
}
