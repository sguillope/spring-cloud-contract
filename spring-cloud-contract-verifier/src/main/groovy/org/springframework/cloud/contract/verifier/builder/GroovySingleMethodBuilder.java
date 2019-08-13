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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

/**
 * Builds a single method body. Must be executed per contract.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @since 2.2.0
 */
class GroovySingleMethodBuilder implements SingleMethodBuilder {

	private List<MethodAnnotations> methodAnnotations = new LinkedList<>();

	private List<MethodMetadata> methodMetadata = new LinkedList<>();

	private List<Given> givens = new LinkedList<>();

	private List<When> whens = new LinkedList<>();

	private List<Then> thens = new LinkedList<>();

	final GeneratedClassMetaData generatedClassMetaData;

	final BlockBuilder blockBuilder;

	private GroovySingleMethodBuilder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	public static GroovySingleMethodBuilder builder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		return new GroovySingleMethodBuilder(blockBuilder, generatedClassMetaData);
	}

	public GroovySingleMethodBuilder methodAnnotation(
			MethodAnnotations methodAnnotations) {
		this.methodAnnotations.add(methodAnnotations);
		return this;
	}

	public MethodAnnotationBuilder methodAnnotation() {
		return new MethodAnnotationBuilder(this);
	}

	public GroovySingleMethodBuilder methodAnnotation(
			MethodAnnotations... methodAnnotations) {
		this.methodAnnotations.addAll(Arrays.asList(methodAnnotations));
		return this;
	}

	public MethodMetadataBuilder methodMetadata() {
		return new MethodMetadataBuilder(this);
	}

	public GroovySingleMethodBuilder methodMetadata(MethodMetadata... methodMetadata) {
		this.methodMetadata.addAll(Arrays.asList(methodMetadata));
		return this;
	}

	public GroovySingleMethodBuilder restAssured() {
		return given(new RestAssuredGiven(this.blockBuilder, this.generatedClassMetaData,
				SpockRestAssuredBodyParser.INSTANCE))
						.when(new RestAssuredWhen(this.blockBuilder,
								this.generatedClassMetaData,
								RestAssuredBodyParser.INSTANCE))
						.then(new RestAssuredThen(this.blockBuilder,
								this.generatedClassMetaData,
								SpockRestAssuredBodyParser.INSTANCE,
								GroovyComparisonBuilder.SPOCK_HTTP_INSTANCE));
	}

	public GroovySingleMethodBuilder jaxRs() {
		return given(new JaxRsGiven(this.generatedClassMetaData))
				.when(new JaxRsWhen(this.blockBuilder, this.generatedClassMetaData,
						SpockJaxRsBodyParser.INSTANCE))
				.then(new JaxRsThen(this.blockBuilder, this.generatedClassMetaData,
						SpockJaxRsBodyParser.INSTANCE,
						GroovyComparisonBuilder.JAXRS_HTTP_INSTANCE));
	}

	public GroovySingleMethodBuilder messaging() {
		// @formatter:off
		return given(new MessagingGiven(this.blockBuilder, this.generatedClassMetaData, SpockMessagingBodyParser.INSTANCE))
				.when(new MessagingWhen(this.blockBuilder))
				.then(new MessagingWithBodyThen(this.blockBuilder,
						this.generatedClassMetaData, GroovyComparisonBuilder.SPOCK_MESSAGING_INSTANCE))
				.then(new SpockMessagingEmptyThen(this.blockBuilder,
						this.generatedClassMetaData));
		// @formatter:on
	}

	public GroovySingleMethodBuilder given(Given... given) {
		this.givens.addAll(Arrays.asList(given));
		return this;
	}

	public GroovySingleMethodBuilder when(When... when) {
		this.whens.addAll(Arrays.asList(when));
		return this;
	}

	public GroovySingleMethodBuilder then(Then... then) {
		this.thens.addAll(Arrays.asList(then));
		return this;
	}

	@Override
	public BlockBuilder getBlockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public GeneratedClassMetaData getGeneratedClassMetaData() {
		return this.generatedClassMetaData;
	}

	/**
	 * Mutates the {@link BlockBuilder} to generate a methodBuilder
	 * @return block builder with contents of a single methodBuilder
	 */
	public BlockBuilder build() {
		MethodMetadata methodMetadatum = pickMetadatum();
		// \n
		this.blockBuilder.addEmptyLine();
		this.generatedClassMetaData.toSingleContractMetadata().forEach(metaData -> {
			// @Test
			if (visit(this.methodAnnotations, metaData, false)) {
				this.blockBuilder.addEmptyLine();
			}
			// @formatter:off
			// public void validate_foo()
			this.blockBuilder.append(methodMetadatum::modifier)
					.appendWithSpace(methodMetadatum::returnType)
					.appendWithSpace(() -> methodMetadatum.name(metaData))
					.append("() throws Exception ");
			// (space) {
			this.blockBuilder.inBraces(() -> {
				// (indent) given
				if (visit(this.givens, metaData)) {
					this.blockBuilder.addEmptyLine();
				}
				// (indent) when
				visit(this.whens, metaData);
				this.blockBuilder.addEmptyLine();
				// (indent) then
				visit(this.thens, metaData);
			});
			this.blockBuilder.addEmptyLine();
			// }
		});
		// @formatter:on
		return this.blockBuilder;
	}

	@Override
	public SingleMethodBuilder variable(String name, String className) {
		this.blockBuilder.addIndented(className).appendWithSpace(name);
		return this;
	}

	private MethodMetadata pickMetadatum() {
		return this.methodMetadata.stream().filter(Acceptor::accept).findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"No matching method metadata found"));
	}

	private boolean visit(List<? extends MethodVisitor> list,
			SingleContractMetadata metaData) {
		return visit(list, metaData, true);
	}

	private boolean visit(List<? extends MethodVisitor> list,
			SingleContractMetadata metaData, boolean addLineEnding) {
		List<? extends MethodVisitor> visitors = list.stream()
				.filter(o -> o.accept(metaData)).collect(Collectors.toList());
		Iterator<? extends MethodVisitor> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			MethodVisitor visitor = iterator.next();
			visitor.apply(metaData, this);
			if (addLineEnding) {
				this.blockBuilder.addEndingIfNotPresent();
			}
			if (iterator.hasNext()) {
				this.blockBuilder.addEmptyLine();
			}
		}
		return !visitors.isEmpty();
	}

}
