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

import org.springframework.cloud.contract.spec.internal.FromFileProperty;
import org.springframework.cloud.contract.spec.internal.NamedProperty;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

import static org.springframework.cloud.contract.verifier.util.ContentUtils.kotlinNamedPropertyValue;

class KotlinMockMvcMultipartGiven extends MockMvcMultipartGiven {

	KotlinMockMvcMultipartGiven(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		super(methodBodyWriter, generatedClassMetaData, bodyParser);
	}

	protected String multipartContent(NamedProperty namedProperty,
			SingleContractMetadata metadata) {
		return kotlinNamedPropertyValue(namedProperty, quote(),
				(FromFileProperty property) -> bodyReader().readBytesFromFileString(
						metadata, property, CommunicationType.REQUEST));
	}

}
