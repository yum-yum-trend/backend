package com.udangtangtang.backend.util;

import com.udangtangtang.backend.dto.LocationRequestDto;
import org.springframework.stereotype.Component;

@Component
public class LocationDataPreprocess {

    public void categoryNamePreprocess(LocationRequestDto locationRequestDto) {
        String categoryInfo = locationRequestDto.getCategoryName();

        if(categoryInfo == null) return;

        String[] infoBundle = categoryInfo.split(" > ");
        locationRequestDto.setCategoryName(infoBundle[infoBundle.length-1]);
    }
}
