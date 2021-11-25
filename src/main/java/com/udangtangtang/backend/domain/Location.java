package com.udangtangtang.backend.domain;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Location extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false)
    private String roadAddressName;

    @Column(nullable = false)
    private String xCoordinate;

    @Column(nullable = false)
    private String yCoordinate;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    private Long userId;

    public Location(String placeName,
                    String roadAddressName,
                    String xCoordinate,
                    String yCoordinate,
                    String categoryName,
                    Long userId
    ) {
        this.placeName = placeName;
        this.roadAddressName = roadAddressName;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.categoryName = categoryName;
        this.userId = userId;
    }

}
