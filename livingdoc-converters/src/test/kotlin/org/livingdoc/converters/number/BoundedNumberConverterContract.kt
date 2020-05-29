package org.livingdoc.converters.number

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.livingdoc.converters.convertValueOnly
import org.livingdoc.converters.exceptions.NumberRangeException
import utils.EnglishDefaultLocale
import java.math.BigDecimal

@EnglishDefaultLocale
internal abstract class BoundedNumberConverterContract<T : Number> : NumberConverterContract<T>() {

    @Nested
    inner class bounds {

        @Test
        fun `if lower bound is broken an exception is thrown`() {
            val lowerBound = cut.lowerBound
            assumeTrue { lowerBound != null }

            val lowerBoundAsString = "$lowerBound"
            val lowerBoundPlusOne = BigDecimal(lowerBoundAsString).subtract(BigDecimal.ONE)
            val lowerBoundPlusOneAsString = "$lowerBoundPlusOne"
            assertThrows(NumberRangeException::class.java) {
                cut.convertValueOnly(lowerBoundPlusOneAsString)
            }
        }

        @Test
        fun `if upper bound is broken an exception is thrown`() {
            val upperBound = cut.upperBound
            assumeTrue { upperBound != null }

            val upperBoundAsString = "$upperBound"
            val upperBoundPlusOne = BigDecimal(upperBoundAsString).add(BigDecimal.ONE)
            val upperBoundPlusOneAsString = "$upperBoundPlusOne"
            assertThrows(NumberRangeException::class.java) {
                cut.convertValueOnly(upperBoundPlusOneAsString)
            }
        }
    }
}
