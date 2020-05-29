package org.livingdoc.scenario.matching

/**
 * Represents a template specified by a fixture developer for scenario steps. Used for matching scenario
 * steps to methods in a fixture. A valid `StepTemplate` consists of a sequence of fragments (text and variables).
 * The sequence of fragments is never empty and does not contain two consecutive fragments of the same type.
 *
 * Variables can be quoted with optional `quotationCharacters` (e.g. single quotation marks). A matching step
 * *must* contain exactly the same quotation characters. By default, no quotation characters are used.
 */
data class StepTemplate(
    val fragments: List<Fragment>,
    val quotationCharacters: Set<Char>
) {

    init {
        assert(fragments.isNotEmpty())
        assertAlternatingSequenceOfFragments()
    }

    private fun assertAlternatingSequenceOfFragments() {
        var wasTextFragment = fragments.first() !is Text
        fragments.forEach { fragment ->
            assert(wasTextFragment xor (fragment is Text))
            wasTextFragment = fragment is Text
        }
    }

    /**
     * Returns a Matching of the template and the specified scenario step.
     */

    fun alignWith(step: String, maxLevelOfStemming: Float = 3.0f) =
        RegMatching(this, step, maxLevelOfStemming)

    override fun toString(): String = fragments.joinToString(separator = "") { fragment ->
        when (fragment) {
            is Text -> fragment.content
            is Variable -> "{${fragment.name}}"
        }
    }

    companion object {

        /**
         * Reads a template for a scenario step description from a string. Optionally takes a set of quotation
         * characters for variable separation.
         *
         * @return a valid `StepTemplate`
         * @throws IllegalFormatException if the specified template string is malformed
         */
        fun parse(templateAsString: String, quotationCharacters: Set<Char> = emptySet()) =
            StepTemplate(
                parseString(
                    templateAsString
                ), quotationCharacters
            )

        private fun parseString(templateAsString: String): List<Fragment> {
            if (templateAsString.isEmpty()) {
                throw IllegalFormatException("StepTemplates cannot be empty!")
            }
            return split(templateAsString)
                .map { createFragment(it) }.toList()
        }

        private fun split(templateAsString: String): List<String> {
            val tokens = mutableListOf<String>()

            var lastIndex = 0
            var isVariable = false
            var isPreceededByVariable = false
            for ((i, c) in templateAsString.withIndex()) {
                if (c == '{' && !isEscaped(
                        templateAsString,
                        i
                    )
                ) {
                    validateVariables(
                        isVariable,
                        i,
                        templateAsString,
                        isPreceededByVariable
                    )
                    isVariable = true
                    if (lastIndex < i) {
                        tokens.add(templateAsString.substring(lastIndex, i))
                    }
                    lastIndex = i
                } else if (c == '}' && !isEscaped(
                        templateAsString,
                        i
                    )
                ) {
                    if (!isVariable) {
                        throw IllegalFormatException(
                            "Illegal closing curly brace at position $i!\nOffending string was: $templateAsString"
                        )
                    }
                    isPreceededByVariable = true
                    isVariable = false
                    tokens.add(templateAsString.substring(lastIndex, i + 1))
                    lastIndex = i + 1
                } else {
                    isPreceededByVariable = false
                }
            }

            if (lastIndex < templateAsString.length) {
                tokens.add(templateAsString.substring(lastIndex))
            }

            return tokens
        }

        private fun validateVariables(
            isVariable: Boolean,
            i: Int,
            templateAsString: String,
            isPreceededByVariable: Boolean
        ) {
            if (isVariable) {
                throw IllegalFormatException(
                    "Illegal opening curly brace at position $i!\nOffending string was: $templateAsString"
                )
            }
            if (isPreceededByVariable) {
                throw IllegalFormatException(
                    "Consecutive variables at position $i! " +
                            "StepTemplate must contain an intervening text fragment to keep them apart.\n" +
                            "Offending string was: $templateAsString"
                )
            }
        }

        private fun isEscaped(s: String, i: Int) = (i > 0 && s[i - 1] == '\\')

        private fun createFragment(fragmentAsString: String): Fragment {
            return if (fragmentAsString.startsWith('{') && fragmentAsString.endsWith('}'))
                Variable(
                    fragmentAsString.substring(
                        1,
                        fragmentAsString.length - 1
                    )
                )
            else
                Text(fragmentAsString)
        }
    }
}

internal class IllegalFormatException(msg: String) : IllegalArgumentException(msg)

sealed class Fragment
internal data class Text(val content: String) : Fragment()
internal data class Variable(val name: String) : Fragment()
