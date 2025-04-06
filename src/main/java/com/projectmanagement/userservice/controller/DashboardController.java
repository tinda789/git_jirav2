package com.projectmanagement.userservice.controller;

import com.projectmanagement.userservice.dto.DashboardStatsDto;
import com.projectmanagement.userservice.dto.MessageResponse;
import com.projectmanagement.userservice.dto.UserProductivityDto;
import com.projectmanagement.userservice.dto.WorkListReportDto;
import com.projectmanagement.userservice.entity.User;
import com.projectmanagement.userservice.service.AuthService;
import com.projectmanagement.userservice.service.DashboardService;
import com.projectmanagement.userservice.service.WorkListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private WorkListService workListService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        User currentUser = authService.getCurrentUser();
        DashboardStatsDto stats = dashboardService.getDashboardStats(currentUser);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/worklist/{workListId}/report")
    @PreAuthorize("@securityService.canManageWorkList(#workListId, principal)")
    public ResponseEntity<?> getWorkListReport(@PathVariable Long workListId) {
        User currentUser = authService.getCurrentUser();
        
        // Kiểm tra quyền báo cáo nâng cao
        if (!currentUser.getSubscription().getHasAdvancedReporting()) {
            return ResponseEntity.status(403)
                    .body(new MessageResponse("Advanced reporting requires a premium subscription"));
        }
        
        return workListService.getWorkListById(workListId)
                .map(workList -> {
                    WorkListReportDto report = dashboardService.getWorkListReport(workList);
                    return ResponseEntity.ok(report);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/worklist/{workListId}/productivity")
    @PreAuthorize("@securityService.canManageWorkList(#workListId, principal)")
    public ResponseEntity<?> getUserProductivity(@PathVariable Long workListId) {
        User currentUser = authService.getCurrentUser();
        
        // Kiểm tra quyền báo cáo nâng cao
        if (!currentUser.getSubscription().getHasAdvancedReporting()) {
            return ResponseEntity.status(403)
                    .body(new MessageResponse("Advanced reporting requires a premium subscription"));
        }
        
        return workListService.getWorkListById(workListId)
                .map(workList -> {
                    List<UserProductivityDto> productivity = dashboardService.getUserProductivity(workList);
                    return ResponseEntity.ok(productivity);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}