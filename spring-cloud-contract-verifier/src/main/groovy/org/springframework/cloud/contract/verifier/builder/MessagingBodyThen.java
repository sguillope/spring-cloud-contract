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
import java.util.stream.Collectors;

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

import static java.util.stream.Collectors.toList;

class MessagingBodyThen implements Then, BodyMethodVisitor {

	private final List<Then> thens = new LinkedList<>();

	private final BodyParser bodyParser;

	protected final MethodBodyWriter methodBodyWriter;

	MessagingBodyThen(MethodBodyWriter methodBodyWriter, GeneratedClassMetaData metaData,
			ComparisonBuilder comparisonBuilder) {
		this.methodBodyWriter = methodBodyWriter;
		this.bodyParser = comparisonBuilder.bodyParser();
		this.thens.addAll(Arrays.asList(
				new GenericBinaryBodyThen(methodBodyWriter, metaData, this.bodyParser,
						comparisonBuilder),
				new GenericTextBodyThen(methodBodyWriter, metaData, this.bodyParser,
						comparisonBuilder),
				new GenericJsonBodyThen(methodBodyWriter, metaData, this.bodyParser,
						comparisonBuilder),
				new GenericXmlBodyThen(methodBodyWriter, this.bodyParser)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata singleContractMetadata) {
		List<Then> thens = this.thens.stream()
				.filter(then -> then.accept(singleContractMetadata)).collect(toList());
		if (!thens.isEmpty()) {
			methodBodyWriter.inAndBlock(
					() -> thens.forEach(then -> then.apply(singleContractMetadata)));
		}
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata singleContractMetadata) {
		return singleContractMetadata.isMessaging()
				&& this.bodyParser.responseBody(singleContractMetadata) != null;
	}

}
