package com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.port.EmployeeDisplayNameFormatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpsertEmployeeDisplayNameFormatServiceTest {

    @Mock EmployeeDisplayNameFormatRepository formatRepository;
    @Mock RuleSystemRepository ruleSystemRepository;
    @InjectMocks UpsertEmployeeDisplayNameFormatService service;

    private RuleSystem ruleSystem(String code) {
        return new RuleSystem(1L, code, "Test RS", "ES", true,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void savesFormat_whenRuleSystemExists() {
        when(ruleSystemRepository.findByCode("RSTEST")).thenReturn(Optional.of(ruleSystem("RSTEST")));
        var saved = new EmployeeDisplayNameFormat("RSTEST", DisplayNameFormatCode.FULL_UPPER);
        when(formatRepository.save(any())).thenReturn(saved);

        var result = service.upsert(new UpsertEmployeeDisplayNameFormatCommand("RSTEST", "FULL_UPPER"));

        assertThat(result.ruleSystemCode()).isEqualTo("RSTEST");
        assertThat(result.formatCode()).isEqualTo(DisplayNameFormatCode.FULL_UPPER);
        verify(formatRepository).save(new EmployeeDisplayNameFormat("RSTEST", DisplayNameFormatCode.FULL_UPPER));
    }

    @Test
    void throwsIllegalArgument_whenRuleSystemNotFound() {
        when(ruleSystemRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.upsert(new UpsertEmployeeDisplayNameFormatCommand("UNKNOWN", "FULL_UPPER")))
                .withMessageContaining("UNKNOWN");
    }

    @Test
    void throwsIllegalArgument_whenFormatCodeInvalid() {
        when(ruleSystemRepository.findByCode("RSTEST")).thenReturn(Optional.of(ruleSystem("RSTEST")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.upsert(new UpsertEmployeeDisplayNameFormatCommand("RSTEST", "BOGUS_FORMAT")))
                .withMessageContaining("BOGUS_FORMAT");
    }

    @Test
    void normalizesRuleSystemCodeToUpperCase() {
        when(ruleSystemRepository.findByCode("RSTEST")).thenReturn(Optional.of(ruleSystem("RSTEST")));
        var saved = new EmployeeDisplayNameFormat("RSTEST", DisplayNameFormatCode.FULL_UPPER);
        when(formatRepository.save(any())).thenReturn(saved);

        service.upsert(new UpsertEmployeeDisplayNameFormatCommand("rstest", "FULL_UPPER"));

        verify(ruleSystemRepository).findByCode("RSTEST");
        verify(formatRepository).save(new EmployeeDisplayNameFormat("RSTEST", DisplayNameFormatCode.FULL_UPPER));
    }
}
