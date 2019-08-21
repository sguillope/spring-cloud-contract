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

import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

class MockMvcBodyGiven implements Given {

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	protected final MethodBodyWriter methodBodyWriter;

	MockMvcBodyGiven(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.bodyReader = new BodyReader(generatedClassMetaData);
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		Object body;
		Request request = metadata.getContract().getRequest();
		Object serverValue = request.getBody().getServerValue();
		if (serverValue instanceof ExecutionProperty
				|| serverValue instanceof FromFileProperty) {
			body = request.getBody().getServerValue();
		}
		else {
			body = this.bodyParser.requestBodyAsString(metadata);
		}
		// @formatter:off
		methodBodyWriter
				.withIndentation()
				.continueWithNewMethodCall("body")
					.withParameter(() -> writeBody(metadata, body)).closeCallAnd()
				.addEmptyLine();
		// @formatter:on
		return this;
	}

	private void writeBody(SingleContractMetadata metadata, Object body) {
		if (body instanceof ExecutionProperty) {
			methodBodyWriter.append(body.toString());
		}
		else if (body instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) body;
			String fileContent = this.bodyReader.readBytesFromFileString(metadata,
					fileProperty, CommunicationType.REQUEST);
			if (fileProperty.isByte()) {
				methodBodyWriter.append(fileContent);
			}
			else {
				// @formatter:off
				methodBodyWriter
						.createInstanceOf("String")
							.withArgument(fileContent)
						.instantiate();
				// @formatter:on
			}
		}
		else {
			String escaped = escapeRequestSpecialChars(metadata, body.toString());
			methodBodyWriter.append(this.bodyParser.quotedEscapedLongText(escaped));
		}
	}

	private String escapeRequestSpecialChars(SingleContractMetadata metadata,
			String string) {
		if (metadata.getInputTestContentType() == ContentType.JSON) {
			return string.replaceAll("\\\\n", "\\\\\\\\n");
		}
		return string;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getBody() != null;
	}

}
