package com.udangtangtang.backend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @OneToOne(orphanRemoval=true)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "article")
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL)
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

    public void addImage(Image image) {
        this.images.add(image);
    }

    public void removeImage(Long id) {
        for(Image image : this.images) {
            if(image.getId().equals(id)) {
                this.images.remove(image);
                break;
            }
        }
    }
}
