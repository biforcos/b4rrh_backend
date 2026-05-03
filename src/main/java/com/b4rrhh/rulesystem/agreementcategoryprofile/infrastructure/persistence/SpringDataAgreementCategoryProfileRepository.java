package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataAgreementCategoryProfileRepository
        extends JpaRepository<AgreementCategoryProfileEntity, Long> {

    Optional<AgreementCategoryProfileEntity> findByAgreementCategoryRuleEntityId(Long agreementCategoryRuleEntityId);

    @Query("""
            SELECT acp.grupoCotizacionCode
            FROM AgreementCategoryProfileEntity acp
            JOIN com.b4rrhh.rulesystem.infrastructure.persistence.RuleEntityEntity re
              ON acp.agreementCategoryRuleEntityId = re.id
            WHERE re.ruleSystemCode = :ruleSystemCode
              AND re.ruleEntityTypeCode = 'AGREEMENT_CATEGORY'
              AND re.code = :categoryCode
            """)
    Optional<String> findGrupoCotizacionCodeByCategoryCode(
            @Param("ruleSystemCode") String ruleSystemCode,
            @Param("categoryCode") String categoryCode
    );
}
