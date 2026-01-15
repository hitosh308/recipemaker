package com.heibonsalaryman.recipemaker.repository;

import com.heibonsalaryman.recipemaker.domain.CookLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookLogRepository extends JpaRepository<CookLog, UUID> {
    List<CookLog> findByCookedAtBetween(LocalDateTime from, LocalDateTime to);
}
