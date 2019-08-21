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

import org.springframework.cloud.contract.verifier.config.TestMode;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

import static org.springframework.cloud.contract.verifier.util.KotlinPluginsAvailabilityChecker.hasKotlinSupport;

class RestAssuredWhen implements When, BodyMethodVisitor {

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<When> responseWhens = new LinkedList<>();

	private final List<When> whens = new LinkedList<>();

	protected final MethodBodyWriter methodBodyWriter;

	RestAssuredWhen(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.generatedClassMetaData = generatedClassMetaData;
		this.responseWhens.addAll(Arrays.asList(
				new SpockMockMvcResponseWhen(methodBodyWriter, generatedClassMetaData),
				new MockMvcResponseWhen(methodBodyWriter, generatedClassMetaData),
				new ExplicitResponseWhen(methodBodyWriter, generatedClassMetaData),
				new WebTestClientResponseWhen(methodBodyWriter, generatedClassMetaData)));
		this.whens.addAll(Arrays.asList(
				new MockMvcQueryParamsWhen(methodBodyWriter, bodyParser),
				hasKotlinSupport()
						? new KotlinMockMvcAsyncWhen(methodBodyWriter,
								generatedClassMetaData)
						: new MockMvcAsyncWhen(methodBodyWriter, generatedClassMetaData),
				new MockMvcUrlWhen(methodBodyWriter, bodyParser)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		methodBodyWriter.inWhenBlock(() -> {
			addResponseWhenLine(singleContractMetadata);
			indentedBodyBlock(methodBodyWriter, this.whens, singleContractMetadata);
		});
		methodBodyWriter.addEmptyLine();
		return this;
	}

	private void addResponseWhenLine(SingleContractMetadata singleContractMetadata) {
		this.responseWhens.stream().filter(when -> when.accept(singleContractMetadata))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No matching request building When implementation for Rest Assured"))
				.apply(singleContractMetadata);
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isHttp()
				&& this.generatedClassMetaData.configProperties
						.getTestMode() != TestMode.JAXRSCLIENT;
	}

}
