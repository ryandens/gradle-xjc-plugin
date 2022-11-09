package org.unbrokendome.gradle.plugins.xjc

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import org.gradle.testkit.runner.GradleRunner
import org.junit.platform.commons.annotation.Testable
import org.unbrokendome.gradle.plugins.xjc.samples.TestEachDslFlavor
import org.unbrokendome.gradle.plugins.xjc.samples.UseSampleProject
import org.unbrokendome.gradle.plugins.xjc.testutil.assertions.isSuccess
import org.unbrokendome.gradle.plugins.xjc.testutil.assertions.isUpToDate
import org.unbrokendome.gradle.plugins.xjc.testutil.assertions.output
import org.unbrokendome.gradle.plugins.xjc.testutil.assertions.task
import org.unbrokendome.gradle.plugins.xjc.testutil.runGradle


@UseSampleProject("basic")
class ConfigurationCacheIntegrationTest : AbstractBasicIntegrationTest() {

    @TestEachDslFlavor
    @Testable
    fun test(runner: GradleRunner) {

        val firstBuildResult = runner.runGradle("--configuration-cache", "build")

        assertThat(firstBuildResult).all {
            output().contains("Configuration cache entry stored.")
            task(":xjcGenerate").isSuccess()
            task(":build").isSuccess()
        }

        val secondBuildResult = runner.runGradle("--configuration-cache", "build")

        assertThat(secondBuildResult).all {
            output().contains("Configuration cache entry reused.")
            task(":xjcGenerate").isUpToDate()
            task(":build").isUpToDate()
        }

    }
}
