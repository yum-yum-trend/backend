package com.udangtangtang.backend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
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

    @OneToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "article")
    private List<Hashtag> hashtagList;

    @OneToMany(mappedBy = "article")
    private List<Image> imageList;

    @OneToMany(mappedBy = "article")
    private List<Comment> comments = new ArrayList<>();

    public Article(String text, Location location, User user) {
        this.text = text;
        this.location = location;
        this.user = user;
    }
}
