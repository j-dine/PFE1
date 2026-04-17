package com.example.paiement.repository;

import com.example.paiement.entity.Paiement;
import com.example.paiement.entity.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    List<Paiement> findByDossierId(Long dossierId);

    List<Paiement> findByStatut(StatutPaiement statut);

    Optional<Paiement> findByReference(String reference);
}
