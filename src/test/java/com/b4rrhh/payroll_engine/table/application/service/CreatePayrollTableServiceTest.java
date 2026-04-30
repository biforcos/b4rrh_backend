package com.b4rrhh.payroll_engine.table.application.service;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.object.domain.port.PayrollObjectRepository;
import com.b4rrhh.payroll_engine.table.application.usecase.CreatePayrollTableCommand;
import com.b4rrhh.payroll_engine.table.domain.exception.PayrollTableAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePayrollTableServiceTest {

    @Mock
    private PayrollObjectRepository objectRepository;

    @InjectMocks
    private CreatePayrollTableService service;

    @Test
    void createsTableWhenCodeIsNew() {
        CreatePayrollTableCommand command = new CreatePayrollTableCommand("ESP", "SB_TEST");
        when(objectRepository.existsByBusinessKey("ESP", PayrollObjectTypeCode.TABLE, "SB_TEST"))
                .thenReturn(false);
        when(objectRepository.save(any(PayrollObject.class))).thenAnswer(inv -> {
            PayrollObject input = inv.getArgument(0);
            return new PayrollObject(10L, input.getRuleSystemCode(), input.getObjectTypeCode(),
                    input.getObjectCode(), LocalDateTime.now(), LocalDateTime.now());
        });

        PayrollObject result = service.create(command);

        ArgumentCaptor<PayrollObject> captor = ArgumentCaptor.forClass(PayrollObject.class);
        verify(objectRepository).save(captor.capture());
        assertThat(captor.getValue().getRuleSystemCode()).isEqualTo("ESP");
        assertThat(captor.getValue().getObjectTypeCode()).isEqualTo(PayrollObjectTypeCode.TABLE);
        assertThat(captor.getValue().getObjectCode()).isEqualTo("SB_TEST");
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void rejectsCreationWhenCodeAlreadyExists() {
        CreatePayrollTableCommand command = new CreatePayrollTableCommand("ESP", "SB_TEST");
        when(objectRepository.existsByBusinessKey("ESP", PayrollObjectTypeCode.TABLE, "SB_TEST"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(PayrollTableAlreadyExistsException.class);

        verify(objectRepository, never()).save(any());
    }
}
