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
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.config.TestFramework;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;

import static org.springframework.cloud.contract.verifier.util.ContentUtils.groovyNamedPropertyValue;

class SpockMockMvcMultipartGiven extends MockMvcMultipartGiven {

	SpockMockMvcMultipartGiven(MethodBodyWriter methodBodyWriter,
			GeneratedClassMetaData generatedClassMetaData, BodyParser bodyParser) {
		super(methodBodyWriter, generatedClassMetaData, bodyParser);
	}

	@Override
	protected String quote() {
		return "'";
	}

	protected String parameterName(String parameterName) {
		return quote() + parameterName + quote();
	}

	protected String multipartContent(NamedProperty namedProperty,
			SingleContractMetadata metadata) {
		return groovyNamedPropertyValue(namedProperty, quote(),
				(FromFileProperty property) -> bodyReader().readBytesFromFileString(
						metadata, property, CommunicationType.REQUEST));
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		return request != null && request.getMultipart() != null
				&& this.generatedClassMetaData.configProperties
						.getTestFramework() == TestFramework.SPOCK;
	}

}
