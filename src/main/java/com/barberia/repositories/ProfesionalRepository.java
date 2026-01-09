package com.barberia.repositories;

import com.barberia.models.Profesional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesionalRepository  extends JpaRepository<Profesional, Long> {
}
