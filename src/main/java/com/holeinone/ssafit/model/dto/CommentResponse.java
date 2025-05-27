package com.holeinone.ssafit.model.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    // comment
    private Long commentId;
    private Date createdAt;
    private Date updatedAt;
    private String content;

    // user
    private Long userId;
    private String profileImage;
    private String username;
    private String nickname;

    // post
    private Long postId;
    private String postTitle;
}
