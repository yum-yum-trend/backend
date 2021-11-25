package com.udangtangtang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LocationRequestDto {
    private String placeName;
    private String roadAddressName;
    private String xCoordinate;
    private String yCoordinate;
    private String categoryName;

    @Override
    public String toString() {
        return "LocationRequestDto{" +
                "placeName='" + placeName + '\'' +
                ", roadAddressName='" + roadAddressName + '\'' +
                ", xCoordinate='" + xCoordinate + '\'' +
                ", yCoordinate='" + yCoordinate + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
