package com.devfat.mini_ecommerce.home.dailyarrival;

import com.devfat.mini_ecommerce.home.dailyarrival.dto.CreateDailyArrivalRequestDto;
import com.devfat.mini_ecommerce.home.dailyarrival.dto.DailyArrivalResponseDto;
import com.devfat.mini_ecommerce.home.dailyarrival.dto.UpdateDailyArrivalRequestDto;
import java.time.LocalDate;
import java.util.List;

public interface DailyArrivalService {
    List<DailyArrivalResponseDto> getByDate(LocalDate date);
    DailyArrivalResponseDto create(CreateDailyArrivalRequestDto request);
    DailyArrivalResponseDto update(Long id, UpdateDailyArrivalRequestDto request);
    void delete(Long id);
}
