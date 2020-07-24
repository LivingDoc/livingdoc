package org.livingdoc.engine.execution.examples.decisiontables;

import org.livingdoc.api.After;
import org.livingdoc.api.Before;
import org.livingdoc.api.fixtures.decisiontables.*;

@DecisionTableFixture
public class MalformedFixtures {

    public static class NoDefaultConstructor {

        public NoDefaultConstructor(String fooBar) {
        }

    }

    public static class BeforeTableWithParameter {

        @Before
        static void beforeTable(String param) {
        }

    }

    public static class NonStaticBeforeTable {

        @Before
        void beforeTable() {
        }

    }

    public static class BeforeRowWithParameter {

        @BeforeRow
        void beforeRow(String param) {
        }

    }

    public static class StaticBeforeRow {

        @BeforeRow
        static void beforeRow() {
        }

    }

    public static class InputFieldsWithSameAlias {

        @Input("input")
        String field1;

        @Input("input")
        String field2;

    }

    public static class InputFieldAndMethodWithSameAlias {

        @Input("input")
        String field;

        @Input("input")
        void method(String value) {
        }

    }

    public static class InputMethodsWithSameAlias {

        @Input("input")
        void method1(String value) {
        }

        @Input("input")
        void method2(String value) {
        }
    }

    public static class InputMethodWithoutParameter {

        @Input("input")
        void input() {
        }

    }

    public static class StaticInputMethod {

        @Input("input")
        static void input(String param) {
        }

    }

    public static class InputAndCheckMethodsWithSameAlias {

        @Input("name")
        void method1(String value) {
        }

        @Check("name")
        void method2(String value) {
        }

    }

    public static class InputFieldAndCheckMethodWithSameAlias {

        @Input("name")
        String value;

        @Check("name")
        void method2(String value) {
        }

    }

    public static class BeforeFirstCheckWithParameter {

        @BeforeFirstCheck
        void beforeFirstCheck(String param) {
        }

    }

    public static class StaticBeforeFirstCheck {

        @BeforeFirstCheck
        static void beforeFirstCheck() {
        }

    }

    public static class CheckMethodsWithSameAlias {

        @Check("check")
        void method1(String value) {
        }

        @Check("check")
        void method2(String value) {
        }
    }

    public static class CheckMethodWithoutParameter {

        @Check("check")
        void check() {
        }

    }

    public static class StaticCheckMethod {

        @Check("check")
        static void check(String param) {
        }

    }

    public static class AfterRowWithParameter {

        @AfterRow
        void afterRow(String param) {
        }

    }

    public static class StaticAfterRow {

        @AfterRow
        static void afterRow() {
        }

    }

    public static class AfterTableWithParameter {

        @After
        static void afterTable(String param) {
        }

    }

    public static class NonStaticAfterTable {

        @After
        void afterTable() {
        }

    }

}
