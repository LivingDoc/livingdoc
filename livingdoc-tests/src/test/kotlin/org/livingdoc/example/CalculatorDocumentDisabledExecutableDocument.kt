package org.livingdoc.example

import org.assertj.core.api.Assertions.assertThat
import org.livingdoc.api.disabled.Disabled
import org.livingdoc.api.documents.ExecutableDocument
import org.livingdoc.api.fixtures.decisiontables.BeforeRow
import org.livingdoc.api.fixtures.decisiontables.Check
import org.livingdoc.api.fixtures.decisiontables.DecisionTableFixture
import org.livingdoc.api.fixtures.decisiontables.Input

/**
 * This [ExecutableDocument] demonstrates the [Disabled] annotation. It will not be run during the LivingDoc test execution. Instead its state will be recorded as `Disabled`
 *
 * @see Disabled
 * @see ExecutableDocument
 */
@Disabled("Skip this document")
@ExecutableDocument("local://Calculator.html")
class CalculatorDocumentDisabledExecutableDocument {

    /**
     * This [DecisionTableFixture] demonstrates that the [Disabled] annotation from above works and you are able to disable the document even if it contains fixtures.
     * Otherwise this would not be tested.
     * @see DecisionTableFixture
     */

    @DecisionTableFixture
    class CalculatorDecisionTableFixture {

        private lateinit var sut: Calculator

        @Input("a")
        private var valueA: Float = 0f
        private var valueB: Float = 0f

        @BeforeRow
        fun beforeRow() {
            sut = Calculator()
        }

        @Input("b")
        fun setValueB(valueB: Float) {
            this.valueB = valueB
        }

        @Check("a + b = ?")
        fun checkSum(expectedValue: Float) {
            val result = sut.sum(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }

        @Check("a - b = ?")
        fun checkDiff(expectedValue: Float) {
            val result = sut.diff(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }

        @Check("a * b = ?")
        fun checkMultiply(expectedValue: Float) {
            val result = sut.multiply(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }

        @Check("a / b = ?")
        fun checkDivide(expectedValue: Float) {
            val result = sut.divide(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }
    }

    @DecisionTableFixture
    class CalculatorDecisionTableFixture2 {

        private lateinit var sut: Calculator

        @Input("x")
        private var valueA: Float = 0f
        private var valueB: Float = 0f

        @BeforeRow
        fun beforeRow() {
            sut = Calculator()
        }

        @Input("y")
        fun setValueB(valueB: Float) {
            this.valueB = valueB
        }

        @Check("a + b = ?")
        fun checkSum(expectedValue: Float) {
            val result = sut.sum(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }

        @Check("a - b = ?")
        fun checkDiff(expectedValue: Float) {
            val result = sut.diff(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }

        @Check("a * b = ?")
        fun checkMultiply(expectedValue: Float) {
            val result = sut.multiply(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }

        @Check("a / b = ?")
        fun checkDivide(expectedValue: Float) {
            val result = sut.divide(valueA, valueB)
            assertThat(result).isEqualTo(expectedValue)
        }
    }
}
