package com.udangtangtang.backend.util;

import com.udangtangtang.backend.dto.LocationRequestDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataPreprocessing {

    public LocationRequestDto categoryDataPreprocessing(LocationRequestDto locationRequestDto) {
        String categoryInfo = locationRequestDto.getCategoryName();
        String[] infoBundle = categoryInfo.split(" > ");
        ArrayList<String> infoList = new ArrayList<String>(Arrays.asList(infoBundle));

        // 전처리 완료한 카테고리 값 저장
        locationRequestDto.setCategoryName(infoList.get(infoList.size() - 1));
        return locationRequestDto;
    }
}
