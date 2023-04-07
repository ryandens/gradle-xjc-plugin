package org.unbrokendome.gradle.plugins.xjc

import assertk.all
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.unbrokendome.gradle.plugins.xjc.spek.applyPlugin
import org.unbrokendome.gradle.plugins.xjc.spek.setupGradleProject
import org.unbrokendome.gradle.plugins.xjc.testlib.directory
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

        val xjcSourceSetExtension by memoized {
            mainSourceSet.requiredExtension<XjcSourceSetExtension>()
        }


        it("should return the correct task and configuration names") {
            assertThat(xjcSourceSetExtension).all {
                prop(XjcSourceSetExtension::xjcGenerateTaskName)
                    .isEqualTo("xjcGenerate")
                prop(XjcSourceSetExtension::xjcClasspathConfigurationName)
                    .isEqualTo("xjcClasspath")
                prop(XjcSourceSetExtension::xjcEpisodesConfigurationName)
                    .isEqualTo("xjcEpisodes")
                prop(XjcSourceSetExtension::xjcCatalogResolutionConfigurationName)
                    .isEqualTo("xjcCatalogResolution")
            }
        }
    }


    describe("custom source set") {

        val sourceSet by memoized {
            project.sourceSets.create("custom")
        }

        val xjcSourceSetExtension by memoized {
            sourceSet.requiredExtension<XjcSourceSetExtension>()
        }


        it("should return the correct task and configuration names") {
            assertThat(xjcSourceSetExtension).all {
                prop(XjcSourceSetExtension::xjcGenerateTaskName)
                    .isEqualTo("xjcGenerateCustom")
                prop(XjcSourceSetExtension::xjcClasspathConfigurationName)
                    .isEqualTo("customXjcClasspath")
                prop(XjcSourceSetExtension::xjcEpisodesConfigurationName)
                    .isEqualTo("customXjcEpisodes")
                prop(XjcSourceSetExtension::xjcCatalogResolutionConfigurationName)
                    .isEqualTo("customXjcCatalogResolution")
            }
        }


        it("should set default include filters") {

            assertThat(xjcSourceSetExtension).all {
                prop("xjcSchema") { it.xjcSchema.get() }
                        .prop("includes") { it.includes }
                        .containsOnly("**/*.xsd")
                prop("xjcBinding") { it.xjcBinding.get() }
                        .prop("includes") { it.includes }
                        .containsOnly("**/*.xjb")
                prop("xjcUrl") { it.xjcUrl.get() }
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
                prop("xjcBinding") { it.xjcBinding.get() }
                        .prop("srcDirs") { it.srcDirs }
                        .containsOnly(project.file("src/custom/xjc"))
                prop("xjcUrl") { it.xjcUrl.get() }
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
