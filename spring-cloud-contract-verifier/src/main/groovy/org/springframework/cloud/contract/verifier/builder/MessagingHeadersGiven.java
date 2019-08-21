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

import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Input;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class MessagingHeadersGiven implements Given, MethodVisitor<Given> {

	protected final MethodBodyWriter methodBodyWriter;

	MessagingHeadersGiven(MethodBodyWriter methodBodyWriter) {
		this.methodBodyWriter = methodBodyWriter;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		Input inputMessage = metadata.getContract().getInput();
		methodBodyWriter.inBlock(() ->
		// @formatter:off
			methodBodyWriter.withIndentation()
					.append(", ")
					.withMethodCall("headers")
					.closeCallAnd()
					.inBlock(() ->
							inputMessage.getMessageHeaders().executeForEachHeader(this::writeHeader)
					)
			// @formatter:on
		);
		return this;
	}

	private void writeHeader(Header header) {
		// @formatter:off
		methodBodyWriter.addNewLine()
				.withIndentation()
				.continueWithNewMethodCall("header")
					.withParameter(getTestSideValue(header.getName()))
					.withParameter(getTestSideValue(header.getServerValue()))
				.closeCall();
		// @formatter:on
	}

	private String getTestSideValue(Object object) {
		return '"' + MapConverter.getTestSideValues(object).toString() + '"';
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getInput().getMessageHeaders() != null;
	}

}
