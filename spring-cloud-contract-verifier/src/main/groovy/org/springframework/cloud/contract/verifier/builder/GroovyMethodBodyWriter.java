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

/**
 * Writes the method body, with Groovy specific differences.
 *
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class GroovyMethodBodyWriter implements MethodBodyWriter, DefaultMethodBodyWriter {

	private final BlockBuilder blockBuilder;

	GroovyMethodBodyWriter(BlockBuilder blockBuilder) {
		this.blockBuilder = blockBuilder;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return blockBuilder;
	}

	@Override
	public MethodBodyWriter inGivenBlock(Runnable inGivenBlock) {
		return withIndentation().append("given:").addNewLine().inBlock(inGivenBlock);
	}

	@Override
	public MethodBodyWriter inWhenBlock(Runnable inWhenBlock) {
		return withIndentation().append("when:").addNewLine().inBlock(inWhenBlock);
	}

	@Override
	public MethodBodyWriter inThenBlock(Runnable inThenBlock) {
		return withIndentation().append("then:").addNewLine().inBlock(inThenBlock);
	}

	@Override
	public MethodBodyWriter inAndBlock(Runnable inAndBlock) {
		// An "and" block is run within a "then" block, so we need to make sure we don't
		// indent again
		blockBuilder.endBlock().addIndented("and:").startBlock();
		return addNewLine().inBlock(0, inAndBlock);
	}

}
