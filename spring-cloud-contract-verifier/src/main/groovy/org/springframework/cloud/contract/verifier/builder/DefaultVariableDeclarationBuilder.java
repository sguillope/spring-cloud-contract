/*
 * Copyright 2019-2019 the original author or authors.
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

/**
 * @author Tim Ysewyn
 */
class DefaultVariableDeclarationBuilder implements VariableDeclarationBuilder {

	protected final String name;

	protected final String className;

	protected final MethodBodyWriter methodBuilder;

	DefaultVariableDeclarationBuilder(String name, String className,
			MethodBodyWriter methodBuilder) {
		this.name = name;
		this.className = className;
		this.methodBuilder = methodBuilder;
	}

	@Override
	public MethodBodyWriter assignValue(String value) {
		declareVariable(true).appendWithSpace("=").appendWithSpace(value)
				.addEndingIfNotPresent().addEmptyLine();
		return methodBuilder;
	}

	@Override
	public MethodBodyWriter assignValue() {
		declareVariable(true).appendWithSpace("= ");
		return methodBuilder;
	}

	protected BlockBuilder declareVariable(boolean hasValue) {
		return methodBuilder.blockBuilder().addIndented(className).appendWithSpace(name);
	}

}
