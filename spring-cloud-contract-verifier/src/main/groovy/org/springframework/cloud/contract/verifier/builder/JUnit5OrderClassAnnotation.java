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

import org.springframework.cloud.contract.verifier.config.TestFramework;

class JUnit5OrderClassAnnotation implements ClassAnnotation {

	private final GeneratedClassMetaData generatedClassMetaData;

	JUnit5OrderClassAnnotation(GeneratedClassMetaData generatedClassMetaData) {
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public String annotation() {
		// return "@FixMethodOrder(MethodSorters.NAME_ASCENDING)";
		throw new UnsupportedOperationException(
				"Not implemented yet in JUnit5 - https://github.com/junit-team/junit5/issues/48");
	}

	@Override
	public boolean accept() {
		return this.generatedClassMetaData.configProperties
				.getTestFramework() == TestFramework.JUNIT5
				&& this.generatedClassMetaData.listOfFiles.stream()
						.anyMatch(meta -> meta.getOrder() != null);
	}

}
