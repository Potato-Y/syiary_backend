package io.potatoy.syiary.post.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import io.potatoy.syiary.enums.FileType;
import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.post.entity.Post;
import io.potatoy.syiary.post.entity.PostFile;
import io.potatoy.syiary.post.exception.PostException;
import io.potatoy.syiary.user.entity.User;

public class TestFileHandler {

    /**
     * 파일을 저장하고 List<PostFile>를 반환한다.
     * 
     * @param user
     * @param group
     * @param post
     * @param files
     * @return
     */
    public List<PostFile> parseFileInfo(User user, Group group, Post post, List<File> files) {
        List<PostFile> postFiles = new ArrayList<>();

        for (File file : files) {
            String[] fileNameSplit = file.getName().split("\\."); // 파일 확장명을 확인하기 위해 분리
            FileType fileType; // 파일 확장명을 저장한다.
            switch (fileNameSplit[fileNameSplit.length - 1]) {
                case "jpeg":
                case "jpg":
                case "png":
                    fileType = FileType.IMAGE;
                    break;

                default:
                    throw new PostException("Invalid file extension.");
            }

            // file 저장
            File saveFile = new File(getPath(group.getId(), post.getId()) + file.getName());
            // 저장할 위치의 디렉토리가 존재하지 않을 경우 생성
            if (!saveFile.exists()) {
                // mkdir()이 아닌 mkdirs() 메소드 사용.
                // 상위 디렉토리가 존재하지 않을 때 그것까지 생성
                saveFile.mkdirs();
            }

            // 파일을 test files 위치에 복사한다.
            try {
                Files.copy(file.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // 파일을 저장하는 중 오류가 발생할 경우 건너뛰기
                System.out.println("ERROR: " + e.getMessage());
                continue;
            }

            // post file entity 생성
            PostFile postFile = PostFile.builder()
                    .post(post)
                    .fileType(fileType)
                    .fileName(file.getName())
                    .build();

            postFiles.add(postFile);
        }

        // 파일 목록을 반환한다.
        return postFiles;
    }

    /**
     * 절대경로를 포함한 파일이 저장될 경로를 반환한다.
     * 
     * @param groupId
     * @param postId
     * @return absolute path/group id/post id/
     */
    private String getPath(Long groupId, Long postId) {
        String path = "files_local/" + Long.toString(groupId) + "/" + Long.toString(postId) + "/";

        return getAbsolutePath() + path;
    }

    /**
     * 절대 경로 반환
     * 
     * @return absolute path/
     */
    public static String getAbsolutePath() {
        // 프로젝트 폴더에 저장하기 위해 절대 경로를 설정 (window의 Tomcat은 temp 파일 이용)
        String absolutePath = new File("").getAbsolutePath() + "/";

        return absolutePath;
    }
}
