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

import java.util.LinkedList;
import java.util.List;

/**
 * Builds the body of the Java class. Sets fields, methods.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class JavaClassBodyBuilder implements ClassBodyBuilder, DefaultClassBodyBuilder {

	private List<Fields> fields = new LinkedList<>();

	final BlockBuilder blockBuilder;

	final GeneratedClassMetaData generatedClassMetaData;

	final SingleMethodBuilder methodBuilder;

	private JavaClassBodyBuilder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.methodBuilder = JavaSingleMethodBuilder
		// @formatter:off
				.builder(this.blockBuilder, this.generatedClassMetaData)
				.methodAnnotation()
					.jUnit4()
					.jUnit5()
					.testNG()
					.build()
				.methodMetadata()
					.jUnit()
					.build()
				.restAssured()
				.jaxRs()
				.messaging();
				// @formatter:on;
	}

	static JavaClassBodyBuilder builder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		return new JavaClassBodyBuilder(blockBuilder, generatedClassMetaData);
	}

	@Override
	public ClassBodyBuilder setupFields(List<Fields> fields) {
		this.fields = fields;
		return this;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public SingleMethodBuilder methodBuilder() {
		return this.methodBuilder;
	}

	@Override
	public List<? extends Fields> fields() {
		return this.fields;
	}

}
