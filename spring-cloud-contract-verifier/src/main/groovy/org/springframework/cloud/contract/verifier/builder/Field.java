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

import java.util.Collections;
import java.util.List;

class Field {

	private final List<String> annotations;

	private final String className;

	private final String name;

	private List<String> parameterizedTypes = Collections.emptyList();

	private String value = null;

	Field(String className, String name) {
		this(Collections.emptyList(), className, name);
	}

	Field(List<String> annotations, String className, String name) {
		this.annotations = annotations;
		this.className = className;
		this.name = name;
	}

	Field withParameterizedTypes(List<String> parameterizedTypes) {
		this.parameterizedTypes = parameterizedTypes;
		return this;
	}

	Field withValue(String value) {
		this.value = value;
		return this;
	}

	List<String> getAnnotations() {
		return this.annotations;
	}

	String getClassName() {
		return this.className;
	}

	String getName() {
		return this.name;
	}

	boolean hasParameterizedTypes() {
		return !this.parameterizedTypes.isEmpty();
	}

	List<String> getParameterizedTypes() {
		return this.parameterizedTypes;
	}

	void setParameterizedTypes(List<String> parameterizedTypes) {
		this.parameterizedTypes = parameterizedTypes;
	}

	boolean hasValue() {
		return this.value != null;
	}

	String getValue() {
		return this.value;
	}

	void setValue(String value) {
		this.value = value;
	}

}
