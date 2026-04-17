package com.example.dossier.repository;

import com.example.dossier.entity.HistoriqueAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<HistoriqueAction, Long> {
    List<HistoriqueAction> findByDossierId(Long dossierId);
}
