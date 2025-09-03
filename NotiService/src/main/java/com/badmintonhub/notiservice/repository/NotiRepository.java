package com.badmintonhub.notiservice.repository;

import com.badmintonhub.notiservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotiRepository extends JpaRepository<Notification,Long>, JpaSpecificationExecutor<Notification> {

}
