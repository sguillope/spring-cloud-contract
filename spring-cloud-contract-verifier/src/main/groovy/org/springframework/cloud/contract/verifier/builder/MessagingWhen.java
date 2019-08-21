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

class MessagingWhen implements When, BodyMethodVisitor {

	private final List<When> whens = new LinkedList<>();

	protected final MethodBodyWriter methodBodyWriter;

	MessagingWhen(MethodBodyWriter methodBodyWriter) {
		this.methodBodyWriter = methodBodyWriter;
		this.whens.addAll(Arrays.asList(new MessagingTriggeredByWhen(methodBodyWriter),
				new MessagingBodyWhen(methodBodyWriter),
				new MessagingAssertThatWhen(methodBodyWriter)));
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata singleContractMetadata) {
		methodBodyWriter.inWhenBlock(
				() -> bodyBlock(methodBodyWriter, this.whens, singleContractMetadata));
		return this;
	}

	@Override
	public void applyVisitorsWithEnding(MethodBodyWriter methodBodyWriter,
			SingleContractMetadata singleContractMetadata, List<MethodVisitor> visitors) {
		visitors.forEach(visitor -> visitor.apply(singleContractMetadata));
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging();
	}

}
