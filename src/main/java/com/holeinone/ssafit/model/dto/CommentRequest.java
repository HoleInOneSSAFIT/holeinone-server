package com.holeinone.ssafit.model.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    private Long postId;
    private String content;

//    private Long parentId;
//    private Long replyToId;
//    private Integer depth;
}
