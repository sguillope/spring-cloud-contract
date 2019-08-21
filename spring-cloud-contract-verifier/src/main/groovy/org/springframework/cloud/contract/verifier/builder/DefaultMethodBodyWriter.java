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

import java.util.function.Consumer;

/**
 * Writes the method body.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
interface DefaultMethodBodyWriter extends MethodBodyWriter {

	BlockBuilder blockBuilder();

	@Override
	default MethodBodyWriter append(String text) {
		blockBuilder().append(text);
		return this;
	}

	@Override
	default MethodBodyWriter addLine(String line) {
		return withIndentation().append(line).addEndingIfNotPresent().addNewLine();
	}

	@Override
	default MethodBodyWriter addComment(String comment) {
		blockBuilder().addIndentation().append("//").appendWithSpace(comment);
		return this;
	}

	@Override
	default MethodBodyWriter addEmptyLine() {
		blockBuilder().addEmptyLine();
		return this;
	}

	@Override
	default MethodBodyWriter addNewLine() {
		return addEmptyLine();
	}

	@Override
	default MethodBodyWriter addEndingIfNotPresent() {
		return this;
	}

	@Override
	default MethodBodyWriter withIndentation() {
		blockBuilder().addIndentation();
		return this;
	}

	@Override
	default VariableDeclarationBuilder declareVariable(String name, String className) {
		return new DefaultVariableDeclarationBuilder(name, className, this);
	}

	@Override
	default DefaultWithVariableBuilder usingVariable(String variableName,
			boolean nullable) {
		return new DefaultWithVariableBuilder(variableName, this);
	}

	@Override
	default MethodCallBuilder withMethodCall(String methodName) {
		return new DefaultMethodCallBuilder(methodName, this);
	}

	@Override
	default MethodCallBuilder continueWithNewMethodCall(String methodName) {
		blockBuilder().append(".");
		return withMethodCall(methodName);
	}

	@Override
	default InstanceBuilder createInstanceOf(String className) {
		return new DefaultInstanceBuilder(className, this);
	}

	@Override
	default MethodBodyWriter createInstanceOf(String className,
			Consumer<InstanceBuilder> configurer) {
		InstanceBuilder instanceBuilder = createInstanceOf(className);
		if (configurer != null) {
			configurer.accept(instanceBuilder);
		}
		return instanceBuilder.instantiate();
	}

	@Override
	default MethodBodyWriter inBlock(Runnable inBlock) {
		return inBlock(1, inBlock);
	}

	@Override
	default MethodBodyWriter inBlock(int indents, Runnable inBlock) {
		for (int i = 0; i < indents; i++) {
			blockBuilder().startBlock();
		}
		inBlock.run();
		for (int i = 0; i < indents; i++) {
			blockBuilder().endBlock();
		}
		return this;
	}

}
