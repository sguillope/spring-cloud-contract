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
import org.springframework.cloud.contract.spec.internal.OutputMessage;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

class MessagingReceiveMessageThen implements Then, BodyMethodVisitor {

	private final ComparisonBuilder comparisonBuilder;

	protected final MethodBodyWriter methodBodyWriter;

	MessagingReceiveMessageThen(MethodBodyWriter methodBodyWriter,
			ComparisonBuilder comparisonBuilder) {
		this.methodBodyWriter = methodBodyWriter;
		this.comparisonBuilder = comparisonBuilder;
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		OutputMessage outputMessage = singleContractMetadata.getContract()
				.getOutputMessage();
		// @formatter:off
		methodBodyWriter
				.declareVariable("response", "ContractVerifierMessage")
				.assignValue()
				.usingVariable("contractVerifierMessaging")
				.callMethod("receive")
					.withParameter(sentToValue(outputMessage.getSentTo().getServerValue()))
				.closeCallAndEndStatement();
		// @formatter:on
		methodBodyWriter.addLine(comparisonBuilder.assertThatIsNotNull("response"));
		return this;
	}

	private String sentToValue(Object sentTo) {
		if (sentTo instanceof ExecutionProperty) {
			return ((ExecutionProperty) sentTo).getExecutionCommand();
		}
		return '"' + sentTo.toString() + '"';
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging() && singleContractMetadata
				.getContract().getOutputMessage().getSentTo() != null;
	}

}
