package com.pruebas.fperiago.SpringBatchHibernate.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * SpringBatchHibernate - CASS - Clase de modelo del Canal de Comunicación
 * com.pruebas.fperiago.SpringBatchHibernate.model
 * 08/09/2020 - 9:21
 * f.periago.oliver
 */
// Clase de modelo para ser utilizada como contenedor de Spring.
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"idComunicacio", "descripcioComunicacio"})
public class CanalComunicacio {

    @Id
    @GeneratedValue
    private int id;

    // Transient indica que estos campos no deben ser conservados en BBDD.
    @Transient
    @NonNull
    private String idCanal;

    @Transient
    @NonNull
    private String descripcioCanal;

    @NonNull
    private String nombreCanal;

    public void concatenateName() {
        this.setNombreCanal(this.idCanal.concat(" - ").concat(this.descripcioCanal));
    }
}
