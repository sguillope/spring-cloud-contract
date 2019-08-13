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

/**
 * Builds the skeleton of the class. (Static) Imports, class definition,....
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.2.0
 */
interface ClassBuilder {

	ClassBuilder setupImports(List<Imports> imports);

	ClassBuilder setStaticImports(List<Imports> staticImports);

	ClassBuilder setupAnnotations(List<ClassAnnotation> annotations);

	ClassMetaData build();

	ClassBuilder writeImport(String fqn);

	ClassBuilder writeStaticImport(String fqn);

	ClassBuilder writeAnnotation(String annotation);

}
