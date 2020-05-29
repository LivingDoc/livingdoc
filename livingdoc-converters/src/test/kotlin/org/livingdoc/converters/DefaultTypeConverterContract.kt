package org.livingdoc.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.livingdoc.api.conversion.TypeConverter

interface DefaultTypeConverterContract {

    val cut: TypeConverter<out Any>

    @Test
    fun `converter is automatically loaded into manager`() {
        val converterOfType = TypeConverterManager.getDefaultConverters()
            .first { it.javaClass == cut.javaClass }
        assertThat(converterOfType).isNotNull()
    }
}
