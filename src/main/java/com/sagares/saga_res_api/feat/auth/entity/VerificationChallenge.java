package com.sagares.saga_res_api.feat.auth.entity;

import com.sagares.saga_res_api.common.base.BaseEntity;

import com.sagares.saga_res_api.feat.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_challenges", schema = "sage")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class VerificationChallenge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private VerificationPurpose purpose;

    @Column(name = "target_email")
    private String targetEmail;

    @Column(name = "code_hash", nullable = false, columnDefinition = "TEXT")
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    // AuditingEntityListener đã điền createdAt trước callback của entity.
    @PrePersist
    private void initializeExpiry() {
        if (expiresAt == null) {
            expiresAt = getCreatedAt().plusMinutes(5);
        }
    }

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null
                && expiresAt != null
                && now.isBefore(expiresAt);
    }
}