/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.builder;

import java.util.Collections;
import java.util.List;

/**
 * Builds the skeleton of the Groovy class. (Static) Imports, class definition,....
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class GroovyClassBuilder implements ClassBuilder, DefaultClassBuilder {

	private final DefaultClassMetadata metaData;

	private List<Imports> imports = Collections.emptyList();

	private List<Imports> staticImports = Collections.emptyList();

	private List<ClassAnnotation> annotations = Collections.emptyList();

	private GroovyClassBuilder(DefaultClassMetadata metaData) {
		this.metaData = metaData;
	}

	public static GroovyClassBuilder builder(DefaultClassMetadata metaData) {
		return new GroovyClassBuilder(metaData);
	}

	@Override
	public ClassBuilder setupImports(List<Imports> imports) {
		this.imports = imports;
		return this;
	}

	@Override
	public ClassBuilder setStaticImports(List<Imports> staticImports) {
		this.staticImports = staticImports;
		return this;
	}

	@Override
	public ClassBuilder setupAnnotations(List<ClassAnnotation> annotations) {
		this.annotations = annotations;
		return this;
	}

	@Override
	public ClassMetaData build() {
		// package com.example
		blockBuilder().addLineWithEnding("package "
				+ this.metaData.generatedClassMetaData().generatedClassData.classPackage);
		// \n
		blockBuilder().addEmptyLine();
		// import ... \n
		addImports(this.imports, true);
		// import static ... \n
		addStaticImports(this.staticImports);
		// @Test ... \n
		addAnnotations(this.annotations);
		// @formatter:off
		// public class FooSpec extends Parent
		blockBuilder().append(this.metaData::modifier)
				.addAtTheEndIfEndsWithAChar(" ")
				// class
				.append("class")
				// Foo
				.appendWithSpace(this.metaData::className)
				// Spec
				.append(this.metaData::suffix)
				// extends Parent
				.appendWithSpace(this.metaData::parentClass);
		// @formatter:on
		return this.metaData;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.metaData.blockBuilder();
	}

}
