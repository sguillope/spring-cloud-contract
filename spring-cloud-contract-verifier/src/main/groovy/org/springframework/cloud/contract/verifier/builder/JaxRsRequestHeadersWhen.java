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
import java.util.Set;

import org.springframework.cloud.contract.spec.internal.Header;
import org.springframework.cloud.contract.spec.internal.Headers;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class JaxRsRequestHeadersWhen implements When {

	private final BodyParser bodyParser;

	protected final MethodBodyWriter methodBodyWriter;

	JaxRsRequestHeadersWhen(MethodBodyWriter methodBodyWriter, BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.bodyParser = bodyParser;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Iterator<Header> iterator = getHeaders(metadata).getEntries().stream()
				.filter(header -> !headerToIgnore(header)).iterator();
		while (iterator.hasNext()) {
			Header header = iterator.next();
			// @formatter:off
			methodBodyWriter.withIndentation()
					.continueWithNewMethodCall("header")
						.withParameter("\"" + header.getName() + "\"")
						.withParameter(this.bodyParser.quotedLongText(MapConverter
					.getTestSideValuesForNonBody(header.getServerValue())))
					.closeCallAnd();
			// @formatter:on
			if (iterator.hasNext()) {
				methodBodyWriter.addNewLine();
			}
		}
		return this;
	}

	private Headers getHeaders(SingleContractMetadata metadata) {
		return metadata.getContract().getRequest().getHeaders();
	}

	private boolean headerToIgnore(Header header) {
		return contentTypeOrAccept(header) || headerOfAbsentType(header);
	}

	private boolean contentTypeOrAccept(Header header) {
		return "Content-Type".equalsIgnoreCase(header.getName())
				|| "Accept".equalsIgnoreCase(header.getName());
	}

	private boolean headerOfAbsentType(Header header) {
		return header.getServerValue() instanceof MatchingStrategy
				&& ((MatchingStrategy) header.getServerValue())
						.getType() == MatchingStrategy.Type.ABSENT;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return getHeaders(metadata) != null
				&& !getHeaders(metadata).getEntries().isEmpty()
				&& !hasHeaderOnlyContentTypeOrAccept(metadata);
	}

	private boolean hasHeaderOnlyContentTypeOrAccept(SingleContractMetadata metadata) {
		Set<Header> entries = getHeaders(metadata).getEntries();
		long filteredOut = entries.stream().filter(this::headerToIgnore).count();
		return filteredOut == entries.size();
	}

}
