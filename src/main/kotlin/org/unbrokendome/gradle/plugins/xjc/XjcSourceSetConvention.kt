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
     * The target package for XJC.
     *
     * When you specify a target package with this command-line option, it overrides any binding customization for the
     * package name and the default package name algorithm defined in the specification.
     *
     * Corresponds to the `-p` command line option.
     *
     * @see XjcGenerate.targetPackage
     */
    abstract val xjcTargetPackage: Property<String>


    /**
     * If `true`, instructs XJC to generate an episode file at `META-INF/sun-jaxb.episode`.
     * The generated episode file will then be included in the [resources][SourceSet.resources] of this source set.
     *
     * The default is `false`.
     *
     * @see XjcGenerate.generateEpisode
     */
    abstract val xjcGenerateEpisode: Property<Boolean>


    /**
     * Additional arguments to be passed to XJC for this source set.
     *
     * These extra arguments will be added after any extra arguments specified via [XjcExtension.extraArgs]
     * in the global `xjc` block.
     *
     * @see XjcExtension.extraArgs
     * @see XjcGenerate.extraArgs
     */
    abstract val xjcExtraArgs: ListProperty<String>


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


    init {

        @Suppress("LeakingThis")
        xjcGenerateEpisode.convention(false)
    }
}
