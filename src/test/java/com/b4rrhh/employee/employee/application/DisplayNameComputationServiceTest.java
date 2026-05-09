package com.b4rrhh.employee.employee.application;

import com.b4rrhh.employee.employee.application.port.DisplayNameFormatLookupPort;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisplayNameComputationServiceTest {

    @Mock DisplayNameFormatLookupPort formatLookupPort;
    @InjectMocks DisplayNameComputationService service;

    @Test
    void preferredName_overridesFormat() {
        String result = service.compute("RS1", "Juan", "Garcia", null, "Juanito");

        assertThat(result).isEqualTo("Juanito");
    }

    @Test
    void appliesFormatCode_whenPreferredNameIsNull() {
        when(formatLookupPort.findFormatCodeForRuleSystem("RS1"))
                .thenReturn(Optional.of(DisplayNameFormatCode.FULL_UPPER));

        String result = service.compute("RS1", "Juan", "Garcia", "Lopez", null);

        assertThat(result).isEqualTo("JUAN GARCIA LOPEZ");
    }

    @Test
    void fallsBackToConcatenation_whenNoFormatConfigured() {
        when(formatLookupPort.findFormatCodeForRuleSystem("RS1"))
                .thenReturn(Optional.empty());

        String result = service.compute("RS1", "Juan", "Garcia", "Lopez", null);

        assertThat(result).isEqualTo("Juan Garcia Lopez");
    }

    @Test
    void fallback_skipsNullLastName2() {
        when(formatLookupPort.findFormatCodeForRuleSystem("RS1"))
                .thenReturn(Optional.empty());

        String result = service.compute("RS1", "Juan", "Garcia", null, null);

        assertThat(result).isEqualTo("Juan Garcia");
    }

    @Test
    void blankPreferredName_treatedAsNull() {
        when(formatLookupPort.findFormatCodeForRuleSystem("RS1"))
                .thenReturn(Optional.of(DisplayNameFormatCode.FULL_UPPER));

        String result = service.compute("RS1", "Juan", "Garcia", null, "   ");

        assertThat(result).isEqualTo("JUAN GARCIA");
    }
}
