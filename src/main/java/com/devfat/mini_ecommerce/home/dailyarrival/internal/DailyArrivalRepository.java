package com.devfat.mini_ecommerce.home.dailyarrival.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyArrivalRepository extends JpaRepository<DailyArrivalEntity, Long> {
    List<DailyArrivalEntity> findByArrivalDateAndIsActiveTrue(LocalDate date);

    @Query("SELECT d FROM DailyArrivalEntity d " +
            "JOIN FETCH d.product p " +
            "WHERE d.arrivalDate = :date " +
            "AND d.isActive = true " +
            "AND p.isActive = true")
    List<DailyArrivalEntity> findByArrivalDateWithProduct(@Param("date") LocalDate date);
}
