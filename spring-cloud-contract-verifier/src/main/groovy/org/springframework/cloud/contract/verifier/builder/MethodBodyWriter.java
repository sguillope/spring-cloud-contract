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
public interface MethodBodyWriter {

	BlockBuilder blockBuilder();

	MethodBodyWriter append(String text);

	MethodBodyWriter addLine(String line);

	MethodBodyWriter addComment(String comment);

	MethodBodyWriter addEmptyLine();

	// Syntactic sugar
	MethodBodyWriter addNewLine();

	MethodBodyWriter addEndingIfNotPresent();

	MethodBodyWriter withIndentation();

	VariableDeclarationBuilder declareVariable(String name, String className);

	default DefaultWithVariableBuilder usingVariable(String variableName) {
		return usingVariable(variableName, false);
	}

	DefaultWithVariableBuilder usingVariable(String variableName, boolean nullable);

	MethodCallBuilder withMethodCall(String methodName);

	MethodCallBuilder continueWithNewMethodCall(String methodName);

	InstanceBuilder createInstanceOf(String className);

	MethodBodyWriter createInstanceOf(String className,
			Consumer<InstanceBuilder> configurer);

	MethodBodyWriter inBlock(Runnable inBlock);

	MethodBodyWriter inBlock(int indents, Runnable inBlock);

	MethodBodyWriter inGivenBlock(Runnable inGivenBlock);

	MethodBodyWriter inWhenBlock(Runnable inWhenBlock);

	MethodBodyWriter inThenBlock(Runnable inThenBlock);

	MethodBodyWriter inAndBlock(Runnable inAndBlock);

}
