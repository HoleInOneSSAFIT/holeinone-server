package com.holeinone.ssafit.model.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long commentId;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isDeleted; // soft-delete 처리
    private Integer depth; // default는 0(최상위 댓글), depth는 최대 1
    private Long userId;
    private Long postId;
    private Long parentId; // depth 계산용, 일반 댓글(null), 대댓글(상위ID)
    private Long replyToId; // 실제 태그 할 대상 명시 용도
}
