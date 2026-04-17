package com.example.dossier.repository;

import com.example.dossier.entity.Dossier;
import com.example.dossier.entity.StatutDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {
    List<Dossier> findByUserId(Long userId);

    List<Dossier> findByStatut(StatutDossier statut);
}
