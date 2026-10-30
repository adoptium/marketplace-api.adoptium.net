package net.adoptium.api.marketplace.parser.maven

import net.adoptium.marketplace.server.frontend.versions.VersionParser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Test
import java.util.stream.Stream

class VersionRangeTest {

    @Test
    fun `matches exact extended versions`() {
        val matcher = VersionRange.parse("25.0.5.0.2+1")
        Assertions.assertTrue(matcher.test(VersionParser.parse("25.0.5.0.2+1")))
        Assertions.assertFalse(matcher.test(VersionParser.parse("25.0.5.0.3+1")))
    }

    @Test
    fun `compares extended range boundaries numerically`() {
        val matcher = VersionRange.parse("[25.0.5.0.2+1,25.0.5.0.10+1)")
        Assertions.assertTrue(matcher.test(VersionParser.parse("25.0.5.0.2+1")))
        Assertions.assertTrue(matcher.test(VersionParser.parse("25.0.5.0.3+1")))
        Assertions.assertFalse(matcher.test(VersionParser.parse("25.0.5.0.1+1")))
        Assertions.assertFalse(matcher.test(VersionParser.parse("25.0.5.0.10+1")))
    }

    @TestFactory
    fun badRangeThrows(): Stream<DynamicTest> {
        return listOf(
            "",
            "8]",
            "[8",
            "[]",
            "foo",
            "[foo]",
            "foo",
            "[8.0.1]foo",
            "foo[8.0.1]",
        ).map {
            DynamicTest.dynamicTest("test $it") {
                Assertions.assertThrows(VersionRange.InvalidVersionRange::class.java) {
                    VersionRange.parse(it)
                }
            }
        }
            .stream()
    }

    @TestFactory
    fun shouldContain(): Stream<DynamicTest> {
        return listOf(
            "8.0.1",

            "[8.0.1]",

            "[8.0.1,]",
            "[,8.0.1]",
            "[8.0.1,8.0.3]",

            "[8.0.1,)",
            "[,8.0.2)",
            "[8,9)",

            "(8,]",
            "(,8.0.1]",
            "(8,9]",

            "(8,)",
            "(,8.0.3)",
            "(8,8.0.3)",
        ).map {
            DynamicTest.dynamicTest(it) {
                Assertions.assertTrue(VersionRange.parse(it).test(VersionParser.parse("8.0.1")))
            }
        }
            .stream()
    }

    @TestFactory
    fun shouldNotContain(): Stream<DynamicTest> {
        return listOf(
            "8.0.1",

            "(8.0.3)",
            "[8.0.1]",

            "[8.0.4,]",
            "[,8.0.2]",
            "[8.0.4,8.0.4]",

            "[8.0.4,)",
            "[,8.0.3)",
            "[8,8.0.3)",

            "(8.0.3,]",
            "(,8.0.2]",
            "(8,8.0.2]",

            "(8.0.3,)",
            "(,8.0.3)",
            "(8.0.3,8.0.3)",
        ).map {
            DynamicTest.dynamicTest(it) {
                Assertions.assertFalse(VersionRange.parse(it).test(VersionParser.parse("8.0.3")))
            }
        }
            .stream()
    }
}
