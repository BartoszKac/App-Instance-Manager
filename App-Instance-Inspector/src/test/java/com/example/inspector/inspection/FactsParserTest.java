package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.Section;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FactsParserTest {

    @Test
    void shouldParseKnownSectionsAndIgnoreUnknownOnes() {
        List<Section> sections = FactsParser.parse("\n@@hostname\nbox\n\n@@unknown\nx\n\n@@ip\neth0 10.0.0.1/24\neth1 10.0.1.1/24\n\n@@gpu\n");

        assertThat(sections).extracting(Section::getKey).containsExactly("hostname", "ip");
        assertThat(sections.get(1).getValue()).isEqualTo("eth0 10.0.0.1/24\neth1 10.0.1.1/24");
        assertThat(sections.get(1).getCategory()).isEqualTo("Sieć");
    }

    @Test
    void shouldRecognizeOutputOnlyWithHostnameMarker() {
        assertThat(FactsParser.isFacts("\n@@hostname\nbox")).isTrue();
        assertThat(FactsParser.isFacts("'section' is not recognized")).isFalse();
        assertThat(FactsParser.isFacts(null)).isFalse();
    }
}
