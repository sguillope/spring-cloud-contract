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

import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.template.HandlebarsTemplateProcessor;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;

import static java.util.stream.Collectors.toList;

class GenericHttpBodyThen implements Then, BodyMethodVisitor {

	private final TemplateProcessor templateProcessor;

	private final List<Then> thens = new LinkedList<>();

	protected final MethodBodyWriter methodBodyWriter;

	GenericHttpBodyThen(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData metaData, BodyParser bodyParser,
			ComparisonBuilder comparisonBuilder) {
		this.methodBodyWriter = methodBodyWriter;
		this.templateProcessor = new HandlebarsTemplateProcessor();
		this.thens.addAll(Arrays.asList(
				new GenericBinaryBodyThen(methodBodyWriter, metaData, bodyParser,
						comparisonBuilder),
				new GenericTextBodyThen(methodBodyWriter, metaData, bodyParser,
						comparisonBuilder),
				new GenericJsonBodyThen(methodBodyWriter, metaData, bodyParser,
						comparisonBuilder),
				new GenericXmlBodyThen(methodBodyWriter, bodyParser)));
	}

	@Override
	public MethodVisitor<Then> apply(SingleContractMetadata metadata) {
		List<Then> thens = this.thens.stream().filter(then -> then.accept(metadata))
				.collect(toList());
		if (!thens.isEmpty()) {
			methodBodyWriter.addEmptyLine().inAndBlock(() -> {
				Request request = metadata.getContract().getRequest();
				thens.forEach(then -> then.apply(metadata));
				String newBody = this.templateProcessor.transform(request,
						methodBodyWriter.blockBuilder().toString());
				methodBodyWriter.blockBuilder().updateContents(newBody);
			});
		}
		return this;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return metadata.getContract().getResponse().getBody() != null;
	}

}
