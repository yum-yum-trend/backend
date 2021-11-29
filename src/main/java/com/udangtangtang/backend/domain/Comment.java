package com.udangtangtang.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.udangtangtang.backend.dto.CommentRequestDto;
import com.udangtangtang.backend.repository.ArticleRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(nullable = false)
    private String commentText;

    public Comment(User user, Article article, CommentRequestDto commentRequestDto) {
        this.user = user;
        this.article = article;
        this.commentText = commentRequestDto.getCommentText();
    }
}
