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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the body of the class. Sets fields, methods.
 *
 * @author Olga Maciaszek-Sharma
 * @author Marcin Grzejszczak
 * @author Tim Ysewyn
 * @since 2.2.0
 */
interface DefaultClassBodyBuilder extends ClassBodyBuilder {

	BlockBuilder blockBuilder();

	SingleMethodBuilder methodBuilder();

	List<? extends Fields> fields();

	default BlockBuilder build() {
		blockBuilder().inBraces(() -> {
			// @Rule ...
			addFields(fields());
			// new line if fields added
			methodBuilder().build();
		});
		return blockBuilder();
	}

	default void addFields(List<? extends Fields> list) {
		List<? extends Fields> visitors = list.stream().filter(Acceptor::accept)
				.collect(Collectors.toList());
		Iterator<? extends Fields> iterator = visitors.iterator();
		while (iterator.hasNext()) {
			iterator.next().fields().forEach(this::addField);
		}
	}

	default void addField(Field field) {
		field.getAnnotations()
				.forEach(annotation -> blockBuilder().addLine("@" + annotation));
		// @formatter:off
		blockBuilder()
				.addIndented(field.getClassName())
				.appendWithSpace(field.getName())
				.addEndingIfNotPresent();
		// @formatter:on
		blockBuilder().addEmptyLine();
	}

}
