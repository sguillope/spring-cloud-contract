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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the skeleton of the Java class. (Static) Imports, class definition,....
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
interface DefaultClassBuilder extends ClassBuilder {

	BlockBuilder blockBuilder();

	default void addImports(List<Imports> imports, boolean separated) {
		List<Imports> elements = imports.stream().filter(Acceptor::accept)
				.collect(Collectors.toList());
		if (!elements.isEmpty()) {
			// @formatter:off
			elements.forEach(i -> i.apply(this));
			// @formatter:on
			if (separated) {
				blockBuilder().addEmptyLine();
			}
		}
	}

	default void addStaticImports(List<Imports> staticImports) {
		List<Imports> elements = staticImports.stream().filter(Acceptor::accept)
				.collect(Collectors.toList());
		if (!elements.isEmpty()) {
			// @formatter:off
			elements.forEach(imports -> imports.apply(this));
			// @formatter:on
			blockBuilder().addEmptyLine();
		}
	}

	default void addAnnotations(List<ClassAnnotation> annotations) {
		annotations.stream().filter(Acceptor::accept).forEach(
				classAnnotation -> blockBuilder().addLine(classAnnotation.annotation()));
	}

	default ClassBuilder writeImport(String fullyQualifiedName) {
		blockBuilder().addLineWithEnding("import " + fullyQualifiedName);
		return this;
	}

	default ClassBuilder writeStaticImport(String fullyQualifiedName) {
		blockBuilder().addLineWithEnding("import static " + fullyQualifiedName);
		return this;
	}

	default ClassBuilder writeAnnotation(String annotation) {
		blockBuilder().addLine(annotation);
		return this;
	}

}
