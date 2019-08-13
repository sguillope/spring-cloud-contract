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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties;
import org.springframework.cloud.contract.verifier.file.ContractMetadata;

/**
 * Builds a single test for the given {@link ContractVerifierConfigProperties properties}
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 1.1.0
 */
public class JavaTestGenerator implements SingleTestGenerator {

	private static final Log log = LogFactory.getLog(JavaTestGenerator.class);

	@Override
	public String buildClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles, String className,
			String classPackage, String includedDirectoryRelativePath) {
		throw new UnsupportedOperationException("Deprecated method");
	}

	@Override
	public String buildClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles,
			String includedDirectoryRelativePath, GeneratedClassData generatedClassData) {
		return this.generateClass(properties, listOfFiles, includedDirectoryRelativePath,
				generatedClassData).content();
	}

	@Override
	public GeneratedTestClass generateClass(ContractVerifierConfigProperties properties,
			Collection<ContractMetadata> listOfFiles,
			String includedDirectoryRelativePath, GeneratedClassData generatedClassData) {
		BlockBuilder builder = new BlockBuilder("\t");
		GeneratedClassMetaData metaData = new GeneratedClassMetaData(properties,
				listOfFiles, includedDirectoryRelativePath, generatedClassData);
		return generateTestClass(builder, metaData);
	}

	GeneratedTestClass generateTestClass(BlockBuilder builder,
			GeneratedClassMetaData metaData) {
		// @formatter:off
		return GeneratedTestClassBuilder.builder(builder, metaData)
				.metaData()
					.custom(customClassMetaData(builder, metaData))
					.kotlin()
					.java()
					.groovy()
					.build()
				.imports()
					.defaultImports()
					.custom()
					.json()
					.jUnit4()
					.jUnit5()
					.testNG()
					.spock()
					.xml()
					.messaging()
					.restAssured()
					.jaxRs()
					.customImports(customImports(builder, metaData))
					.customStaticImports(customStaticImports(builder, metaData))
					.build()
				.classAnnotations()
					.defaultAnnotations()
					.jUnit4()
					.jUnit5()
					.spock()
					.custom(customAnnotations(builder, metaData))
					.build()
				.fields()
					.messaging()
					.custom(customFields(builder, metaData))
					.build()
				.build();
		// @formatter:on
	}

	@Override
	public String fileExtension(ContractVerifierConfigProperties properties) {
		return properties.getTestFramework().getClassExtension();
	}

	List<ClassMetaData> customClassMetaData(BlockBuilder builder,
			GeneratedClassMetaData metaData) {
		return Collections.emptyList();
	}

	List<Imports> customImports(BlockBuilder builder, GeneratedClassMetaData metaData) {
		return Collections.emptyList();
	}

	List<Imports> customStaticImports(BlockBuilder builder,
			GeneratedClassMetaData metaData) {
		return Collections.emptyList();
	}

	List<ClassAnnotation> customAnnotations(BlockBuilder builder,
			GeneratedClassMetaData metaData) {
		return Collections.emptyList();
	}

	List<Fields> customFields(BlockBuilder builder, GeneratedClassMetaData metaData) {
		return Collections.emptyList();
	}

}