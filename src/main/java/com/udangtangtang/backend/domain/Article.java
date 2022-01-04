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

    @Column
    private String text;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval=true)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval=true)
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval=true)
    private List<Comment> comments = new ArrayList<>();

    public Article(String text, Location location, User user) {
        this.text = text;
        this.location = location;
        this.user = user;
    }

    public void update(String text, Location location, List<Tag> tags, List<Image> images) {
        this.text = text;
        this.location = location;
        this.tags.clear();
        this.tags.addAll(tags);
        this.images.addAll(images);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }
}
