package org.livingdoc.fixture.converter.number;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;


class BigDecimalConverterJavaTest {

    BigDecimalConverter cut = new BigDecimalConverter();

    @Test
    void converterCanConvertedToJavaBigDecimal() {
        assertThat(cut.canConvertTo(BigDecimal.class)).isTrue();
    }

    @Test
    void javaInteroperabilityIsWorking() {
        BigDecimal value = cut.convert("42.01");
        assertThat(value).isEqualTo(BigDecimal.valueOf(42.01d));
    }

}
