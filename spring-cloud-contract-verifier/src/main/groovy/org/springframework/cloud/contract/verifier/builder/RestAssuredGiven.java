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

class RestAssuredGiven implements Given, BodyMethodVisitor {

	private final GeneratedClassMetaData generatedClassMetaData;

	private final List<Given> requestGivens = new LinkedList<>();

	private final List<Given> bodyGivens = new LinkedList<>();

	protected final MethodBodyWriter methodBodyWriter;

	RestAssuredGiven(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.generatedClassMetaData = generatedClassMetaData;
		this.requestGivens.addAll(Arrays.asList(
				new MockMvcRequestGiven(methodBodyWriter, generatedClassMetaData),
				new SpockMockMvcRequestGiven(methodBodyWriter, generatedClassMetaData),
				new ExplicitRequestGiven(methodBodyWriter, generatedClassMetaData),
				new WebTestClientRequestGiven(methodBodyWriter, generatedClassMetaData)));
		this.bodyGivens.addAll(Arrays.asList(new MockMvcHeadersGiven(methodBodyWriter),
				new MockMvcCookiesGiven(methodBodyWriter),
				new MockMvcBodyGiven(methodBodyWriter, generatedClassMetaData,
						bodyParser),
				hasKotlinSupport()
						? new KotlinMockMvcMultipartGiven(methodBodyWriter,
								generatedClassMetaData, bodyParser)
						: new MockMvcMultipartGiven(methodBodyWriter,
								generatedClassMetaData, bodyParser),
				new SpockMockMvcMultipartGiven(methodBodyWriter, generatedClassMetaData,
						bodyParser)));
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata singleContractMetadata) {
		methodBodyWriter.inGivenBlock(() -> {
			addRequestGivenLine(singleContractMetadata);
			indentedBodyBlock(methodBodyWriter, this.bodyGivens, singleContractMetadata);
		});
		return this;
	}

	private void addRequestGivenLine(SingleContractMetadata singleContractMetadata) {
		this.requestGivens.stream().filter(given -> given.accept(singleContractMetadata))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No matching request building Given implementation for Rest Assured"))
				.apply(singleContractMetadata);
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isHttp()
				&& this.generatedClassMetaData.configProperties
						.getTestMode() != TestMode.JAXRSCLIENT;
	}

}
