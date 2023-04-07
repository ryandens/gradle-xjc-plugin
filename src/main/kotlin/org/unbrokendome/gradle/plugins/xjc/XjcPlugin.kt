package org.unbrokendome.gradle.plugins.xjc

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GUtil
import org.gradle.util.GradleVersion
import org.unbrokendome.gradle.plugins.xjc.internal.GRADLE_VERSION_6_1
import org.unbrokendome.gradle.plugins.xjc.internal.MIN_REQUIRED_GRADLE_VERSION
import org.unbrokendome.gradle.plugins.xjc.internal.SerializableResolvedArtifact


class XjcPlugin : Plugin<Project> {

    companion object {

        @JvmStatic
        val XJC_EXTENSION_NAME = "xjc"

        @JvmStatic
        val XJC_TOOL_CONFIGURATION_NAME = "xjcTool"

        @JvmStatic
        val XJC_GLOBAL_CLASSPATH_CONFIGURATION_NAME = "xjcClasspathGlobal"

        @JvmStatic
        val XJC_GLOBAL_CATALOG_RESOLUTION_CONFIGURATION_NAME = "xjcCatalogResolutionGlobal"

        private val DefaultXjcToolDependenciesByVersion = mapOf(
            "2.2" to listOf(
                "com.sun.xml.bind:jaxb-xjc:2.2.11",
                "com.sun.xml.bind:jaxb-core:2.2.11",
                "com.sun.xml.bind:jaxb-impl:2.2.11",
                "javax.xml.bind:jaxb-api:2.2.11"
            ),
            "2.3" to listOf(
                "com.sun.xml.bind:jaxb-xjc:2.3.3"
            ),
            "3.0" to listOf(
                "com.sun.xml.bind:jaxb-xjc:3.0.0-M4",
                "com.sun.xml.bind:jaxb-impl:3.0.0-M4"
            )
        )
    }


