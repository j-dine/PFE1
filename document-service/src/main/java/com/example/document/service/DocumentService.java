package com.example.document.service;

import com.example.document.dto.DocumentDTO;
import com.example.document.exception.ResourceNotFoundException;
import com.example.document.model.Document;
import com.example.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Value("${app.storage.upload-dir:/app/uploads}")
    private String uploadDir;

    public List<DocumentDTO> getAllDocuments() {
        return documentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public DocumentDTO getDocumentById(Long id) {
        Document d = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouve avec l'id : " + id));
        return toDTO(d);
    }

    public List<DocumentDTO> getDocumentsByDossier(Long dossierId) {
        return documentRepository.findByDossierId(dossierId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Metadata-only create
    public DocumentDTO createDocument(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document obligatoire");
        }
        if (document.getDossierId() == null) {
            throw new IllegalArgumentException("dossierId obligatoire");
        }
        if (document.getNomFichier() == null || document.getNomFichier().isBlank()) {
            throw new IllegalArgumentException("nomFichier obligatoire");
        }
        return toDTO(documentRepository.save(document));
    }

    // Multipart upload: store file + persist metadata
    public DocumentDTO upload(Long dossierId, String type, MultipartFile file) {
        if (dossierId == null) {
            throw new IllegalArgumentException("dossierId obligatoire");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier obligatoire");
        }

        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            original = "document";
        }
        // Basic sanitization
        original = original.replace("\\\\", "_").replace("/", "_");

        Path dir = Paths.get(uploadDir);
        try {
            Files.createDirectories(dir);
            String stored = UUID.randomUUID() + "_" + original;
            Path target = dir.resolve(stored);
            file.transferTo(target);

            Document d = new Document();
            d.setDossierId(dossierId);
            d.setType(type);
            d.setNomFichier(original);
            d.setUrlStorage("file:" + target.toAbsolutePath().toString().replace('\\', '/'));
            return toDTO(documentRepository.save(d));
        } catch (IOException e) {
            throw new IllegalArgumentException("Upload impossible: " + e.getMessage());
        }
    }

    public void deleteDocument(Long id) {
        Document d = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouve avec l'id : " + id));

        // Best-effort remove file from storage if it exists.
        String storage = d.getUrlStorage();
        if (storage != null && !storage.isBlank()) {
            String pathStr = storage.startsWith("file:") ? storage.substring("file:".length()) : storage;
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = Paths.get(pathStr).toAbsolutePath().normalize();
            if (filePath.startsWith(base)) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException ignored) {
                    // If deletion fails, still remove DB row (e.g. file already removed manually).
                }
            }
        }

        documentRepository.deleteById(id);
    }

    public ResponseEntity<Resource> downloadFile(Long id) {
        Document d = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouve avec l'id : " + id));

        String storage = d.getUrlStorage();
        if (storage == null || storage.isBlank()) {
            throw new IllegalArgumentException("urlStorage manquant pour le document " + id);
        }

        String pathStr = storage.startsWith("file:") ? storage.substring("file:".length()) : storage;
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = Paths.get(pathStr).toAbsolutePath().normalize();

        // Basic safety: don't allow reading outside of upload dir.
        if (!filePath.startsWith(base)) {
            throw new IllegalArgumentException("Chemin interdit");
        }

        try {
            Resource res = new UrlResource(filePath.toUri());
            if (!res.exists()) {
                throw new ResourceNotFoundException("Fichier introuvable sur le stockage");
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            String filename = d.getNomFichier() != null && !d.getNomFichier().isBlank()
                    ? d.getNomFichier()
                    : filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                    .body(res);
        } catch (IOException e) {
            throw new IllegalArgumentException("Lecture fichier impossible: " + e.getMessage());
        }
    }

    private DocumentDTO toDTO(Document d) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(d.getId());
        dto.setNomFichier(d.getNomFichier());
        dto.setType(d.getType());
        dto.setUrlStorage(d.getUrlStorage());
        dto.setDossierId(d.getDossierId());
        dto.setDateUpload(d.getDateUpload());
        return dto;
    }
}
