package com.tms.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "otp")
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name= "code")
    private String code;
    @Column(name = "otp", nullable = false)
    private String codeHash;
    @Column(name = "email", nullable = false)
    private String email;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "expiry", nullable = false)
    private LocalDateTime expiry;
   @Column(name = "status", nullable = false)
   @Enumerated(EnumType.STRING)
   private Status status;
   public enum Status {
       ACTIVE, USED, EXPIRED
   }
}
