package com.udangtangtang.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class Hashtag extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String tag;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(nullable = false)
    private Long userId;

    public Hashtag(String tag, Article article, Long userId) {
        this.tag = tag;
        this.article = article;
        this.userId = userId;
    }
}
