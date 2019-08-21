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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Tim Ysewyn
 */
class DefaultInstanceBuilder implements InstanceBuilder {

	protected final String className;

	protected final MethodBodyWriter methodBodyWriter;

	private List<Runnable> arguments = new LinkedList<>();

	DefaultInstanceBuilder(String className, MethodBodyWriter methodBodyWriter) {
		this.className = className;
		this.methodBodyWriter = methodBodyWriter;
	}

	@Override
	public InstanceBuilder withArgument(String argument) {
		return withArgument(() -> methodBodyWriter.append(argument));
	}

	@Override
	public InstanceBuilder withArgument(Runnable argument) {
		arguments.add(argument);
		return this;
	}

	@Override
	public MethodBodyWriter instantiateAndEndStatement() {
		instantiate().blockBuilder().addEndingIfNotPresent().addEmptyLine();
		return methodBodyWriter;
	}

	@Override
	public MethodBodyWriter instantiate() {
		// @formatter:off
		methodBodyWriter
				.append(instantiateStatement())
				.append("(");
		// @formatter:on
		if (!arguments.isEmpty()) {
			Iterator<Runnable> it = arguments.iterator();
			while (it.hasNext()) {
				it.next().run();
				if (it.hasNext()) {
					methodBodyWriter.append(", ");
				}
			}
		}
		methodBodyWriter.blockBuilder().append(")");
		return methodBodyWriter;
	}

	protected String instantiateStatement() {
		return "new " + className;
	}

	@Override
	public MethodBodyWriter instantiateAnd() {
		return instantiate();
	}

}