    override fun apply(project: Project) {

        check(GradleVersion.current() >= MIN_REQUIRED_GRADLE_VERSION) {
            "The org.unbroken-dome.xjc plugin requires Gradle $MIN_REQUIRED_GRADLE_VERSION or higher."
        }

        val xjcExtension = project.createXjcExtension()
        project.extensions.add(XjcExtension::class.java, XJC_EXTENSION_NAME, xjcExtension)

        val toolClasspathConfiguration = project.createInternalConfiguration(XJC_TOOL_CONFIGURATION_NAME) {
            defaultDependencies { deps ->
                deps.addAll(project.defaultXjcDependencies(xjcExtension.xjcVersion.get()))
            }
        }
        val globalXjcClasspathConfiguration = project.createInternalConfiguration(
            XJC_GLOBAL_CLASSPATH_CONFIGURATION_NAME
        )
        val globalCatalogResolutionConfiguration = project.createInternalConfiguration(
            XJC_GLOBAL_CATALOG_RESOLUTION_CONFIGURATION_NAME
        )

        project.tasks.withType(XjcGenerate::class.java) { task ->
            (task as XjcGeneratorOptions).setFrom(xjcExtension)
            task.toolClasspath.setFrom(toolClasspathConfiguration)
            task.extraArgs.addAll(xjcExtension.extraArgs)
        }

        project.plugins.withType(JavaBasePlugin::class.java) {

            val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)
            sourceSets.all { sourceSet ->

                val xjcSourceSetConvention = project.objects.newInstance(
                    XjcSourceSetConvention::class.java, sourceSet
                )


                val xjcSourceSetExtension = sourceSet.extensions.create(XJC_EXTENSION_NAME, XjcSourceSetExtension::class.java, project.objects)
                val displayName = GUtil.toWords(sourceSet.name)
                val xjcSchema = project.objects.sourceDirectorySet("xjcSchema", "$displayName XJC schema").apply {
                    include("**/*.xsd")
                }

                val xjcBinding = project.objects.sourceDirectorySet("xjcBinding", "$displayName XJC binding").apply {
                    include("**/*.xjb")
                }

                val xjcUrl = project.objects.sourceDirectorySet("xjcUrl", "$displayName XJC schema URLs").apply {
                    include("**/*.url")
                }

                val xjcCatalog = project.objects.sourceDirectorySet("xjcCatalog", "$displayName XJC catalogs").apply {
                    include("**/*.cat")
                }


                listOf(xjcSchema, xjcBinding, xjcUrl, xjcCatalog).forEach { sourceDirSet ->
                    sourceDirSet.srcDir(project.layout.projectDirectory.dir("src/${sourceSet.name}").dir(xjcExtension.srcDirName))

                }
                with(sourceSet.allSource) {
                    source(xjcSchema)
                    source(xjcBinding)
                    source(xjcUrl)
                    source(xjcCatalog)
                }

                xjcSourceSetExtension.xjcSchema.set(xjcSchema)
                xjcSourceSetExtension.xjcBinding.set(xjcBinding)
                xjcSourceSetExtension.xjcUrl.set(xjcUrl)
                xjcSourceSetExtension.xjcCatalog.set(xjcCatalog)
                xjcSourceSetExtension.xjcGenerateTaskName.set("xjcGenerate" +
                        if (sourceSet.name == SourceSet.MAIN_SOURCE_SET_NAME) "" else sourceSet.name.capitalize())
                xjcSourceSetExtension.xjcClasspathConfigurationName.set(sourceSetSpecificConfigurationName("xjcClasspath", sourceSet))
                xjcSourceSetExtension.xjcEpisodesConfigurationName.set(sourceSetSpecificConfigurationName("xjcEpisodes", sourceSet))
                xjcSourceSetExtension.xjcCatalogResolutionConfigurationName.set(sourceSetSpecificConfigurationName("xjcCatalogResolution", sourceSet))




                (sourceSet as HasConvention).convention.plugins[XJC_EXTENSION_NAME] = xjcSourceSetConvention

                val xjcClasspathConfiguration = project.createInternalConfiguration(
                        xjcSourceSetExtension.xjcClasspathConfigurationName.get()
                ) {
                    extendsFrom(globalXjcClasspathConfiguration)
                }

                val catalogResolutionConfiguration = project.createInternalConfiguration(
                        xjcSourceSetExtension.xjcCatalogResolutionConfigurationName.get()
                ) {
                    extendsFrom(
                        globalCatalogResolutionConfiguration,
                        project.configurations.getByName(sourceSet.compileClasspathConfigurationName)
                    )
                }

                val episodesConfiguration = project.createInternalConfiguration(
                        xjcSourceSetExtension.xjcEpisodesConfigurationName.get()
                )

                val generateTask = project.tasks.register(
                        xjcSourceSetExtension.xjcGenerateTaskName.get(), XjcGenerate::class.java
                ) { task ->
                    task.source.setFrom(xjcSourceSetExtension.xjcSchema)
                    task.bindingFiles.setFrom(xjcSourceSetExtension.xjcBinding)
                    task.urlSources.setFrom(xjcSourceSetExtension.xjcUrl)
                    task.catalogs.setFrom(xjcSourceSetExtension.xjcCatalog)

                    task.pluginClasspath.setFrom(xjcClasspathConfiguration)

                    task.catalogSerializableResolvedArtifact.set(project.provider { catalogResolutionConfiguration.resolvedConfiguration.lenientConfiguration.artifacts.map { SerializableResolvedArtifact(it) } })

                    task.episodes.setFrom(episodesConfiguration)

                    task.targetPackage.set(xjcSourceSetExtension.xjcTargetPackage)
                    task.generateEpisode.set(xjcSourceSetExtension.xjcGenerateEpisode)
                    task.extraArgs.addAll(xjcSourceSetExtension.xjcExtraArgs)

                    task.outputDirectory.set(
                        project.layout.buildDirectory.dir("generated/sources/xjc/java/${sourceSet.name}")
                    )
                    task.episodeOutputDirectory.set(
                        project.layout.buildDirectory.dir("generated/resources/xjc/${sourceSet.name}")
                    )
                }

                val xjcOutputDir = generateTask.flatMap { it.outputDirectory }

                if (GradleVersion.current() >= GRADLE_VERSION_6_1) {
                    xjcSchema.destinationDirectory.set(xjcOutputDir)
                    sourceSet.java.srcDir(xjcOutputDir)
                } else {
                    xjcSchema.destinationDirectory.set(
                        project.file(generateTask.flatMap { it.outputDirectory }))
                    sourceSet.java.srcDir(xjcOutputDir)
                }

                sourceSet.resources.srcDir(
                    generateTask.flatMap { it.episodeOutputDirectory }
                )
            }
        }
    }

    private fun sourceSetSpecificConfigurationName(name: String, sourceSet: SourceSet) =
            if (sourceSet.name == SourceSet.MAIN_SOURCE_SET_NAME) name else "${sourceSet.name}${name.capitalize()}"


    private fun Project.defaultXjcDependencies(xjcVersion: String): List<Dependency> =
        checkNotNull(DefaultXjcToolDependenciesByVersion[xjcVersion]) {
            "Invalid XJC version: \"$version\". Valid values are: ${DefaultXjcToolDependenciesByVersion.keys}"
        }.map { notation ->
            project.dependencies.create(notation)
        }


    private fun Project.createInternalConfiguration(
        name: String, configureAction: Configuration.() -> Unit = { }
    ) =
        configurations.create(name) { configuration ->
            configuration.isVisible = false
            configuration.configureAction()
        }
}
