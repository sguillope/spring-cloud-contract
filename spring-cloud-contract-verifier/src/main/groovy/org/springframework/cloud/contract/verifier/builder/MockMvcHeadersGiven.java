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

import java.util.Iterator;

import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

import static org.springframework.cloud.contract.verifier.builder.ContentHelper.getTestSideForNonBodyValue;
import static org.springframework.cloud.contract.verifier.util.MapConverter.getTestSideValuesForNonBody;

class MockMvcHeadersGiven implements Given {

	protected final MethodBodyWriter methodBodyWriter;

	MockMvcHeadersGiven(MethodBodyWriter methodBodyWriter) {
		this.methodBodyWriter = methodBodyWriter;
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		Iterator<Header> iterator = metadata.getContract().getRequest().getHeaders()
				.getEntries().iterator();
		while (iterator.hasNext()) {
			Header header = iterator.next();
			if (ofAbsentType(header)) {
				continue;
			}
			// @formatter:off
			methodBodyWriter.withIndentation()
					.continueWithNewMethodCall("header")
						.withParameter(getTestSideForNonBodyValue(header.getName()))
						.withParameter(getTestSideForNonBodyValue(
						getTestSideValuesForNonBody(header.getServerValue())))
					.closeCallAnd();
			// @formatter:on
			if (iterator.hasNext()) {
				methodBodyWriter.addNewLine();
			}
		}
		return this;
	}

	private boolean ofAbsentType(Header header) {
		return header.getServerValue() instanceof MatchingStrategy
				&& MatchingStrategy.Type.ABSENT
						.equals(((MatchingStrategy) header.getServerValue()).getType());
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getHeaders() != null;
	}

}
