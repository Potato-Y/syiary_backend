package io.potatoy.syiary.post.entity;

import io.potatoy.syiary.enums.FileType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class PostFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private FileType fileType;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Builder
    public PostFile(FileType fileType, Post post, String fileName) {
        this.fileType = fileType;
        this.post = post;
        this.fileName = fileName;
    }
}
