package org.unbrokendome.gradle.plugins.xjc

import assertk.all
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.unbrokendome.gradle.plugins.xjc.spek.applyPlugin
import org.unbrokendome.gradle.plugins.xjc.spek.setupGradleProject
import org.unbrokendome.gradle.plugins.xjc.testlib.directory
import org.unbrokendome.gradle.plugins.xjc.testutil.requiredConvention
import org.unbrokendome.gradle.plugins.xjc.testutil.requiredExtension
import org.unbrokendome.gradle.plugins.xjc.testutil.sourceSets


object XjcSourceSetConventionTest : Spek({

    val project by setupGradleProject {
        applyPlugin<JavaPlugin>()
        applyPlugin<XjcPlugin>()
    }


    describe("main source set") {

        val mainSourceSet by memoized {
            project.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        }

        val xjcSourceSetConvention by memoized {
            (mainSourceSet as HasConvention).requiredConvention<XjcSourceSetConvention>()
        }


        it("should return the correct task and configuration names") {
            assertThat(xjcSourceSetConvention).all {
                prop(XjcSourceSetConvention::xjcGenerateTaskName)
                    .isEqualTo("xjcGenerate")
                prop(XjcSourceSetConvention::xjcClasspathConfigurationName)
                    .isEqualTo("xjcClasspath")
                prop(XjcSourceSetConvention::xjcEpisodesConfigurationName)
                    .isEqualTo("xjcEpisodes")
                prop(XjcSourceSetConvention::xjcCatalogResolutionConfigurationName)
                    .isEqualTo("xjcCatalogResolution")
            }
        }
    }


    describe("custom source set") {

        val sourceSet by memoized {
            project.sourceSets.create("custom")
        }

        val xjcSourceSetConvention by memoized {
            (sourceSet as HasConvention).requiredConvention<XjcSourceSetConvention>()
        }

        val xjcSourceSetExtension by memoized {
            sourceSet.requiredExtension<XjcSourceSetExtension>()
        }


        it("should return the correct task and configuration names") {
            assertThat(xjcSourceSetConvention).all {
                prop(XjcSourceSetConvention::xjcGenerateTaskName)
                    .isEqualTo("xjcGenerateCustom")
                prop(XjcSourceSetConvention::xjcClasspathConfigurationName)
                    .isEqualTo("customXjcClasspath")
                prop(XjcSourceSetConvention::xjcEpisodesConfigurationName)
                    .isEqualTo("customXjcEpisodes")
                prop(XjcSourceSetConvention::xjcCatalogResolutionConfigurationName)
                    .isEqualTo("customXjcCatalogResolution")
            }
        }


        it("should set default include filters") {

            assertThat(xjcSourceSetExtension).all {
                prop("xjcSchema") { it.xjcSchema.get() }
                        .prop("includes") { it.includes }
                        .containsOnly("**/*.xsd")
            }
            assertThat(xjcSourceSetConvention).all {
                prop("xjcBinding") { it.xjcBinding }
                    .prop("includes") { it.includes }
                    .containsOnly("**/*.xjb")
                prop("xjcUrl") { it.xjcUrl }
                    .prop("includes") { it.includes }
                    .containsOnly("**/*.url")
            }
        }


        it("should honor the global xjcSrcDirName") {
            val xjc = project.requiredExtension<XjcExtension>()
            xjc.srcDirName.set("xjc")

            assertThat(xjcSourceSetExtension).all {
                prop("xjcSchema") { it.xjcSchema.get() }
                        .prop("srcDirs") { it.srcDirs }
                        .containsOnly(project.file("src/custom/xjc"))
            }

            assertThat(xjcSourceSetConvention).all {
                prop("xjcBinding") { it.xjcBinding }
                    .prop("srcDirs") { it.srcDirs }
                    .containsOnly(project.file("src/custom/xjc"))
                prop("xjcUrl") { it.xjcUrl }
                    .prop("srcDirs") { it.srcDirs }
                    .containsOnly(project.file("src/custom/xjc"))
            }
        }


        it("should combine all source types in allSource") {
            directory(project.projectDir) {
                directory("src/custom/schema") {
                    file(name = "schema.xsd", contents = "")
                    file(name = "binding.xjb", contents = "")
                    file(name = "externals.url", contents = "")
                    file(name = "catalog.cat", contents = "")
                }
            }

            assertThat(sourceSet)
                .prop("allSource") { it.allSource }
                .containsAll(
                    project.file("src/custom/schema/schema.xsd"),
                    project.file("src/custom/schema/binding.xjb"),
                    project.file("src/custom/schema/externals.url"),
                    project.file("src/custom/schema/catalog.cat")
                )
        }
    }
})
