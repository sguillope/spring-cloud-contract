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
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.spec.internal.Url;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class MockMvcUrlWhen implements When, MockMvcAcceptor, QueryParamsResolver {

	private final BodyParser bodyParser;

	protected final MethodBodyWriter methodBodyWriter;

	MockMvcUrlWhen(MethodBodyWriter methodBodyWriter, BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		// @formatter:off
		methodBodyWriter
				.withIndentation()
				.continueWithNewMethodCall(requestMethod(request))
					.withParameter(urlFrom(request))
				.closeCall();
		// @formatter:on
		return this;
	}

	private String requestMethod(Request request) {
		return request.getMethod().getServerValue().toString().toLowerCase();
	}

	private String urlFrom(Request request) {
		Url url = getUrl(request);
		Object testSideUrl = MapConverter.getTestSideValues(url);
		if (!(testSideUrl instanceof ExecutionProperty)) {
			return this.bodyParser.quotedShortText(testSideUrl.toString());
		}
		return testSideUrl.toString();
	}

	private Url getUrl(Request request) {
		if (request.getUrl() != null) {
			return request.getUrl();
		}
		if (request.getUrlPath() != null) {
			return request.getUrlPath();
		}
		throw new IllegalStateException("URL is not set!");
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return true;
	}

}
