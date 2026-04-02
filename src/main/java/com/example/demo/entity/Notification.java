package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
<<<<<<< HEAD
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
=======
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // TASK_ASSIGNED / STATUS_CHANGED / NEW_COMMENT / OVERDUE
    @Column(length = 50)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(length = 255)
    private String message;

    private boolean isRead = false;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> c9a64acd04492b6ec54c00c0eac45d06b6618803
