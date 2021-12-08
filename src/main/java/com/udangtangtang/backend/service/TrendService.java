package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Location;
import com.udangtangtang.backend.dto.response.TrendResponseDto;
import com.udangtangtang.backend.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class TrendService {

    private final LocationRepository locationRepository;

    public List<TrendResponseDto> getTrendData() {
        String color;
        List<TrendResponseDto> trendResponseDto = new ArrayList<>();
        List<String> tempRegionList = new ArrayList<>();
        HashSet<String> tempRegionHashSet = new HashSet<>();
        List<Location> allLocation = locationRepository.findAll();
        for (Location value : allLocation) {
            if (value.getPlaceName().equals("집")) {
                continue;
            }
            String region = value.getRoadAddressName().split(" ")[0];

            tempRegionList.add(region);
            tempRegionHashSet.add(region);
        }

        // 원래 목표 : count의 평균을 구해서 해당 count 수준에 맞는 색상을 자동으로 넣어주도록 하고 싶었음.
        for (String element : tempRegionHashSet) {
            int count = Collections.frequency(tempRegionList, element);
            if (count >= 100) {
                color = "1050FF";
            } else if (count < 100 && count >= 50) {
                color = "4979FF";
            } else if (count < 50 && count >= 25) {
                color = "658EFF";
            } else if (count < 25 && count >= 10) {
                color = "81A3FF";
            } else {
                color = "BACDFF";
            }

            TrendResponseDto responseDto = new TrendResponseDto(element, count, color);

            trendResponseDto.add(responseDto);
        }
        return trendResponseDto;
    }

    public List<TrendResponseDto> getChartTrendData(String location) {
        List<TrendResponseDto> trendResponseDto = new ArrayList<>();
        List<String> tempCategoryList = new ArrayList<>();
        HashSet<String> tempCategoryHashSet = new HashSet<>();
        List<Location> allLocation = locationRepository.findAll();
        if(location.isEmpty()) {
            for (Location value : allLocation) {
                if (value.getPlaceName().equals("집")) {
                    continue;
                }
                tempCategoryList.add(value.getCategoryName());
                tempCategoryHashSet.add(value.getCategoryName());
            }

            for (String element : tempCategoryHashSet) {
                int count = Collections.frequency(tempCategoryList, element);

                TrendResponseDto responseDto = new TrendResponseDto(element, count);

                trendResponseDto.add(responseDto);
            }
        } else {
            for (Location value : allLocation) {
                if (value.getPlaceName().equals("집")) {
                    continue;
                }
                if (value.getRoadAddressName().contains(location)) {
                    tempCategoryList.add(value.getCategoryName());
                    tempCategoryHashSet.add(value.getCategoryName());
                }
            }
            for (String element : tempCategoryHashSet) {
                int count = Collections.frequency(tempCategoryList, element);

                TrendResponseDto responseDto = new TrendResponseDto(element, count);

                trendResponseDto.add(responseDto);
            }
        }
        return trendResponseDto;
    }
}
