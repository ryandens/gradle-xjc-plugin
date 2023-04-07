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




}
