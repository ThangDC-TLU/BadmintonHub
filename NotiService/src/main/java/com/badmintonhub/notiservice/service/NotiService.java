package com.badmintonhub.notiservice.service;

import com.badmintonhub.notiservice.dto.message.ObjectResponse;
import com.badmintonhub.notiservice.dto.model.NotiDTO;
import com.badmintonhub.notiservice.dto.request.NotiCreateDTO;
import com.badmintonhub.notiservice.dto.request.NotiUpdateDTO;
import com.badmintonhub.notiservice.entity.Notification;
import com.badmintonhub.notiservice.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface NotiService {
    NotiDTO getNotiWithProduct(long productId);

    NotiDTO getNotificationById(long notiId) throws ResourceNotFoundException;

    ObjectResponse getAllNotifications(Pageable pageable, Specification<Notification> spec);

    void deleteNotification(long notiId) throws ResourceNotFoundException;

    NotiDTO createNotification(NotiCreateDTO notiCreateDTO);

    NotiDTO updateNotification(long notiId, NotiUpdateDTO notiUpdateDTO) throws ResourceNotFoundException;
}
