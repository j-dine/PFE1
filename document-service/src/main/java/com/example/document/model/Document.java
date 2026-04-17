package com.example.document.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UML: nomFichier
    @Column(name = "nom_fichier")
    private String nomFichier;

    private String type;

    // UML: urlStockage / urlStorage
    @Column(name = "url_storage")
    private String urlStorage;

    // Reference to dossier-service entity
    private Long dossierId;

    @Column(name = "date_upload")
    private LocalDateTime dateUpload;

    @PrePersist
    public void prePersist() {
        if (this.dateUpload == null) {
            this.dateUpload = LocalDateTime.now();
        }
    }
}