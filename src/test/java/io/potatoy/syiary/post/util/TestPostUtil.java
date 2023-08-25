package io.potatoy.syiary.post.util;

import java.io.File;
import java.util.List;

import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.post.entity.Post;
import io.potatoy.syiary.post.entity.PostFile;
import io.potatoy.syiary.post.entity.PostFileRepository;
import io.potatoy.syiary.post.entity.PostRepository;
import io.potatoy.syiary.post.handler.TestFileHandler;
import io.potatoy.syiary.user.entity.User;

public class TestPostUtil {

    private PostRepository postRepository;
    private PostFileRepository postFileRepository;

    TestFileHandler testFileHandler;

    public TestPostUtil(PostRepository postRepository, PostFileRepository postFileRepository) {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
        this.testFileHandler = new TestFileHandler();
    }

    /**
     * 포스트를 생성한다. file이 포함된 경우 파일도 복사하여 함께 저장한다.
     * 
     * @param group
     * @param createUser
     * @param content
     * @param files
     * @return
     */
    public Post createPost(Group group, User createUser, String content, List<File> files) {

        Post post = Post.builder()
                .group(group)
                .user(createUser)
                .content(content)
                .build();

        postRepository.save(post);

        // 파일이 비어있지 않다면 파일을 저장한다.
        if (files != null) {
            List<PostFile> postFiles = testFileHandler.parseFileInfo(createUser, group, post, files);
            postFileRepository.saveAll(postFiles);
            post.updateFile(postFiles);
        }

        return postRepository.save(post);
    }
}
