package com.udangtangtang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

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

    public LocationRequestDto(String locationJsonString) {
        JSONObject jsonObject = new JSONObject(locationJsonString);

        if(jsonObject.isEmpty()) {
            this.placeName = "집";
            this.roadAddressName = "";
            this.xCoordinate = "";
            this.yCoordinate = "";
            this.categoryName = "";
        } else {
            this.placeName = jsonObject.getString("placeName");
            this.roadAddressName = jsonObject.getString("roadAddressName");
            this.xCoordinate = jsonObject.getString("xCoordinate");
            this.yCoordinate = jsonObject.getString(("yCoordinate"));
            this.categoryName = jsonObject.getString("categoryName");
        }
    }

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
