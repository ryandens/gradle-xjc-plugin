@file:Suppress("unused")

package org.gradle.kotlin.dsl

import org.gradle.api.internal.HasConvention
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.unbrokendome.gradle.plugins.xjc.XjcExtension
import org.unbrokendome.gradle.plugins.xjc.XjcSourceSetConvention


private val SourceSet.xjcConvention: XjcSourceSetConvention
    get() = (this as HasConvention).convention.getPlugin(XjcSourceSetConvention::class.java)


/**
 * The target package for XJC.
 *
 * When you specify a target package with this command-line option, it overrides any binding customization for the
 * package name and the default package name algorithm defined in the specification.
 *
 * Corresponds to the `-p` command line option.
 *
 * @see XjcSourceSetConvention.xjcTargetPackage
 */
val SourceSet.xjcTargetPackage: Property<String>
    get() = xjcConvention.xjcTargetPackage


/**
 * If `true`, instructs XJC to generate an episode file at `META-INF/sun-jaxb.episode`.
 * The generated episode file will then be included in the [resources][SourceSet.resources] of this source set.
 *
 * The default is `false`.
 *
 * @see XjcSourceSetConvention.xjcGenerateEpisode
 */
val SourceSet.xjcGenerateEpisode: Property<Boolean>
    get() = xjcConvention.xjcGenerateEpisode


/**
 * Additional arguments to be passed to XJC for this source set.
 *
 * These extra arguments will be added after any extra arguments specified via [XjcExtension.extraArgs]
 * in the global `xjc` block.
 *
 * @see XjcSourceSetConvention.xjcExtraArgs
 * @see XjcExtension.extraArgs
 */
val SourceSet.xjcExtraArgs: ListProperty<String>
    get() = xjcConvention.xjcExtraArgs
