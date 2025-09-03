package com.badmintonhub.notiservice.controller;

import com.badmintonhub.notiservice.dto.message.ObjectResponse;
import com.badmintonhub.notiservice.dto.model.NotiDTO;
import com.badmintonhub.notiservice.dto.request.NotiCreateDTO;
import com.badmintonhub.notiservice.dto.request.NotiUpdateDTO;
import com.badmintonhub.notiservice.entity.Notification;
import com.badmintonhub.notiservice.exception.ResourceNotFoundException;
import com.badmintonhub.notiservice.service.NotiService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/notifications")
public class NotiController {
    private final NotiService notiService;

    public NotiController(NotiService notiService) {
        this.notiService = notiService;
    }

    @GetMapping("/shock/{productId}")
    public ResponseEntity<NotiDTO> shockNoti(@PathVariable(name = "productId") long productId) {
        return ResponseEntity.ok(this.notiService.getNotiWithProduct(productId));
    }

    @GetMapping("/{notiId}")
    public ResponseEntity<NotiDTO> getNoti(@PathVariable(name = "notiId") long notiId) throws ResourceNotFoundException {
        return ResponseEntity.ok(this.notiService.getNotificationById(notiId));
    }

    @GetMapping
    public ResponseEntity<ObjectResponse> getAllNotifications(
            Pageable pageable,
            @Filter Specification<Notification> spec
    ) {
        return ResponseEntity.ok(this.notiService.getAllNotifications(pageable, spec));
    }


    @PostMapping
    public ResponseEntity<NotiDTO> createNotification(@RequestBody NotiCreateDTO notiCreateDTO) {
        return ResponseEntity.ok(this.notiService.createNotification(notiCreateDTO));
    }

    @PutMapping("/{notiId}")
    public ResponseEntity<NotiDTO> updateNotification(
            @PathVariable(name = "notiId") long notiId,
            @RequestBody NotiUpdateDTO notiUpdateDTO) throws ResourceNotFoundException {
        return ResponseEntity.ok(this.notiService.updateNotification(notiId, notiUpdateDTO));
    }

    @DeleteMapping("/{notiId}")
    public ResponseEntity<String> deleteNotification(@PathVariable(name = "notiId") long notiId) throws ResourceNotFoundException {
        this.notiService.deleteNotification(notiId);
        return ResponseEntity.ok("Delete notification successfully!");
    }


}

