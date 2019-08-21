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

class MessagingGiven implements Given, MethodVisitor<Given>, BodyMethodVisitor {

	private final List<Given> givens = new LinkedList<>();

	protected final MethodBodyWriter methodBodyWriter;

	MessagingGiven(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		this.methodBodyWriter = methodBodyWriter;
		this.givens.addAll(Arrays.asList(
				new MessagingBodyGiven(methodBodyWriter,
						new BodyReader(generatedClassMetaData), bodyParser),
				new MessagingHeadersGiven(methodBodyWriter)));
	}

	@Override
	public MethodVisitor<Given> apply(SingleContractMetadata metadata) {
		methodBodyWriter.inGivenBlock(() -> {
			// @formatter:off
			methodBodyWriter
					.declareVariable("inputMessage", "ContractVerifierMessage")
					.assignValue()
					.usingVariable("contractVerifierMessaging").and();
			// @formatter:on
			methodBodyWriter.blockBuilder().append(".create(").addEmptyLine();
			methodBodyWriter.inBlock(2, () -> this.givens.stream()
					.filter(given -> given.accept(metadata)).forEach(given -> {
						given.apply(metadata);
						methodBodyWriter.addEmptyLine();
					}));
			methodBodyWriter.addLine(")");
		});
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.isMessaging()
				&& metadata.getContract().getInput().getTriggeredBy() == null;
	}

}
