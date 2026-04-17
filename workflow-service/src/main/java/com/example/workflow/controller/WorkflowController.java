package com.example.workflow.controller;

import com.example.workflow.dto.CompleteTaskRequest;
import com.example.workflow.dto.TaskDTO;
import com.example.workflow.dto.WorkflowRequest;
import com.example.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) CompleteTaskRequest body
    ) {
        Map<String, Object> vars = new HashMap<>();
        if (body != null && body.getVariables() != null) {
            vars.putAll(body.getVariables());
        }
        taskService.complete(taskId, vars);
        return ResponseEntity.ok(Map.of("taskId", taskId, "completed", true));
    }
}

