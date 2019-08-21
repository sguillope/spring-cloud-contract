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

import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;

class MessagingBodyGiven implements Given, MethodVisitor<Given> {

	private final BodyReader bodyReader;

	private final BodyParser bodyParser;

	protected final MethodBodyWriter methodBodyWriter;

	MessagingBodyGiven(MethodBodyWriter methodBodyWriter, BodyReader bodyReader,
			BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.bodyReader = bodyReader;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		Object body = body(metadata);
		if (body instanceof FromFileProperty) {
			FromFileProperty fileProperty = (FromFileProperty) body;
			String fileContent = this.bodyReader.readBytesFromFileString(metadata,
					fileProperty, CommunicationType.REQUEST);
			if (fileProperty.isByte()) {
				methodBodyWriter.withIndentation().append(fileContent);
			}
			else {
				// @formatter:off
				methodBodyWriter
						.withIndentation()
						.createInstanceOf("String")
							.withArgument(fileContent)
						.instantiate();
				// @formatter:on
			}
		}
		else {
			String text = this.bodyParser.convertToJsonString(body);
			// @formatter:off
			methodBodyWriter.withIndentation()
					.append(this.bodyParser.quotedEscapedLongText(text));
			// @formatter:on
		}
		return this;
	}

	private Object body(SingleContractMetadata metadata) {
		ContentType contentType = metadata.getInputTestContentType();
		Input inputMessage = metadata.getContract().getInput();
		return this.bodyParser.extractServerValueFromBody(contentType,
				inputMessage.getMessageBody().getServerValue());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getMessageBody() != null;
	}

}
