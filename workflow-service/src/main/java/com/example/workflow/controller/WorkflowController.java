package com.example.workflow.controller;

import com.example.workflow.dto.CompleteTaskRequest;
import com.example.workflow.dto.TaskDTO;
import com.example.workflow.dto.WorkflowRequest;
import com.example.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;

    // --- Process ---

    @PostMapping("/start/{dossierId}")
    public ResponseEntity<String> startProcess(@PathVariable Long dossierId) {
        String processId = workflowService.startDossierProcess(dossierId);
        return ResponseEntity.ok("Process started with ID: " + processId);
    }

    @PostMapping("/start")
    public ResponseEntity<String> startProcessFromRequest(@RequestBody WorkflowRequest request) {
        String processId = workflowService.startProcess(request);
        return ResponseEntity.ok("Process started with ID: " + processId);
    }

    @GetMapping("/status/{processInstanceId}")
    public ResponseEntity<String> getProcessStatus(@PathVariable String processInstanceId) {
        String status = workflowService.getProcessStatus(processInstanceId);
        return ResponseEntity.ok("Process status: " + status);
    }

    // --- User Tasks ---

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> listTasks(
            @RequestParam(required = false) String assignee,
            @RequestParam(required = false) String candidateGroup,
            @RequestParam(required = false) Long dossierId
    ) {
        enforceCandidateGroup(candidateGroup);
        var q = taskService.createTaskQuery().active();
        if (assignee != null && !assignee.isBlank()) q = q.taskAssignee(assignee);
        if (candidateGroup != null && !candidateGroup.isBlank()) q = q.taskCandidateGroup(candidateGroup);
        if (dossierId != null) q = q.processVariableValueEquals("dossierId", dossierId);

        List<TaskDTO> tasks = q.list().stream().map(t -> {
            TaskDTO dto = new TaskDTO();
            dto.setId(t.getId());
            dto.setName(t.getName());
            dto.setTaskDefinitionKey(t.getTaskDefinitionKey());
            dto.setAssignee(t.getAssignee());
            dto.setProcessInstanceId(t.getProcessInstanceId());
            dto.setCreated(t.getCreateTime() != null ? t.getCreateTime().toInstant() : null);
            try {
                Object v = runtimeService.getVariable(t.getProcessInstanceId(), "dossierId");
                if (v instanceof Number n) dto.setDossierId(n.longValue());
            } catch (Exception ignored) {
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(tasks);
    }

    private void enforceCandidateGroup(String candidateGroup) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (roles.contains("ROLE_ADMIN")) return;
        if (candidateGroup == null || candidateGroup.isBlank()) {
            // Prevent listing tasks for other groups.
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "candidateGroup is required");
        }
        String expected = expectedCandidateGroupForRoles(roles);
        if (expected == null || !expected.equalsIgnoreCase(candidateGroup)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden for this candidateGroup");
        }
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) CompleteTaskRequest body
    ) {
        enforceTaskRole(taskId);
        Map<String, Object> vars = new HashMap<>();
        if (body != null && body.getVariables() != null) {
            vars.putAll(body.getVariables());
        }
        taskService.complete(taskId, vars);
        return ResponseEntity.ok(Map.of("taskId", taskId, "completed", true));
    }

    private void enforceTaskRole(String taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (roles.contains("ROLE_ADMIN")) return;

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }

        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        Set<String> candidateGroups = new HashSet<>();
        for (IdentityLink l : links) {
            if ("candidate".equalsIgnoreCase(l.getType()) && l.getGroupId() != null && !l.getGroupId().isBlank()) {
                candidateGroups.add(l.getGroupId());
            }
        }

        // If no candidate group is set, allow (fallback for custom tasks).
        if (candidateGroups.isEmpty()) return;

        String expectedGroup = expectedCandidateGroupForRoles(roles);
        if (expectedGroup == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role not allowed");
        }
        boolean ok = candidateGroups.stream().anyMatch(g -> g.equalsIgnoreCase(expectedGroup));
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden for this task group");
        }
    }

    private String expectedCandidateGroupForRoles(Set<String> roles) {
        if (roles.contains("ROLE_AGENT_BUREAU_ORDRE")) return "BO";
        if (roles.contains("ROLE_AGENT_SERVICE")) return "SERVICE";
        if (roles.contains("ROLE_RESPONSABLE")) return "RESPONSABLE";
        if (roles.contains("ROLE_AGENT_FINANCIER")) return "FINANCIER";
        return null;
    }
}

