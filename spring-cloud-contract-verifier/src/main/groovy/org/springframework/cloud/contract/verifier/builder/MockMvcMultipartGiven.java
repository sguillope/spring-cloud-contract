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

import java.util.Map;

import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.javaNamedPropertyValue;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.namedContentTypeNameIfPresent;
import static org.springframework.cloud.contract.verifier.util.ContentUtils.namedPropertyName;

class MockMvcMultipartGiven implements Given {

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	protected final GeneratedClassMetaData generatedClassMetaData;

	protected final MethodBodyWriter methodBodyWriter;

	MockMvcMultipartGiven(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.bodyReader = new BodyReader(generatedClassMetaData);
		this.bodyParser = bodyParser;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		getMultipartParameters(metadata).forEach((key, value) -> {
			if (isMultipartParameter(value)) {
				NamedProperty namedProperty = (NamedProperty) value;
				// @formatter:off
				methodBodyWriter.withIndentation()
						.continueWithNewMethodCall("multiPart")
							.withParameter(parameterName(key))
							.withParameter(multipartName(namedProperty))
							.withParameter(multipartContent(namedProperty, metadata))
							.withParameter(multipartContentType(namedProperty))
						.closeCallAnd()
						.addNewLine();
				// @formatter:on
			}
			else {
				// @formatter:off
				methodBodyWriter.withIndentation()
						.continueWithNewMethodCall("param")
							.withParameter(this.bodyParser.quotedShortText(key))
							.withParameter(this.bodyParser.quotedShortText(
									MapConverter.getTestSideValuesForNonBody(value)))
						.closeCallAnd()
						.addNewLine();
				// @formatter:on
			}
		});
		return this;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMultipartParameters(SingleContractMetadata metadata) {
		return (Map<String, Object>) metadata.getContract().getRequest().getMultipart()
				.getServerValue();
	}

	private boolean isMultipartParameter(Object parameter) {
		return parameter instanceof NamedProperty;
	}

	protected BodyReader bodyReader() {
		return bodyReader;
	}

	protected String quote() {
		return "\"";
	}

	protected String parameterName(String parameterName) {
		return quote() + escapeJava(parameterName) + quote();
	}

	protected String multipartName(NamedProperty namedProperty) {
		return namedPropertyName(namedProperty, quote());
	}

	protected String multipartContent(NamedProperty namedProperty,
			SingleContractMetadata metadata) {
		return javaNamedPropertyValue(namedProperty, quote(),
				(FromFileProperty property) -> this.bodyReader.readBytesFromFileString(
						metadata, property, CommunicationType.REQUEST));
	}

	protected String multipartContentType(NamedProperty namedProperty) {
		return namedContentTypeNameIfPresent(namedProperty, quote());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() != TestFramework.SPOCK;
	}

}
