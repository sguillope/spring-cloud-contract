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

package org.springframework.cloud.contract.verifier

import spock.lang.Specification

import org.springframework.cloud.contract.verifier.builder.SingleTestGenerator
import org.springframework.cloud.contract.verifier.config.ContractVerifierConfigProperties

import static org.springframework.cloud.contract.verifier.builder.GeneratedTestClassMother.generatedTestClass
import static org.springframework.cloud.contract.verifier.config.TestFramework.SPOCK

class GeneratorScannerSpec extends Specification {

	private SingleTestGenerator classGenerator = Mock(SingleTestGenerator)

	def "should find all json files and generate 6 classes for them"() {
		given:
			File resource = new File(this.getClass().getResource("/directory/with/stubs/stubsRepositoryIndicator").toURI())
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties()
			properties.contractsDslDir = resource.parentFile
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("org.springframework.cloud.contract.verifier")
		then:
			6 * classGenerator.generateClass(_, _, _, _) >> generatedTestClass("OtherTest.java", "qwerty")
	}

	def "should create class with full package"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(testFramework: SPOCK)
			properties.contractsDslDir = new File(this.getClass().getResource("/directory/with/stubs/package").toURI())
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("org.springframework.cloud.contract.verifier")
		then:
			1 * classGenerator.generateClass(_, _, _, { SingleTestGenerator.GeneratedClassData it -> it.className == 'exceptions' && it.classPackage == 'org.springframework.cloud.contract.verifier' }) >> generatedTestClass("ExceptionsSpec.groovy", "spec")
			1 * classGenerator.generateClass(_, _, _, { SingleTestGenerator.GeneratedClassData it -> it.className == 'exceptions' && it.classPackage == 'org.springframework.cloud.contract.verifier.v1' }) >> generatedTestClass("ExceptionsSpec.groovy", "spec1")
			1 * classGenerator.generateClass(_, _, _, { SingleTestGenerator.GeneratedClassData it -> it.className == 'exceptions' && it.classPackage == 'org.springframework.cloud.contract.verifier.v2' }) >> generatedTestClass("ExceptionsSpec.groovy", "spec2")
	}

	def "should create class with name with hyphen"() {
		given:
			ContractVerifierConfigProperties properties = new ContractVerifierConfigProperties(testFramework: SPOCK)
			properties.contractsDslDir = new File(this.getClass().getResource("/directory/with/name-with-hyphen").toURI())
			TestGenerator testGenerator = new TestGenerator(properties, classGenerator, Stub(FileSaver))
		when:
			testGenerator.generateTestClasses("org.springframework.cloud.contract.verifier")
		then:
			1 * classGenerator.generateClass(_, _, _, { SingleTestGenerator.GeneratedClassData it -> it.className == 'car_rental' && it.classPackage == 'org.springframework.cloud.contract.verifier' }) >> generatedTestClass("CarRentalSpec.groovy", "spec")
	}

}
