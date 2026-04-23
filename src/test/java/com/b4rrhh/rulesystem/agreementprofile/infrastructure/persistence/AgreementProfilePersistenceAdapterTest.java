package com.b4rrhh.rulesystem.agreementprofile.infrastructure.persistence;

import com.b4rrhh.rulesystem.agreementprofile.domain.model.AgreementProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true"
})
@Import(AgreementProfilePersistenceAdapter.class)
class AgreementProfilePersistenceAdapterTest {

    @Autowired
    private SpringDataAgreementProfileRepository repository;

    @Autowired
    private AgreementProfilePersistenceAdapter adapter;

    @Test
    void savesPersistsAgreementProfileByAgreementRuleEntityId() {
        Long agreementRuleEntityId = 1L;
        AgreementProfile profile = new AgreementProfile(
                "CA-2024-001",
                "Collective Agreement 2024",
                "CA-2024",
                new BigDecimal("1560.00"),
                true
        );

        AgreementProfile saved = adapter.save(agreementRuleEntityId, profile);

        assertNotNull(saved);
        assertEquals("CA-2024-001", saved.getOfficialAgreementNumber());
        assertEquals("Collective Agreement 2024", saved.getDisplayName());
        assertEquals(0, new BigDecimal("1560.00").compareTo(saved.getAnnualHours()));
        assertTrue(saved.isActive());
    }

    @Test
    void findByAgreementRuleEntityIdReturnsProfileWhenExists() {
        Long agreementRuleEntityId = 1L;
        AgreementProfile profile = new AgreementProfile(
                "CA-2024-001",
                "Collective Agreement 2024",
                "CA-2024",
                new BigDecimal("1560.00"),
                true
        );
        adapter.save(agreementRuleEntityId, profile);

        Optional<AgreementProfile> found = adapter.findByAgreementRuleEntityId(agreementRuleEntityId);

        assertTrue(found.isPresent());
        assertEquals("CA-2024-001", found.get().getOfficialAgreementNumber());
    }

    @Test
    void findByAgreementRuleEntityIdReturnsEmptyWhenNotFound() {
        Optional<AgreementProfile> found = adapter.findByAgreementRuleEntityId(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void updateModifiesExistingProfile() {
        Long agreementRuleEntityId = 1L;
        AgreementProfile original = new AgreementProfile(
                "CA-2024-001",
                "Original Name",
                "ORIG",
                new BigDecimal("1560.00"),
                true
        );
        adapter.save(agreementRuleEntityId, original);

        AgreementProfile updated = original.update(
                "CA-2024-002",
                "Updated Name",
                "UPD",
                new BigDecimal("1680.00"),
                true
        );
        adapter.save(agreementRuleEntityId, updated);

        Optional<AgreementProfile> fetched = adapter.findByAgreementRuleEntityId(agreementRuleEntityId);
        assertTrue(fetched.isPresent());
        assertEquals("CA-2024-002", fetched.get().getOfficialAgreementNumber());
        assertEquals("Updated Name", fetched.get().getDisplayName());
        assertEquals(0, new BigDecimal("1680.00").compareTo(fetched.get().getAnnualHours()));
    }
}
