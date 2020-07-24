package org.livingdoc.engine.execution.examples.decisiontables

import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.livingdoc.engine.mockkJClass
import org.livingdoc.engine.resources.DisabledDecisionTableDocument
import org.livingdoc.repositories.model.decisiontable.DecisionTable
import org.livingdoc.repositories.model.decisiontable.Field
import org.livingdoc.repositories.model.decisiontable.Header
import org.livingdoc.repositories.model.decisiontable.Row
import org.livingdoc.results.Status
import org.livingdoc.results.examples.decisiontables.DecisionTableResult

internal class DecisionTableFixtureWrapperTest {

    @BeforeEach
    fun reset() {
        LifeCycleFixture.reset()
        LifeCycleFixtureParallel.reset()
        ExtendedLifeCycleFixture.reset()
    }

    @Test
    fun disabledDecisionTableExecute() {
        val decisionTableMock = mockkJClass(DecisionTable::class.java)
        val fixtureClass = DisabledDecisionTableDocument.DisabledDecisionTableFixture::class.java
        val cut = DecisionTableFixtureWrapper(fixtureClass)

        val result = cut.execute(decisionTableMock).status

        Assertions.assertThat(result).isInstanceOf(Status.Disabled::class.java)
        Assertions.assertThat((result as Status.Disabled).reason).isEqualTo("Disabled DecisionTableFixture")
    }

    @Test
    fun `life cycle of simple parallel fixture`() {
        val input = Header("input")
        val check = Header("check")
        val headers = arrayListOf(input, check)

        val row1 = Row(mapOf(input to Field("r1i"), check to Field("r1c")))
        val row2 = Row(mapOf(input to Field("r2i"), check to Field("r2c")))
        val rows = arrayListOf(row1, row2)

        val resultTable = executeDecisionTable(DecisionTable(headers, rows), LifeCycleFixtureParallel::class.java)
        Assertions.assertThat(resultTable.status).isInstanceOf(Status.Executed::class.java)

        val fixture = LifeCycleFixtureParallel.callback
        verifyOrder {
            fixture.beforeTable()
            fixture.beforeRow()
            fixture.afterTable()
        }

        verifyOrder {
            fixture.beforeRow()
            fixture.input("r1i")
            fixture.beforeFirstCheck()
            fixture.check("r1c")
            fixture.afterRow()
        }

        verifyOrder {
            fixture.beforeRow()
            fixture.input("r2i")
            fixture.beforeFirstCheck()
            fixture.check("r2c")
            fixture.afterRow()
        }
    }

    @Test
    fun `life cycle of simple fixture`() {
        val input = Header("input")
        val check = Header("check")
        val headers = arrayListOf(input, check)

        val row1 = Row(mapOf(input to Field("r1i"), check to Field("r1c")))
        val row2 = Row(mapOf(input to Field("r2i"), check to Field("r2c")))
        val rows = arrayListOf(row1, row2)

        val resultTable = executeDecisionTable(DecisionTable(headers, rows), LifeCycleFixture::class.java)
        Assertions.assertThat(resultTable.status).isInstanceOf(Status.Executed::class.java)

        val fixture = LifeCycleFixture.callback
        verifySequence {
            fixture.beforeTable()
            fixture.beforeRow()
            fixture.input("r1i")
            fixture.beforeFirstCheck()
            fixture.check("r1c")
            fixture.afterRow()
            fixture.beforeRow()
            fixture.input("r2i")
            fixture.beforeFirstCheck()
            fixture.check("r2c")
            fixture.afterRow()
            fixture.afterTable()
        }
    }

    @Test
    fun `malformed fixtures throw a special exception class`() {
        val headers = emptyList<Header>()
        val rows = arrayListOf(Row(emptyMap()))
        val decisionTable = DecisionTable(headers, rows)
        val fixtureClass = MalformedFixtures.NoDefaultConstructor::class.java

        val tableResult = executeDecisionTable(decisionTable, fixtureClass).status
        Assertions.assertThat(tableResult).isInstanceOf(Status.Exception::class.java)

        val exception = (tableResult as Status.Exception).exception
        Assertions.assertThat(exception)
            .isInstanceOf(DecisionTableFixtureWrapper.MalformedDecisionTableFixtureException::class.java)
    }

