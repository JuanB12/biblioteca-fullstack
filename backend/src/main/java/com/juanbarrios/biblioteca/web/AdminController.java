package com.juanbarrios.biblioteca.web;

import com.juanbarrios.biblioteca.service.AdminService;
import com.juanbarrios.biblioteca.web.dto.AdminStatsResponse;
import com.juanbarrios.biblioteca.web.dto.BlockedUserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminService.getStats();
    }

    @GetMapping("/users/blocked")
    public List<BlockedUserResponse> blockedUsers() {
        return adminService.getBlockedUsers();
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ResponseEntity.noContent().build();
    }
}
