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

import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.util.SerializationUtils;

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @since 2.1.0
 */
interface BodyMethodGeneration {

	default Object cloneBody(Object object) {
		byte[] serializedObject = SerializationUtils.serialize(object);
		return SerializationUtils.deserialize(serializedObject);
	}

	default void addBodyMatchingBlock(List<BodyMatcher> matchers,
			MethodBodyWriter methodBodyWriter, Object responseBody) {
		methodBodyWriter.inAndBlock(() -> {
			matchers.forEach(it -> {
				if (it.matchingType() == MatchingType.NULL) {
					methodForNullCheck(it);
				}
				else if (MatchingType.regexRelated(it.matchingType())
						|| it.matchingType() == MatchingType.EQUALITY) {
					methodForEqualityCheck(it, responseBody);
				}
				else if (it.matchingType() == MatchingType.COMMAND) {
					methodForCommandExecution(it, responseBody);
				}
				else {
					methodForTypeCheck(it, responseBody);
				}
			});
		});
	}

	default String quotedAndEscaped(String string) {
		return '"' + StringEscapeUtils.escapeJava(string) + '"';
	}

	void methodForNullCheck(BodyMatcher bodyMatcher);

	void methodForEqualityCheck(BodyMatcher bodyMatcher, Object body);

	void methodForCommandExecution(BodyMatcher bodyMatcher, Object body);

	void methodForTypeCheck(BodyMatcher bodyMatcher, Object body);

}
