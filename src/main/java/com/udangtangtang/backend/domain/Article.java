package com.udangtangtang.backend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Article extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = true)
    private String location;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "article")
    private List<Hashtag> hashtagList;

    @OneToMany(mappedBy = "article")
    private List<Image> imageList;

    public Article(String text, String location, User user) {
        this.text = text;
        this.location = location;
        this.user = user;
    }
}
