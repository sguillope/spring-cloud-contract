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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.cloud.contract.verifier.util.NamesUtil.capitalize;

/**
 * A generated test class consists of the class meta data (e.g. packages, imports) fields
 * and methods. The latter are generated via the {@link ClassBodyBuilder}.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class GeneratedTestClassBuilder {

	private List<ClassMetaData> metaData = new LinkedList<>();

	private List<Imports> imports = new LinkedList<>();

	private List<Imports> staticImports = new LinkedList<>();

	private List<ClassAnnotation> annotations = new LinkedList<>();

	private List<Fields> fields = new LinkedList<>();

	final BlockBuilder blockBuilder;

	final GeneratedClassMetaData generatedClassMetaData;

	private GeneratedTestClassBuilder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	static GeneratedTestClassBuilder builder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		return new GeneratedTestClassBuilder(blockBuilder, generatedClassMetaData);
	}

	GeneratedTestClassBuilder metaData(ClassMetaData metaData) {
		this.metaData.add(metaData);
		return this;
	}

	MetaDataBuilder metaData() {
		return new MetaDataBuilder(this);
	}

	GeneratedTestClassBuilder metaData(ClassMetaData... metaData) {
		return metaData(Arrays.asList(metaData));
	}

	GeneratedTestClassBuilder metaData(List<ClassMetaData> metaData) {
		this.metaData.addAll(metaData);
		return this;
	}

	ImportsBuilder imports() {
		return new ImportsBuilder(this);
	}

	GeneratedTestClassBuilder imports(Imports imports) {
		this.imports.add(imports);
		return this;
	}

	GeneratedTestClassBuilder imports(Imports... imports) {
		return imports(Arrays.asList(imports));
	}

	GeneratedTestClassBuilder imports(List<Imports> imports) {
		this.imports.addAll(imports);
		return this;
	}

	GeneratedTestClassBuilder staticImports(Imports imports) {
		this.staticImports.add(imports);
		return this;
	}

	GeneratedTestClassBuilder staticImports(Imports... imports) {
		return staticImports(Arrays.asList(imports));
	}

	GeneratedTestClassBuilder staticImports(List<Imports> imports) {
		this.staticImports.addAll(imports);
		return this;
	}

	ClassAnnotationsBuilder classAnnotations() {
		return new ClassAnnotationsBuilder(this);
	}

	GeneratedTestClassBuilder classAnnotations(ClassAnnotation... annotations) {
		return classAnnotations(Arrays.asList(annotations));
	}

	GeneratedTestClassBuilder classAnnotations(List<ClassAnnotation> annotations) {
		this.annotations.addAll(annotations);
		return this;
	}

	FieldsBuilder fields() {
		return new FieldsBuilder(this);
	}

	GeneratedTestClassBuilder field(Fields fields) {
		this.fields.add(fields);
		return this;
	}

	GeneratedTestClassBuilder fields(Fields... fields) {
		return fields(Arrays.asList(fields));
	}

	GeneratedTestClassBuilder fields(List<Fields> fields) {
		this.fields.addAll(fields);
		return this;
	}

	/**
	 * From a matching {@link ClassMetaData} given the present input data, builds a
	 * generated test class.
	 * @return generated test class
	 */
	GeneratedTestClass build() {
		// picks a matching class meta data
		ClassMetaData classMetaData = this.metaData.stream().filter(Acceptor::accept)
				.findFirst().orElseThrow(() -> new IllegalStateException(
						"There is no matching class meta data"));
		// @formatter:off
		classMetaData.setupLineEnding()
				.setupLabelPrefix()
				.classDefinition()
					.setupImports(this.imports)
					.setStaticImports(this.staticImports)
					.setupAnnotations(this.annotations)
					.build()
				.classBody()
					.setupFields(this.fields)
					.build();
		// @formatter:on
		String classFileName = capitalize(
				this.generatedClassMetaData.generatedClassData.className);
		String classFileSuffix = classMetaData.getClassNameSuffix();
		if (!classFileName.endsWith(classFileSuffix)) {
			classFileName += classFileSuffix;
		}
		classFileName += classMetaData.getFileExtension();
		return new GeneratedTestClass(classFileName, this.blockBuilder.toString());
	}

}
