package com.pruebas.fperiago.SpringBatchHibernate.repository;

import com.pruebas.fperiago.SpringBatchHibernate.entity.CanalComunicacio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SpringBatchHibernate - CASS
 * com.pruebas.fperiago.SpringBatchHibernate.repository
 * 08/09/2020 - 9:33
 * f.periago.oliver
 */
// Implementación de Repositorio de JPA.
@Repository
public interface CanalComunicacioRepository extends JpaRepository<CanalComunicacio, Integer> {
}
