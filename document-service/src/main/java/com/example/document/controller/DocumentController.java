package com.example.document.controller;

import com.example.document.dto.DocumentDTO;
import com.example.document.model.Document;
import com.example.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public List<DocumentDTO> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return documentService.downloadFile(id);
    }

    @GetMapping("/dossier/{dossierId}")
    public List<DocumentDTO> getDocumentsByDossier(@PathVariable Long dossierId) {
        return documentService.getDocumentsByDossier(dossierId);
    }

    // Create metadata (JSON)
    @PostMapping
    public ResponseEntity<DocumentDTO> createDocument(@RequestBody Document document) {
        return ResponseEntity.ok(documentService.createDocument(document));
    }

    // Upload file (multipart/form-data)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDTO> upload(
            @RequestParam Long dossierId,
            @RequestParam(required = false) String type,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(documentService.upload(dossierId, type, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
