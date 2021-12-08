package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.dto.response.TrendResponseDto;
import com.udangtangtang.backend.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrendController {
    private final TrendService trendService;

    @GetMapping("/trend")
    public List<TrendResponseDto> getTrendData() {
        return trendService.getTrendData();
    }

    @GetMapping("/trend/chart")
    public List<TrendResponseDto> getChartTrendData(@RequestParam(required = false) String location) {
        return trendService.getChartTrendData(location);
    }
}
