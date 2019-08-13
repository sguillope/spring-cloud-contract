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
import org.springframework.core.KotlinDetector;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Tim Ysewyn
 * @since 2.2.0
 */
class KotlinClassMetaData implements ClassMetaData, DefaultClassMetadata {

	private static boolean kotlinSupportMaven = false;

	private static boolean kotlinSupportGradle = false;

	static {
		ClassLoader classLoader = KotlinClassMetaData.class.getClassLoader();
		try {
			ClassUtils.forName(
					"org.springframework.cloud.contract.maven.verifier.GenerateTestsMojo",
					classLoader);
			kotlinSupportMaven = KotlinDetector.isKotlinPresent();
		}
		catch (ClassNotFoundException ex) {
		}
		try {
			ClassUtils.forName(
					"org.springframework.cloud.contract.verifier.plugin.GenerateServerTestsTask",
					classLoader);
			// We need to be sure that the kotlin-gradle-plugin is present
			// KotlinDetector.isKotlinPresent() can't be used with Gradle
			// since it uses a different classpath during compilation/testing
			ClassUtils.forName("org.jetbrains.kotlin.gradle.tasks.KotlinCompile",
					classLoader);
			kotlinSupportGradle = true;
		}
		catch (ClassNotFoundException ex) {
		}
	}

	public static boolean hasKotlinSupport() {
		return kotlinSupportMaven || kotlinSupportGradle;
	}

	private final BlockBuilder blockBuilder;

	private final BaseClassProvider baseClassProvider = new BaseClassProvider();

	private final GeneratedClassMetaData generatedClassMetaData;

	KotlinClassMetaData(BlockBuilder blockBuilder,
			GeneratedClassMetaData generatedClassMetaData) {
		this.blockBuilder = blockBuilder;
		this.generatedClassMetaData = generatedClassMetaData;
	}

	@Override
	public ClassMetaData modifier() {
		return this;
	}

	@Override
	public ClassMetaData suffix() {
		String suffix = this.getClassNameSuffix();
		if (!this.blockBuilder.endsWith(suffix)) {
			this.blockBuilder.addAtTheEnd(suffix);
		}
		return this;
	}

	@Override
	public ClassMetaData setupLineEnding() {
		return this;
	}

	@Override
	public ClassMetaData setupLabelPrefix() {
		this.blockBuilder.setupLabelPrefix("// ");
		return this;
	}

	@Override
	public ClassBuilder classDefinition() {
		return KotlinClassBuilder.builder(this);
	}

	@Override
	public ClassBodyBuilder classBody() {
		return KotlinClassBodyBuilder.builder(this.blockBuilder,
				this.generatedClassMetaData);
	}

	@Override
	public GeneratedClassMetaData generatedClassMetaData() {
		return this.generatedClassMetaData;
	}

	@Override
	public BaseClassProvider baseClassProvider() {
		return this.baseClassProvider;
	}

	@Override
	public BlockBuilder blockBuilder() {
		return this.blockBuilder;
	}

	@Override
	public ClassMetaData parentClass() {
		String baseClass = fqnBaseClass();
		if (StringUtils.hasText(baseClass)) {
			int lastIndexOf = baseClass.lastIndexOf(".");
			if (lastIndexOf > 0) {
				baseClass = baseClass.substring(lastIndexOf + 1);
			}
			blockBuilder().append(": ").append(baseClass).append("() ");
		}
		return this;
	}

	@Override
	public boolean accept() {
		return hasKotlinSupport() && (this.generatedClassMetaData.configProperties
				.getTestFramework() != TestFramework.SPOCK);
	}

	@Override
	public String getClassNameSuffix() {
		return StringUtils.hasText(
				this.generatedClassMetaData.configProperties.getNameSuffixForTests())
						? this.generatedClassMetaData.configProperties
								.getNameSuffixForTests()
						: "Test";
	}

	@Override
	public String getFileExtension() {
		return ".kt";
	}

}
