package com.notificationservice.repository;

import com.notificationservice.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

    List<DeviceToken> findByUserIdAndActiveTrue(UUID userId);

    Optional<DeviceToken> findByToken(String token);

    boolean existsByToken(String token);

    @Modifying
    @Transactional
    @Query("UPDATE DeviceToken d SET d.active = false WHERE d.token = :token")
    void deactivateToken(String token);

    @Modifying
    @Transactional
    void deleteAllByUserId(UUID userId);
}
