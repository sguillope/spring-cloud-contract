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
import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.ContentType;
import org.springframework.util.StringUtils;

class JaxRsRequestMethodWhen implements When, JaxRsBodyParser {

	private final BodyReader bodyReader;

	protected final MethodBodyWriter methodBodyWriter;

	JaxRsRequestMethodWhen(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData metaData) {
		this.methodBodyWriter = methodBodyWriter;
		this.bodyReader = new BodyReader(metaData);
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		String requestMethod = requestMethod(request);
		if (request.getBody() != null) {
			// @formatter:off
			// .build("[request-method]", entity([request-content], "[content-type]"))
			methodBodyWriter.withIndentation()
					.continueWithNewMethodCall("build")
						.withParameter("\"" + requestMethod.toUpperCase() + "\"")
						.withParameter(() ->
							methodBodyWriter.withMethodCall("entity")
								.withParameter(() -> writeRequestContent(request, metadata, methodBodyWriter))
								.withParameter("\"" + requestContentType(request, metadata) + "\"")
							.closeCall()
						)
					.closeCall();
			// @formatter:on
		}
		else {
			// @formatter:off
			methodBodyWriter.withIndentation()
					.continueWithNewMethodCall("build")
						.withParameter("\"" + requestMethod.toUpperCase() + "\"")
					.closeCall();
			// @formatter:on
		}
		return this;
	}

	private String requestMethod(Request request) {
		return request.getMethod().getServerValue().toString().toLowerCase();
	}

	private void writeRequestContent(Request request, SingleContractMetadata metadata,
			MethodBodyWriter methodBodyWriter) {
		Object body = request.getBody().getServerValue();
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
			methodBodyWriter.append("\"" + requestBodyAsString(metadata) + "\"");
		}
	}

	private String requestContentType(Request request, SingleContractMetadata metadata) {
		ContentType type = metadata.getInputTestContentType();
		if (StringUtils.hasText(type.getMimeType())) {
			return type.getMimeType();
		}
		Header contentType = request.getHeaders().getEntries().stream()
				.filter(header -> "Content-Type".equalsIgnoreCase(header.getName()))
				.findFirst().orElse(null);
		return contentType != null ? contentType.getServerValue().toString() : "";
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}
