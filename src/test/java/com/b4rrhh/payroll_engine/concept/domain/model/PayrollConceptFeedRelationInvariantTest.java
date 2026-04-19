package com.b4rrhh.payroll_engine.concept.domain.model;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollConceptFeedRelationInvariantTest {

    private PayrollObject conceptObject(Long id, String code) {
        return new PayrollObject(id, "ESP", PayrollObjectTypeCode.CONCEPT, code, null, null);
    }

    private PayrollObject tableObject(Long id, String code) {
        return new PayrollObject(id, "ESP", PayrollObjectTypeCode.TABLE, code, null, null);
    }

    private PayrollConceptFeedRelation validRelation(
            PayrollObject source,
            PayrollObject target,
            LocalDate from,
            LocalDate to
    ) {
        return new PayrollConceptFeedRelation(null, source, target, FeedMode.FEED_BY_SOURCE, null, from, to, null, null);
    }

    @Test
    void sourceObjectMustBeConceptType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validRelation(
                        tableObject(1L, "TABLA1"),
                        conceptObject(2L, "IRPF"),
                        LocalDate.of(2025, 1, 1), null
                ));

        assertTrue(ex.getMessage().contains("sourceObject must be of type CONCEPT"));
    }

    @Test
    void targetObjectMustBeConceptType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validRelation(
                        conceptObject(1L, "SALBASE"),
                        tableObject(2L, "TABLA1"),
                        LocalDate.of(2025, 1, 1), null
                ));

        assertTrue(ex.getMessage().contains("targetObject must be of type CONCEPT"));
    }

    @Test
    void effectiveToBeforeEffectiveFromThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validRelation(
                        conceptObject(1L, "SALBASE"),
                        conceptObject(2L, "IRPF"),
                        LocalDate.of(2025, 6, 1),
                        LocalDate.of(2025, 1, 1)
                ));

        assertTrue(ex.getMessage().contains("effectiveTo"));
    }

    @Test
    void effectiveToEqualToEffectiveFromIsValid() {
        // same date is allowed (one-day effective range)
        validRelation(
                conceptObject(1L, "SALBASE"),
                conceptObject(2L, "IRPF"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 1)
        );
    }

    @Test
    void nullEffectiveToIsValid() {
        validRelation(
                conceptObject(1L, "SALBASE"),
                conceptObject(2L, "IRPF"),
                LocalDate.of(2025, 1, 1),
                null
        );
    }

    @Test
    void isActiveAtReturnsTrueWithinRange() {
        PayrollConceptFeedRelation relation = validRelation(
                conceptObject(1L, "SALBASE"),
                conceptObject(2L, "IRPF"),
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        assertTrue(relation.isActiveAt(LocalDate.of(2025, 6, 15)));
    }
}
