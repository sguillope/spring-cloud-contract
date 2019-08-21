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

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class JaxRsWhen implements When, BodyMethodVisitor, JaxRsAcceptor {

	private final GeneratedClassMetaData generatedClassMetaData;

	private final JaxRsBodyParser bodyParser;

	private final List<When> whens = new LinkedList<>();

	protected final MethodBodyWriter methodBodyWriter;

	JaxRsWhen(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, JaxRsBodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.generatedClassMetaData = generatedClassMetaData;
		this.bodyParser = bodyParser;
		this.whens.addAll(Arrays.asList(
				new JaxRsUrlPathWhen(methodBodyWriter, generatedClassMetaData),
				new JaxRsRequestWhen(methodBodyWriter, generatedClassMetaData),
				new JaxRsRequestHeadersWhen(methodBodyWriter, bodyParser),
				new JaxRsRequestCookiesWhen(methodBodyWriter, bodyParser),
				new JaxRsRequestMethodWhen(methodBodyWriter, generatedClassMetaData),
				new JaxRsRequestInvokerWhen(methodBodyWriter)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		methodBodyWriter.inWhenBlock(() -> {
			// @formatter:off
			methodBodyWriter
					.declareVariable("response", "Response")
					.assignValue()
					.usingVariable("webTarget")
					.and();
			// @formatter:on
			indentedBodyBlock(methodBodyWriter, this.whens, singleContractMetadata);
			if (expectsResponseBody(singleContractMetadata)) {
				// @formatter:off
				methodBodyWriter
						.declareVariable("responseAsString", "String")
						.assignValue(this.bodyParser.readEntity());
				// @formatter:on
			}
		});
		return this;
	}

	private boolean expectsResponseBody(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBody() != null;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return acceptType(this.generatedClassMetaData, singleContractMetadata);
	}

}