    @Test
    fun `unmapped headers throw special exception class`() {
        val input = Header("input")
        val check = Header("check")
        val unknown = Header("unknown")

        val headers = listOf(input, check, unknown)
        val rows = emptyList<Row>()
        val decisionTable = DecisionTable(headers, rows)
        val fixtureClass = LifeCycleFixture::class.java

        val tableResult = executeDecisionTable(decisionTable, fixtureClass).status
        Assertions.assertThat(tableResult).isInstanceOf(Status.Exception::class.java)

        val exception = (tableResult as Status.Exception).exception
        Assertions.assertThat(exception).isInstanceOf(DecisionTableFixtureWrapper.UnmappedHeaderException::class.java)
        Assertions.assertThat(exception).hasMessageContaining("- unknown")

        val fixture = LifeCycleFixture.callback

        verify(exactly = 0) { fixture.beforeTable() }
    }

    @Nested
    inner class `life cycle methods can exist multiple times` {

        val callback = ExtendedLifeCycleFixture.callback!!

        @Test
        fun `before table`() {
            execute()
            verify {
                callback.beforeTable1()
                callback.beforeTable2()
            }
        }

        @Test
        fun `before row`() {
            execute()
            verify {
                callback.beforeRow1()
                callback.beforeRow2()
            }
        }

        @Test
        fun `before first check`() {
            execute()
            verify {
                callback.beforeFirstCheck1()
                callback.beforeFirstCheck2()
            }
        }

        @Test
        fun `after row`() {
            execute()
            verify {
                callback.afterRow1()
                callback.afterRow2()
            }
        }

        @Test
        fun `after table`() {
            execute()
            verify {
                callback.afterTable1()
                callback.afterTable2()
            }
        }

        private fun execute(): DecisionTableResult {
            val input = Header("input")
            val check = Header("check")
            val headers = arrayListOf(input, check)
            val row = Row(mapOf(input to Field("r1i"), check to Field("r1c")))
            val decisionTable = DecisionTable(headers, listOf(row))
            return executeDecisionTable(decisionTable, ExtendedLifeCycleFixture::class.java)
        }
    }

    @Nested
    inner class `exceptional behaviour - parallel` {

        val input = Header("input")
        val check = Header("check")
        val headers = arrayListOf(input, check)

        val row = Row(mapOf(input to Field("r1i"), check to Field("r1c")))
        val anotherRow = Row(mapOf(input to Field("r2i"), check to Field("r2c")))

        val callback = LifeCycleFixtureParallel.callback!!

        @Test
        fun `exception in one row does not effect execution of another`() {
            val exception = IllegalStateException()
            every { callback.input("r1i") } throws exception

            execute(row, anotherRow)

            verifyOrder {
                callback.beforeTable()
                callback.beforeRow()
                callback.afterTable()
            }

            verifyOrder {
                callback.beforeRow()
                callback.input("r1i")
                callback.afterRow()
            }

            verifyOrder {
                callback.beforeRow()
                callback.input("r2i")
                callback.beforeFirstCheck()
                callback.check("r2c")
                callback.afterRow()
            }
        }

        @Test
        fun `assertion error in one row does not effect execution of another`() {
            val exception = AssertionError()
            every { callback.input("r1i") } throws exception

            execute(row, anotherRow)

            verifyOrder {
                callback.beforeTable()
                callback.beforeRow()
                callback.afterTable()
            }

            verifyOrder {
                callback.beforeRow()
                callback.input("r1i")
                callback.afterRow()
            }

            verifyOrder {
                callback.beforeRow()
                callback.input("r2i")
                callback.beforeFirstCheck()
                callback.check("r2c")
                callback.afterRow()
            }
        }

        private fun execute(vararg rows: Row): DecisionTableResult {
            val decisionTable = DecisionTable(headers, rows.asList())
            return DecisionTableFixtureWrapper(LifeCycleFixtureParallel::class.java).execute(decisionTable)
        }
    }

