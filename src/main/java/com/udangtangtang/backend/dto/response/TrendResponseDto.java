package com.udangtangtang.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrendResponseDto {
    // location info
    private String location;
    private int numberOfOrdersByRegion;
    private String color;
    // chart info
    private String categoryName;
    private int numberOfOrderByCategoryName;

    public TrendResponseDto(String location, int numberOfOrdersByRegion, String color) {
        this.location = location;
        this.numberOfOrdersByRegion = numberOfOrdersByRegion;
        this.color = color;
    }

    public TrendResponseDto(String categoryName, int numberOfOrderByCategoryName) {
        this.categoryName = categoryName;
        this.numberOfOrderByCategoryName = numberOfOrderByCategoryName;
    }

}
