package com.b4rrhh.payroll_engine.concept.application.service;

import com.b4rrhh.payroll_engine.concept.domain.exception.PayrollConceptNotFoundException;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletePayrollConceptServiceTest {

    @Mock
    private PayrollConceptRepository conceptRepository;

    @InjectMocks
    private DeletePayrollConceptService service;

    @Test
    void deletesExistingConcept() {
        when(conceptRepository.existsByBusinessKey("ES", "201")).thenReturn(true);

        service.delete("ES", "201");

        verify(conceptRepository).deleteByBusinessKey("ES", "201");
    }

    @Test
    void throwsNotFoundForUnknownConcept() {
        when(conceptRepository.existsByBusinessKey("ES", "999")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("ES", "999"))
                .isInstanceOf(PayrollConceptNotFoundException.class);

        verify(conceptRepository, never()).deleteByBusinessKey("ES", "999");
    }
}
