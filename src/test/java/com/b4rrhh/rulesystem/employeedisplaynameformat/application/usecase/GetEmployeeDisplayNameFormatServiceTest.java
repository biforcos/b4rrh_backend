package com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase;

import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.port.EmployeeDisplayNameFormatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeDisplayNameFormatServiceTest {

    @Mock EmployeeDisplayNameFormatRepository repository;
    @InjectMocks GetEmployeeDisplayNameFormatService service;

    @Test
    void returnsConfig_whenExists() {
        var format = new EmployeeDisplayNameFormat("RSTEST", DisplayNameFormatCode.FULL_UPPER);
        when(repository.findByRuleSystemCode("RSTEST")).thenReturn(Optional.of(format));

        var result = service.getByRuleSystemCode("RSTEST");

        assertThat(result).isPresent().hasValue(format);
    }

    @Test
    void returnsEmpty_whenNotConfigured() {
        when(repository.findByRuleSystemCode("RSTEST")).thenReturn(Optional.empty());

        var result = service.getByRuleSystemCode("RSTEST");

        assertThat(result).isEmpty();
    }

    @Test
    void normalizesRuleSystemCodeToUpperCase() {
        when(repository.findByRuleSystemCode("RSTEST")).thenReturn(Optional.empty());

        service.getByRuleSystemCode("rstest");

        verify(repository).findByRuleSystemCode("RSTEST");
    }
}
