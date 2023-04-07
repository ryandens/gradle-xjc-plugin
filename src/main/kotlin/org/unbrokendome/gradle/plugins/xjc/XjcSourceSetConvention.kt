package org.unbrokendome.gradle.plugins.xjc

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.util.ConfigureUtil
import org.gradle.util.GUtil
import javax.inject.Inject


/**
 * Mixed into each [SourceSet] when the [XjcPlugin] is applied.
 */
abstract class XjcSourceSetConvention
@Inject internal constructor(
    private val sourceSet: SourceSet,
) {


    /**
     * The name of the [XjcGenerate] task that will perform the XJC generation for this source set.
     */
    val xjcGenerateTaskName: String
        get() = "xjcGenerate" +
                if (sourceSet.name == SourceSet.MAIN_SOURCE_SET_NAME) "" else sourceSet.name.capitalize()


    /**
     * The name of the XJC classpath configuration for this source set. This configuration should be used for
     * adding plugins to the XJC generation step.
     *
     * Any dependencies added to the XJC classpath configuration will be passed to XJC using the `-classpath`
     * CLI option.
     */
    val xjcClasspathConfigurationName: String
        get() = sourceSetSpecificConfigurationName("xjcClasspath")


    /**
     * The name of the XJC episodes configuration for this source set.
     */
    val xjcEpisodesConfigurationName: String
        get() = sourceSetSpecificConfigurationName("xjcEpisodes")


    /**
     * The name of the catalog resolution configuration for this source set. This configuration should be used for
     * artifacts containing other schemas that need to be resolved from a catalog.
     */
    val xjcCatalogResolutionConfigurationName: String
        get() = sourceSetSpecificConfigurationName("xjcCatalogResolution")


    private fun sourceSetSpecificConfigurationName(name: String) =
        if (sourceSet.name == SourceSet.MAIN_SOURCE_SET_NAME) name else "${sourceSet.name}${name.capitalize()}"
}
