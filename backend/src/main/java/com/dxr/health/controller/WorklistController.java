package com.dxr.health.controller;

import com.dxr.health.dto.WorklistItemResponse;
import com.dxr.health.model.TriagePriority;
import com.dxr.health.service.WorklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worklist")
public class WorklistController {

    private final WorklistService worklistService;

    public WorklistController(WorklistService worklistService) {
        this.worklistService = worklistService;
    }

    @GetMapping
    public ResponseEntity<List<WorklistItemResponse>> getWorklist(
            @RequestParam(name = "priority", required = false) String priority
    ) {
        TriagePriority filter = null;
        if (priority != null && !priority.isBlank()) {
            filter = TriagePriority.valueOf(priority.toUpperCase());
        }
        List<WorklistItemResponse> worklist = worklistService.getPrioritizedWorklist(filter);
        return ResponseEntity.ok(worklist);
    }
}
