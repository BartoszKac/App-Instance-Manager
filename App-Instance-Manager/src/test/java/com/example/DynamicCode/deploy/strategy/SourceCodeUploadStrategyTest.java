package com.example.DynamicCode.deploy.strategy;

import com.example.DynamicCode.code.entity.SourceCode;
import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.code.service.SourceCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SourceCodeUploadStrategyTest {

    @Mock
    private SourceCodeService sourceCodeService;
    @InjectMocks
    private SourceCodeUploadStrategy strategy;

    @Test
    void shouldEncodeSourceAsUtf8WhenItContainsPolishLetters() {
        String code = "print(\"zażółć gęślą jaźń\")";
        when(sourceCodeService.getAllFilesFromMainClass(3L))
                .thenReturn(List.of(new SourceCode(3L, "main.py", code, LanguageType.PYTHON, 3L)));

        Map<String, byte[]> files = strategy.resolveFiles(3L);

        assertThat(files.get("main.py")).isEqualTo(code.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldKeepLastFileWhenNamesRepeat() {
        when(sourceCodeService.getAllFilesFromMainClass(3L)).thenReturn(List.of(
                new SourceCode(3L, "a.py", "first", LanguageType.PYTHON, 3L),
                new SourceCode(4L, "a.py", "second", LanguageType.PYTHON, 3L)));

        Map<String, byte[]> files = strategy.resolveFiles(3L);

        assertThat(new String(files.get("a.py"), StandardCharsets.UTF_8)).isEqualTo("second");
    }
}
