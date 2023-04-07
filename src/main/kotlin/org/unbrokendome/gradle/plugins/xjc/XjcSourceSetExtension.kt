package org.unbrokendome.gradle.plugins.xjc

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class XjcSourceSetExtension(objectFactory: ObjectFactory) {

    val xjcSchema: Property<SourceDirectorySet> = objectFactory.property(SourceDirectorySet::class.java)
}