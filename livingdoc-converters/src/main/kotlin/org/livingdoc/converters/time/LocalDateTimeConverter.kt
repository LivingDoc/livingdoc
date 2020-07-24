package org.livingdoc.converters.time

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * This converter parses a String to the local date and time format
 */
open class LocalDateTimeConverter : AbstractTemporalConverter<LocalDateTime>() {

    override fun defaultFormatter(): DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    override fun doParse(value: String, formatter: DateTimeFormatter): LocalDateTime =
        LocalDateTime.parse(value, formatter)

    override fun canConvertTo(targetType: Class<*>) = LocalDateTime::class.java == targetType
}
