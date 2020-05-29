package org.livingdoc.converters.number

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.livingdoc.converters.DefaultTypeConverterContract

internal class ByteConverterTest : BoundedNumberConverterContract<Byte>(), DefaultTypeConverterContract {

    override val cut = ByteConverter()

    override val minValue = Byte.MIN_VALUE
    override val negativeValue = (-42).toByte()
    override val zeroValue = 0.toByte()
    override val positiveValue = 42.toByte()
    override val maxValue = Byte.MAX_VALUE

    override val englishValue = "100.12" to 100.toByte()
    override val germanValue = "100,12" to 100.toByte()

    @Test fun `converter can converted to Kotlin Byte`() {
        assertThat(cut.canConvertTo(Byte::class)).isTrue()
    }
}
