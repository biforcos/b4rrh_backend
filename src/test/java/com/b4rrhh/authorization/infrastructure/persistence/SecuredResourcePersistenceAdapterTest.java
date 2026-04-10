package com.b4rrhh.authorization.infrastructure.persistence;

import com.b4rrhh.authorization.domain.model.SecuredResource;
import com.b4rrhh.authorization.domain.model.SecuredResourceKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecuredResourcePersistenceAdapterTest {

    @Mock
    private SpringDataSecuredResourceRepository springDataRepository;

    private SecuredResourcePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SecuredResourcePersistenceAdapter(springDataRepository);
    }

    @Test
    void mapsValidSecuredResourceToDomain() {
        SecuredResourceEntity entity = new SecuredResourceEntity();
        entity.setCode("EMPLOYEE.CONTACT");
        entity.setParentCode("EMPLOYEE");
        entity.setBoundedContextCode("employee");
        entity.setResourceKind("VERTICAL");
        entity.setResourceFamilyCode("EMPLOYEE_DATA");
        entity.setName("Employee Contact");
        entity.setActive(true);

        when(springDataRepository.findByCode("EMPLOYEE.CONTACT")).thenReturn(Optional.of(entity));

        Optional<SecuredResource> result = adapter.findByCode("EMPLOYEE.CONTACT");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().code()).isEqualTo("EMPLOYEE.CONTACT");
        assertThat(result.orElseThrow().parentCode()).isEqualTo("EMPLOYEE");
        assertThat(result.orElseThrow().boundedContextCode()).isEqualTo("employee");
        assertThat(result.orElseThrow().resourceKind()).isEqualTo(SecuredResourceKind.VERTICAL);
        assertThat(result.orElseThrow().resourceFamilyCode()).isEqualTo("EMPLOYEE_DATA");
        assertThat(result.orElseThrow().name()).isEqualTo("Employee Contact");
        assertThat(result.orElseThrow().active()).isTrue();
    }

    @Test
    void failsFastWhenDatabaseContainsInvalidResourceKind() {
        SecuredResourceEntity entity = new SecuredResourceEntity();
        entity.setCode("EMPLOYEE.CONTACT");
        entity.setParentCode("EMPLOYEE");
        entity.setBoundedContextCode("employee");
        entity.setResourceKind("NOT_A_KIND");
        entity.setResourceFamilyCode("EMPLOYEE_DATA");
        entity.setName("Employee Contact");
        entity.setActive(true);

        when(springDataRepository.findByCode("EMPLOYEE.CONTACT")).thenReturn(Optional.of(entity));

        assertThrows(IllegalStateException.class, () -> adapter.findByCode("EMPLOYEE.CONTACT"));
    }
}