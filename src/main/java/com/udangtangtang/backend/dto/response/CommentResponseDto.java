package com.udangtangtang.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long userId;
    private String username;
    private String userProfileImageUrl;
    private Long commentId;
    private String commentText;
}
