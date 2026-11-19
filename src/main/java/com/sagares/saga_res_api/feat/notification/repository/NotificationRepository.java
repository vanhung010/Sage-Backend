package com.sagares.saga_res_api.feat.notification.repository;

import com.sagares.saga_res_api.feat.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}