package org.unbrokendome.gradle.plugins.xjc

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet

abstract class XjcSourceSetExtension(objectFactory: ObjectFactory) {

    val xjcSchema: Property<SourceDirectorySet> = objectFactory.property(SourceDirectorySet::class.java)

    val xjcBinding: Property<SourceDirectorySet> = objectFactory.property(SourceDirectorySet::class.java)

    val xjcUrl: Property<SourceDirectorySet> = objectFactory.property(SourceDirectorySet::class.java)

    val xjcCatalog: Property<SourceDirectorySet> = objectFactory.property(SourceDirectorySet::class.java)

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
    val xjcTargetPackage: Property<String> = objectFactory.property(String::class.java)


    /**
     * If `true`, instructs XJC to generate an episode file at `META-INF/sun-jaxb.episode`.
     * The generated episode file will then be included in the [resources][SourceSet.resources] of this source set.
     *
     * The default is `false`.
     *
     * @see XjcGenerate.generateEpisode
     */
    val xjcGenerateEpisode: Property<Boolean> = objectFactory.property(Boolean::class.java)


    /**
     * Additional arguments to be passed to XJC for this source set.
     *
     * These extra arguments will be added after any extra arguments specified via [XjcExtension.extraArgs]
     * in the global `xjc` block.
     *
     * @see XjcExtension.extraArgs
     * @see XjcGenerate.extraArgs
     */
    val xjcExtraArgs: ListProperty<String> = objectFactory.listProperty(String::class.java)

    val xjcGenerateTaskName: Property<String> = objectFactory.property(String::class.java)

    val xjcClasspathConfigurationName: Property<String> = objectFactory.property(String::class.java)

    val xjcEpisodesConfigurationName: Property<String> = objectFactory.property(String::class.java)

    val xjcCatalogResolutionConfigurationName: Property<String> = objectFactory.property(String::class.java)


    init {
        xjcGenerateEpisode.convention(false)
    }
}