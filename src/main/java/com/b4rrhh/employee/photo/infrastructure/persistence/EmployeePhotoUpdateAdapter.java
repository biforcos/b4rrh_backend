package com.b4rrhh.employee.photo.infrastructure.persistence;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoUpdatePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmployeePhotoUpdateAdapter implements EmployeePhotoUpdatePort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void setPhotoUrl(Long employeeId, String photoUrl) {
        entityManager.createQuery(
                        "UPDATE EmployeeEntity e SET e.photoUrl = :photoUrl WHERE e.id = :id"
                )
                .setParameter("photoUrl", photoUrl)
                .setParameter("id", employeeId)
                .executeUpdate();
    }
}
