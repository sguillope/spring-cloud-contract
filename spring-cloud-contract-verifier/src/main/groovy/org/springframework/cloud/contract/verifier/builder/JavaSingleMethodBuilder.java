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
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class JavaSingleMethodBuilder implements SingleMethodBuilder {

	private List<MethodAnnotations> methodAnnotations = new LinkedList<>();

	private List<MethodMetadata> methodMetadata = new LinkedList<>();

	private List<Given> givens = new LinkedList<>();

	private List<When> whens = new LinkedList<>();

	private List<Then> thens = new LinkedList<>();

	private final MethodBodyWriter methodBodyWriter;

	final GeneratedClassMetaData generatedClassMetaData;

	final BlockBuilder blockBuilder;

	private JavaSingleMethodBuilder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
		this.methodBodyWriter = new JavaMethodBodyWriter(blockBuilder);
	}

	public static JavaSingleMethodBuilder builder(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		return new JavaSingleMethodBuilder(blockBuilder, generatedClassMetaData);
	}

	public JavaSingleMethodBuilder methodAnnotation(MethodAnnotations methodAnnotations) {
		this.methodAnnotations.add(methodAnnotations);
		return this;
	}

	public MethodAnnotationBuilder methodAnnotation() {
		return new MethodAnnotationBuilder(this);
	}

	public JavaSingleMethodBuilder methodAnnotation(
			MethodAnnotations... methodAnnotations) {
		this.methodAnnotations.addAll(Arrays.asList(methodAnnotations));
		return this;
	}

	public MethodMetadataBuilder methodMetadata() {
		return new MethodMetadataBuilder(this);
	}

	public JavaSingleMethodBuilder methodMetadata(MethodMetadata... methodMetadata) {
		this.methodMetadata.addAll(Arrays.asList(methodMetadata));
		return this;
	}

	public JavaSingleMethodBuilder restAssured() {
		return given(new RestAssuredGiven(this.methodBodyWriter,
				this.generatedClassMetaData, RestAssuredBodyParser.INSTANCE))
						.when(new RestAssuredWhen(this.methodBodyWriter,
								this.generatedClassMetaData,
								RestAssuredBodyParser.INSTANCE))
						.then(new RestAssuredThen(this.methodBodyWriter,
								this.generatedClassMetaData,
								RestAssuredBodyParser.INSTANCE,
								ComparisonBuilder.JAVA_HTTP_INSTANCE));
	}

	public JavaSingleMethodBuilder jaxRs() {
		return given(new JaxRsGiven(this.generatedClassMetaData))
				.when(new JaxRsWhen(this.methodBodyWriter, this.generatedClassMetaData,
						JaxRsBodyParser.INSTANCE))
				.then(new JaxRsThen(this.methodBodyWriter, this.generatedClassMetaData,
						JaxRsBodyParser.INSTANCE, ComparisonBuilder.JAVA_HTTP_INSTANCE));
	}

	public JavaSingleMethodBuilder messaging() {
		// @formatter:off
		return given(new MessagingGiven(this.methodBodyWriter, this.generatedClassMetaData, JavaMessagingBodyParser.INSTANCE))
				.when(new MessagingWhen(this.methodBodyWriter))
				.then(new MessagingWithBodyThen(this.methodBodyWriter,
						this.generatedClassMetaData, ComparisonBuilder.JAVA_MESSAGING_INSTANCE));
		// @formatter:on
	}

	public JavaSingleMethodBuilder given(Given... given) {
		this.givens.addAll(Arrays.asList(given));
		return this;
	}

	public JavaSingleMethodBuilder when(When... when) {
		this.whens.addAll(Arrays.asList(when));
		return this;
	}

	public JavaSingleMethodBuilder then(Then... then) {
		this.thens.addAll(Arrays.asList(then));
		return this;
	}

	@Override
	public BlockBuilder blockBuilder() {
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
				// (indent) then
				visit(this.thens, metaData);
			});
			this.blockBuilder.addEmptyLine();
			// }
		});
		// @formatter:on
		return this.blockBuilder;
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
			visitor.apply(metaData);
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
