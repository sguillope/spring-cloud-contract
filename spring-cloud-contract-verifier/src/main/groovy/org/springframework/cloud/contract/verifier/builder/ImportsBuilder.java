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

class ImportsBuilder {

	private final GeneratedTestClassBuilder parentBuilder;

	private final BlockBuilder builder;

	private final GeneratedClassMetaData metaData;

	ImportsBuilder(GeneratedTestClassBuilder generatedTestClassBuilder) {
		this.parentBuilder = generatedTestClassBuilder;
		this.builder = generatedTestClassBuilder.blockBuilder;
		this.metaData = generatedTestClassBuilder.generatedClassMetaData;
	}

	ImportsBuilder defaultImports() {
		this.parentBuilder.imports(new DefaultImports(metaData));
		this.parentBuilder.staticImports(new DefaultStaticImports());
		return this;
	}

	ImportsBuilder custom() {
		this.parentBuilder.imports(new CustomImports(metaData));
		this.parentBuilder.staticImports(new CustomStaticImports(metaData));
		return this;
	}

	ImportsBuilder json() {
		this.parentBuilder.imports(new JsonPathImports(metaData));
		this.parentBuilder.staticImports(new DefaultJsonStaticImports(metaData));
		return this;
	}

	ImportsBuilder xml() {
		this.parentBuilder.imports(new XmlImports(metaData));
		return this;
	}

	ImportsBuilder jUnit4() {
		this.parentBuilder.imports(new JUnit4Imports(metaData),
				new JUnit4IgnoreImports(metaData), new JUnit4OrderImports(metaData));
		return this;
	}

	ImportsBuilder jUnit5() {
		this.parentBuilder.imports(new JUnit5Imports(metaData),
				new JUnit5IgnoreImports(metaData), new JUnit5OrderImports(metaData));
		return this;
	}

	ImportsBuilder testNG() {
		this.parentBuilder.imports(new TestNGImports(metaData));
		return this;
	}

	ImportsBuilder spock() {
		this.parentBuilder.imports(new SpockImports(metaData),
				new SpockIgnoreImports(metaData), new SpockOrderImports(metaData));
		return this;
	}

	ImportsBuilder messaging() {
		this.parentBuilder.imports(new MessagingImports(metaData));
		this.parentBuilder.staticImports(new MessagingStaticImports(metaData));
		return this;
	}

	ImportsBuilder restAssured() {
		this.parentBuilder.imports(new MockMvcRestAssuredImports(metaData),
				new ExplicitRestAssuredImports(metaData),
				new WebTestClientRestAssuredImports(metaData));
		this.parentBuilder.staticImports(new MockMvcRestAssuredStaticImports(metaData),
				new ExplicitRestAssuredStaticImports(metaData),
				new WebTestClientRestAssured3StaticImports(metaData));
		return this;
	}

	ImportsBuilder jaxRs() {
		this.parentBuilder.imports(new JaxRsImports(metaData));
		this.parentBuilder.staticImports(new JaxRsStaticImports(metaData));
		return this;
	}

	ImportsBuilder customImports(List<Imports> imports) {
		this.parentBuilder.imports(imports);
		return this;
	}

	ImportsBuilder customStaticImports(List<Imports> imports) {
		this.parentBuilder.staticImports(imports);
		return this;
	}

	GeneratedTestClassBuilder build() {
		return this.parentBuilder;
	}

}
