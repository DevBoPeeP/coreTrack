package com.taskservice.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Project> projects;


    @ElementCollection
    @CollectionTable(name = "team_member_ids", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "member_id")
    private Set<UUID> memberIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "team_member_names", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "member_name")
    private Set<String> memberNames = new HashSet<>();




    @Column
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column (name = "updated_at")
    private Instant updatedAt;
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

}
