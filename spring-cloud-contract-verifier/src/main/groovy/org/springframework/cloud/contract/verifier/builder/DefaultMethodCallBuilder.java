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
class DefaultMethodCallBuilder implements MethodCallBuilder {

	private final String methodName;

	private final MethodBodyWriter methodBodyWriter;

	private List<Runnable> parameters = new LinkedList<>();

	private boolean callMade = false;

	DefaultMethodCallBuilder(String methodName, MethodBodyWriter methodBodyWriter) {
		this.methodName = methodName;
		this.methodBodyWriter = methodBodyWriter;
	}

	@Override
	public MethodCallBuilder withParameter(String parameter) {
		if (parameter == null) {
			return this;
		}
		return withParameter(() -> methodBodyWriter.append(parameter));
	}

	@Override
	public MethodCallBuilder withParameter(Runnable parameter) {
		if (callMade)
			throw new IllegalStateException(
					"You can't add a parameter when the call has already been made");
		this.parameters.add(parameter);
		return this;
	}

	@Override
	public MethodBodyWriter closeCallAndEndStatement() {
		makeCall().blockBuilder().addEndingIfNotPresent().addEmptyLine();
		return methodBodyWriter;
	}

	@Override
	public MethodBodyWriter closeCall() {
		return makeCall();
	}

	@Override
	public MethodBodyWriter closeCallAnd() {
		return closeCall();
	}

	private MethodBodyWriter makeCall() {
		callMade = true;
		methodBodyWriter.append(methodName).append("(");
		if (!parameters.isEmpty()) {
			Iterator<Runnable> it = parameters.iterator();
			while (it.hasNext()) {
				it.next().run();
				if (it.hasNext()) {
					methodBodyWriter.append(", ");
				}
			}
		}
		methodBodyWriter.append(")");
		return methodBodyWriter;
	}

}
