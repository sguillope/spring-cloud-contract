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

/**
 * Builds a single method body. Must be executed per contract.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.2.0
 */
public interface SingleMethodBuilder {

	SingleMethodBuilder methodAnnotation(MethodAnnotations methodAnnotations);

	MethodAnnotationBuilder methodAnnotation();

	SingleMethodBuilder methodAnnotation(MethodAnnotations... methodAnnotations);

	MethodMetadataBuilder methodMetadata();

	SingleMethodBuilder methodMetadata(MethodMetadata... methodMetadata);

	SingleMethodBuilder restAssured();

	SingleMethodBuilder jaxRs();

	SingleMethodBuilder messaging();

	SingleMethodBuilder given(Given... given);

	SingleMethodBuilder when(When... when);

	SingleMethodBuilder then(Then... then);

	BlockBuilder getBlockBuilder();

	GeneratedClassMetaData getGeneratedClassMetaData();

	/**
	 * Mutates the {@link BlockBuilder} to generate a methodBuilder
	 * @return block builder with contents of a single methodBuilder
	 */
	BlockBuilder build();

	SingleMethodBuilder variable(String name, String className);

}
