package org.livingdoc.converters.number

import java.math.BigDecimal

/**
 * This converter converts a BigDecimal number to a Long
 */
open class LongConverter : AbstractNumberConverter<Long>() {

    override val lowerBound: Long = Long.MIN_VALUE
    override val upperBound: Long = Long.MAX_VALUE

    /**
     * This function returns the value of number as a Long
     */
    override fun convertToTarget(number: BigDecimal): Long = number.toLong()

    override fun canConvertTo(targetType: Class<*>): Boolean {
        val isJavaObjectType = Long::class.javaObjectType == targetType
        val isKotlinType = Long::class.java == targetType
        return isJavaObjectType || isKotlinType
    }
}