    @Nested
    inner class `exceptional behaviour` {

        val input = Header("input")
        val check = Header("check")
        val headers = arrayListOf(input, check)

        val row = Row(mapOf(input to Field("r1i"), check to Field("r1c")))
        val anotherRow = Row(mapOf(input to Field("r2i"), check to Field("r2c")))

        val callback = LifeCycleFixture.callback!!

        @Test
        fun `exception in one row does not effect execution of another`() {
            val exception = IllegalStateException()
            every { callback.input("r1i") } throws exception

            execute(row, anotherRow)

            verifySequence {
                callback.beforeTable()
                callback.beforeRow()
                callback.input("r1i")
                callback.afterRow()
                callback.beforeRow()
                callback.input("r2i")
                callback.beforeFirstCheck()
                callback.check("r2c")
                callback.afterRow()
                callback.afterTable()
            }
        }

        @Test
        fun `assertion error in one row does not effect execution of another`() {
            val exception = AssertionError()
            every { callback.input("r1i") } throws exception

            execute(row, anotherRow)

            verifySequence {
                callback.beforeTable()
                callback.beforeRow()
                callback.input("r1i")
                callback.afterRow()
                callback.beforeRow()
                callback.input("r2i")
                callback.beforeFirstCheck()
                callback.check("r2c")
                callback.afterRow()
                callback.afterTable()
            }
        }

        @Nested
        inner class `input methods` {

            @Test
            fun `exception will set exception result for input`() {
                val exception = IllegalStateException()
                every { callback.input(ofType(String::class)) } throws exception

                val inputField = execute(row).rows[0].headerToField[input]!!

                Assertions.assertThat(inputField.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `exception will prevent checks from being executed`() {
                val exception = IllegalStateException()
                every { callback.input(ofType(String::class)) } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
                verify(exactly = 0) {
                    callback.beforeFirstCheck()
                    callback.check(ofType(String::class))
                }
            }

            @Test
            fun `assertion error will set failed result for input`() {
                val exception = AssertionError()
                every { callback.input(ofType(String::class)) } throws exception

                val inputField = execute(row).rows[0].headerToField[input]!!

                Assertions.assertThat(inputField.status).isInstanceOf(Status.Failed::class.java)
            }

            @Test
            fun `assertion error will prevent checks from being executed`() {
                val exception = AssertionError()
                every { callback.input(ofType(String::class)) } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
                verify(exactly = 0) {
                    callback.beforeFirstCheck()
                    callback.check(ofType(String::class))
                }
            }
        }

        @Nested
        inner class `check methods` {

            @Test
            fun `exception will set exception result for check`() {
                val exception = IllegalStateException()
                every { callback.check(ofType(String::class)) } throws exception

                val checkField = execute(row).rows[0].headerToField[check]!!

                Assertions.assertThat(checkField.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `exception will still execute after row methods`() {
                val exception = IllegalStateException()
                every { callback.check(ofType(String::class)) } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
            }

            @Test
            fun `assertion error will set failed result for check`() {
                val exception = AssertionError()
                every { callback.check(ofType(String::class)) } throws exception

                val checkField = execute(row).rows[0].headerToField[check]!!

                Assertions.assertThat(checkField.status).isInstanceOf(Status.Failed::class.java)
            }

            @Test
            fun `assertion error will still execute after row methods`() {
                val exception = AssertionError()
                every { callback.check(ofType(String::class)) } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
            }
        }

        @Nested
        inner class `before row methods` {

            @Test
            fun `exception will set exception result on row`() {
                val exception = IllegalStateException()
                every { callback.beforeRow() } throws exception

                val row = execute(row).rows[0]

                Assertions.assertThat(row.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `exception will set skipped result for remaining fields of row`() {
                val exception = IllegalStateException()
                every { callback.beforeRow() } throws exception

                val rowResult = execute(row).rows[0]

                Assertions.assertThat(rowResult.headerToField[input]!!.status).isInstanceOf(Status.Skipped::class.java)
                Assertions.assertThat(rowResult.headerToField[check]!!.status).isInstanceOf(Status.Skipped::class.java)
            }

            @Test
            fun `exception will prevent any further row actions from being executed`() {
                val exception = IllegalStateException()
                every { callback.beforeRow() } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
                verify(exactly = 0) {
                    callback.input(ofType(String::class))
                    callback.beforeFirstCheck()
                    callback.check(ofType(String::class))
                }
            }

            @Test
            fun `assertion error will set exception result on row`() {
                val exception = AssertionError()
                every { callback.beforeRow() } throws exception

                val row = execute(row).rows[0]

                Assertions.assertThat(row.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `assertion error will set skipped result for remaining fields of row`() {
                val exception = AssertionError()
                every { callback.beforeRow() } throws exception

                val rowResult = execute(row).rows[0]

                Assertions.assertThat(rowResult.headerToField[input]!!.status).isInstanceOf(Status.Skipped::class.java)
                Assertions.assertThat(rowResult.headerToField[check]!!.status).isInstanceOf(Status.Skipped::class.java)
            }

            @Test
            fun `assertion error will prevent any further row actions from being executed`() {
                val exception = AssertionError()
                every { callback.beforeRow() } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
                verify(exactly = 0) {
                    callback.input(ofType(String::class))
                    callback.beforeFirstCheck()
                    callback.check(ofType(String::class))
                }
            }
        }

        @Nested
        inner class `before first check methods` {

            @Test
            fun `exception will set exception result on row`() {
                val exception = IllegalStateException()
                every { callback.beforeFirstCheck() } throws exception

                val row = execute(row).rows[0]

                Assertions.assertThat(row.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `exception will set skipped result for remaining fields of row`() {
                val exception = IllegalStateException()
                every { callback.beforeFirstCheck() } throws exception

                val rowResult = execute(row).rows[0]

                Assertions.assertThat(rowResult.headerToField[check]!!.status).isInstanceOf(Status.Skipped::class.java)
            }

            @Test
            fun `exception will prevent any further row actions from being executed`() {
                val exception = IllegalStateException()
                every { callback.beforeFirstCheck() } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
                verify(exactly = 0) { callback.check(ofType(String::class)) }
            }

            @Test
            fun `assertion error will set exception result on row`() {
                val exception = AssertionError()
                every { callback.beforeFirstCheck() } throws exception

                val row = execute(row).rows[0]

                Assertions.assertThat(row.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `assertion error will set skipped result for remaining fields of row`() {
                val exception = AssertionError()
                every { callback.beforeFirstCheck() } throws exception

                val rowResult = execute(row).rows[0]

                Assertions.assertThat(rowResult.headerToField[check]!!.status).isInstanceOf(Status.Skipped::class.java)
            }

            @Test
            fun `assertion error will prevent any further row actions from being executed`() {
                val exception = AssertionError()
                every { callback.beforeFirstCheck() } throws exception

                execute(row)

                verify {
                    callback.afterRow()
                    callback.afterTable()
                }
                verify(exactly = 0) {
                    callback.check(ofType(String::class))
                }
            }
        }

        @Nested
        inner class `after row methods` {

            @Test
            fun `exception will set exception result on row`() {
                val exception = IllegalStateException()
                every { callback.afterRow() } throws exception

                val row = execute(row).rows[0]

                Assertions.assertThat(row.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `exception will prevent any further row actions from being executed`() {
                val exception = IllegalStateException()
                every { callback.afterRow() } throws exception

                execute(row)

                verify { callback.afterTable() }
            }

            @Test
            fun `assertion error will set exception result on row`() {
                val exception = AssertionError()
                every { callback.afterRow() } throws exception

                val row = execute(row).rows[0]

                Assertions.assertThat(row.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `assertion error will prevent any further row actions from being executed`() {
                val exception = AssertionError()
                every { callback.afterRow() } throws exception

                execute(row)

                verify { callback.afterTable() }
            }
        }

        @Nested
        inner class `before table methods` {

            @Test
            fun `exception will set exception result on table`() {
                val exception = IllegalStateException()
                every { callback.beforeTable() } throws exception

                val table = execute(row)

                Assertions.assertThat(table.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `exception will set skipped result for all rows`() {
                val exception = IllegalStateException()
                every { callback.beforeTable() } throws exception

                val table = execute(row, anotherRow)

                val rows = table.rows
                Assertions.assertThat(rows[0].status).isInstanceOf(Status.Skipped::class.java)
                Assertions.assertThat(rows[1].status).isInstanceOf(Status.Skipped::class.java)
            }

            @Test
            fun `exception will prevent any row being executed`() {
                val exception = IllegalStateException()
                every { callback.beforeTable() } throws exception

                execute(row, anotherRow)

                verify(exactly = 0) { callback.beforeRow() }
            }

            @Test
            fun `assertion error will set exception result on row`() {
                val exception = AssertionError()
                every { callback.beforeTable() } throws exception

                val table = execute(row)

                Assertions.assertThat(table.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `assertion error will set skipped result for all rows`() {
                val exception = AssertionError()
                every { callback.beforeTable() } throws exception

                val table = execute(row, anotherRow)

                val rows = table.rows
                Assertions.assertThat(rows[0].status).isInstanceOf(Status.Skipped::class.java)
                Assertions.assertThat(rows[1].status).isInstanceOf(Status.Skipped::class.java)
            }

            @Test
            fun `assertion error will prevent any row being executed`() {
                val exception = AssertionError()
                every { callback.beforeTable() } throws exception

                execute(row, anotherRow)

                verify(exactly = 0) { callback.beforeRow() }
            }
        }

        @Nested
        inner class `after table methods` {

            @Test
            fun `exception will set exception result on table`() {
                val exception = IllegalStateException()
                every { callback.afterTable() } throws exception

                val table = execute(row)

                Assertions.assertThat(table.status).isInstanceOf(Status.Exception::class.java)
            }

            @Test
            fun `assertion error will set exception result on row`() {
                val exception = AssertionError()
                every { callback.afterTable() } throws exception

                val table = execute(row)

                Assertions.assertThat(table.status).isInstanceOf(Status.Exception::class.java)
            }
        }

        private fun execute(vararg rows: Row): DecisionTableResult {
            val decisionTable = DecisionTable(headers, rows.asList())
            return DecisionTableFixtureWrapper(LifeCycleFixture::class.java).execute(decisionTable)
        }
    }

    @Test
    fun `smoke test of good case fixture`() {

        val valueAColumn = Header("a")
        val valueBColumn = Header("b")
        val sumColumn = Header("a + b = ?")
        val diffColumn = Header("a - b = ?")
        val multiplyColumn = Header("a * b = ?")
        val divideColumn = Header("a / b = ?")
        val headers = arrayListOf(valueAColumn, valueBColumn, sumColumn, diffColumn, multiplyColumn, divideColumn)

        val rows = arrayListOf(
            Row(
                mapOf(
                    valueAColumn to Field("0"),
                    valueBColumn to Field("0"),
                    sumColumn to Field("0"),
                    diffColumn to Field("0"),
                    multiplyColumn to Field("0"),
                    divideColumn to Field("NaN")
                )
            ),
            Row(
                mapOf(
                    valueAColumn to Field("1"),
                    valueBColumn to Field("0"),
                    sumColumn to Field("1"),
                    diffColumn to Field("1"),
                    multiplyColumn to Field("0"),
                    divideColumn to Field("Infinity")
                )
            ),
            Row(
                mapOf(
                    valueAColumn to Field("0"),
                    valueBColumn to Field("1"),
                    sumColumn to Field("1"),
                    diffColumn to Field("-1"),
                    multiplyColumn to Field("0"),
                    divideColumn to Field("0")
                )
            ),
            Row(
                mapOf(
                    valueAColumn to Field("1"),
                    valueBColumn to Field("1"),
                    sumColumn to Field("2"),
                    diffColumn to Field("0"),
                    multiplyColumn to Field("1"),
                    divideColumn to Field("1")
                )
            )
        )

        val resultTable = executeDecisionTable(DecisionTable(headers, rows), CalculatorDecisionTableFixture::class.java)

        Assertions.assertThat(resultTable.status).isEqualTo(Status.Executed)
        Assertions.assertThat(resultTable.rows).hasSize(4)
        resultTable.rows.forEach { (headerToField, result) ->
            Assertions.assertThat(result).isEqualTo(Status.Executed)
            headerToField.values.forEach { field ->
                Assertions.assertThat(field.status).isEqualTo(Status.Executed)
            }
        }
    }

    private fun executeDecisionTable(decisionTable: DecisionTable, fixtureClass: Class<*>): DecisionTableResult {
        return DecisionTableFixtureWrapper(fixtureClass).execute(decisionTable)
    }
}
