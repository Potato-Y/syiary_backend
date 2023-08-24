package io.potatoy.syiary.post.handler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import io.potatoy.syiary.enums.FileType;
import io.potatoy.syiary.group.entity.Group;
import io.potatoy.syiary.post.entity.Post;
import io.potatoy.syiary.post.entity.PostFile;
import io.potatoy.syiary.user.entity.User;
import io.potatoy.syiary.util.UriMaker;

@Component
public class FileHandler {

    private final Logger logger = LogManager.getLogger(FileHandler.class);

    public List<PostFile> parseFileInfo(
            User user,
            Group group,
            Post post,
            List<MultipartFile> multipartFiles) throws Exception {

        UriMaker uriMaker = new UriMaker();

        // 반환할 파일 리스트
        List<PostFile> postFileList = new ArrayList<>();

        // 파일이 비어있으면 빈 리스트 반환
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            logger.info("not file");
            return postFileList;
        }

        // 프로젝트 폴더에 저장하기 위해 절대 경로를 설정 (window의 Tomcat은 temp 파일 이용)
        String absolutePath = new File("").getAbsolutePath() + "/";

        // 경로를 지정, 해당 경로에 저장
        String path = "files/" + Long.toString(group.getId()) + "/" + Long.toString(post.getId());
        File file = new File(absolutePath + path);
        // 저장할 위치의 디렉토리가 존재하지 않을 경우 생성

        if (!file.exists()) {
            // mkdir()이 아닌 mkdirs() 메소드 사용.
            // 상위 디렉토리가 존재하지 않을 때 그것까지 생성
            file.mkdirs();
        }

        // 파일 컨트롤
        for (MultipartFile multipartFile : multipartFiles) {
            // 파일이 비어있는 상태가 아니어야 오류가 나지 않는다.
            if (!multipartFile.isEmpty()) {
                // 우선적으로 jpeg, png 파일만 허용 및 처리
                String contentType = multipartFile.getContentType();
                String originalFileExtension;
                FileType fileType;

                // 확장자명이 없으면 잘 못된 파일
                if (ObjectUtils.isEmpty(contentType) || contentType == null) {
                    break;
                } else {
                    if (contentType.contains("image/jpeg")) {
                        originalFileExtension = ".jpg";
                        fileType = FileType.IMAGE;
                    } else if (contentType.contains("image/png")) {
                        originalFileExtension = ".png";
                        fileType = FileType.IMAGE;
                    }
                    // 다른 확장명이면 아무 일을 하지 않는다.
                    else {
                        break;
                    }
                }

                // 파일 이름을 임의로 변경하여 저장
                // 파일 이름 양식: {date}.{랜덤 문자}
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                String current_date = simpleDateFormat.format(new Date());

                String newFileName = current_date + "." + uriMaker.createName() + originalFileExtension;

                // 생성 후 리스트에 추가
                PostFile postFile = PostFile.builder()
                        .post(post)
                        .fileType(fileType)
                        .fileName(newFileName)
                        .build();

                postFileList.add(postFile);

                // 저장된 파일로 변경하여 이를 보여주기 위함
                file = new File(absolutePath + path + "/" + newFileName);
                multipartFile.transferTo(file);
            }

        }
        return postFileList;

    }

    public void deleteFile(PostFile postFile) {
        // 프로젝트 폴더에 저장하기 위해 절대 경로를 설정 (window의 Tomcat은 temp 파일 이용)
        String absolutePath = new File("").getAbsolutePath() + "/";
        final String filePath = absolutePath + "files/" + postFile.getPost().getGroup().getId() + "/"
                + postFile.getPost().getId() + "/" + postFile.getFileName();

        File file = new File(filePath);
        file.delete();
    }

    /**
     * 이미지 파일을 byte로 반환
     * 
     * @param groupId
     * @param postId
     * @param fileName
     * @return
     */
    public byte[] getFile(Long groupId, Long postId, String fileName) {
        // 프로젝트 폴더에 저장하기 위해 절대 경로를 설정 (window의 Tomcat은 temp 파일 이용)
        String absolutePath = new File("").getAbsolutePath() + "/";

        // 경로를 지정, 해당 경로에 저장
        String path = "files/" + Long.toString(groupId) + "/" + Long.toString(postId) + "/" + fileName;
        File file = new File(absolutePath + path); // 해당 파일을 불러온다.

        byte[] fileByte = null;

        try {
            fileByte = FileCopyUtils.copyToByteArray(file);
        } catch (IOException e) {
            logger.warn("getFile. Not found. message={}", e.getMessage());
        }

        return fileByte;
    }
}
