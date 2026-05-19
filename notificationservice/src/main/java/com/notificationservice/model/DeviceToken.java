package com.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_tokens", indexes = {
        @Index(name = "idx_device_tokens_user_id", columnList = "userId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 512)
    private String token;          // FCM device token

    @Enumerated(EnumType.STRING)
    private DevicePlatform platform;  // ANDROID, IOS, WEB

    @CreatedDate
    @Column(updatable = false)
    private Instant registeredAt;

    private Instant lastUsedAt;

    @Builder.Default
    private boolean active = true;

    public enum DevicePlatform {
        ANDROID,
        IOS,
        WEB
    }
}
