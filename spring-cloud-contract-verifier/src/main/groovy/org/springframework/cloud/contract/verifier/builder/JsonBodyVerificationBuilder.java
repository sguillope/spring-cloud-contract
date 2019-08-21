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

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import groovy.json.JsonOutput;
import groovy.lang.Closure;
import org.apache.commons.beanutils.PropertyUtilsBean;

import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.spec.ContractTemplate;
import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.spec.internal.RegexProperty;
import org.springframework.cloud.contract.verifier.template.TemplateProcessor;
import org.springframework.cloud.contract.verifier.util.JsonPaths;
import org.springframework.cloud.contract.verifier.util.JsonToJsonPathsConverter;
import org.springframework.cloud.contract.verifier.util.MapConverter;

import static org.springframework.cloud.contract.verifier.util.KotlinPluginsAvailabilityChecker.hasKotlinSupport;

/**
 * @author Marcin Grzejszczak
 * @author Olga Maciaszek-Sharma
 * @since 2.1.0
 */
class JsonBodyVerificationBuilder implements BodyMethodGeneration, ClassVerifier {

	private static final String FROM_REQUEST_PREFIX = "request.";

	private static final String FROM_REQUEST_BODY = "escapejsonbody";

	private static final String FROM_REQUEST_PATH = "path";

	private final boolean assertJsonSize;

	private final TemplateProcessor templateProcessor;

	private final ContractTemplate contractTemplate;

	private final Contract contract;

	private final Function<String, String> postProcessJsonPathCall;

	protected final MethodBodyWriter methodBodyWriter;

	// FIXME
	// Passing way more arguments here than I would like to, but since we are planning a
	// major
	// refactoring of this module for Hoxton release, leaving it this way for now
	JsonBodyVerificationBuilder(MethodBodyWriter methodBodyWriter, boolean assertJsonSize,
			TemplateProcessor templateProcessor, ContractTemplate contractTemplate,
			Contract contract, Function<String, String> postProcessJsonPathCall) {
		this.methodBodyWriter = methodBodyWriter;
		this.assertJsonSize = assertJsonSize;
		this.templateProcessor = templateProcessor;
		this.contractTemplate = contractTemplate;
		this.contract = contract;
		this.postProcessJsonPathCall = postProcessJsonPathCall;
	}

	Object addJsonResponseBodyCheck(Object convertedResponseBody,
			BodyMatchers bodyMatchers, String responseString) {
		appendJsonPath(methodBodyWriter, responseString);
		DocumentContext parsedRequestBody = null;
		boolean dontParseStrings = convertedResponseBody instanceof Map;
		Closure parsingClosure = dontParseStrings ? Closure.IDENTITY
				: MapConverter.JSON_PARSING_CLOSURE;
		if (hasRequestBody()) {
			Object testSideRequestBody = MapConverter
					.getTestSideValues(contract.getRequest().getBody(), parsingClosure);
			parsedRequestBody = JsonPath.parse(testSideRequestBody);
			if (convertedResponseBody instanceof String
					&& !textContainsJsonPathTemplate(convertedResponseBody.toString())) {
				convertedResponseBody = templateProcessor.transform(contract.getRequest(),
						convertedResponseBody.toString());
			}
		}
		Object copiedBody = cloneBody(convertedResponseBody);
		convertedResponseBody = JsonToJsonPathsConverter
				.removeMatchingJsonPaths(convertedResponseBody, bodyMatchers);
		// remove quotes from fromRequest objects before picking json paths
		TestSideRequestTemplateModel templateModel = hasRequestBody()
				? TestSideRequestTemplateModel.from(contract.getRequest()) : null;
		convertedResponseBody = MapConverter.transformValues(convertedResponseBody,
				returnReferencedEntries(templateModel), parsingClosure);
		JsonPaths jsonPaths = new JsonToJsonPathsConverter(assertJsonSize)
				.transformToJsonPathWithTestsSideValues(convertedResponseBody,
						parsingClosure);
		DocumentContext finalParsedRequestBody = parsedRequestBody;
		jsonPaths.forEach(it -> {
			String method = it.method();
			method = processIfTemplateIsPresent(method, finalParsedRequestBody);
			String postProcessedMethod = templateProcessor.containsJsonPathTemplateEntry(
					method) ? method : postProcessJsonPathCall.apply(method);
			methodBodyWriter.addLine("assertThatJson(parsedJson)" + postProcessedMethod);
		});
		doBodyMatchingIfPresent(bodyMatchers, methodBodyWriter, copiedBody);
		return convertedResponseBody;
	}

	private boolean hasRequestBody() {
		return contract.getRequest() != null && contract.getRequest().getBody() != null;
	}

	private void checkType(BodyMatcher it, Object elementFromBody) {
		String classToCheck = classToCheck(elementFromBody).getName()
				+ (hasKotlinSupport() ? "::class.java" : ".class");
		String method = "assertThat("
				+ castJsonPathToClass(
						"parsedJson.read(" + quotedAndEscaped(it.path()) + ")",
						(hasKotlinSupport() ? "Any" : "Object"))
				+ ").isInstanceOf(" + classToCheck + ")";
		methodBodyWriter.addLine(postProcessJsonPathCall.apply(method));
	}

	// we want to make the type more generic (e.g. not ArrayList but List)
	private String sizeCheckMethod(BodyMatcher bodyMatcher,
			String quotedAndEscaptedPath) {
		String prefix = sizeCheckPrefix(bodyMatcher, quotedAndEscaptedPath);
		if (bodyMatcher.minTypeOccurrence() != null
				&& bodyMatcher.maxTypeOccurrence() != null) {
			return prefix + "Between(" + bodyMatcher.minTypeOccurrence() + ", "
					+ bodyMatcher.maxTypeOccurrence() + ")";
		}
		else if (bodyMatcher.minTypeOccurrence() != null) {
			return prefix + "GreaterThanOrEqualTo(" + bodyMatcher.minTypeOccurrence()
					+ ")";
		}
		else if (bodyMatcher.maxTypeOccurrence() != null) {
			return prefix + "LessThanOrEqualTo(" + bodyMatcher.maxTypeOccurrence() + ")";
		}
		return prefix;
	}

	protected void buildCustomMatchingConditionForEachElement(String path,
			String valueAsParam) {
		String classToCastTo = "java.util.Collection"
				+ (hasKotlinSupport() ? "::class.java" : ".class");
		String method = "assertThat("
				+ castJsonPathToClass(
						"parsedJson.read(" + path + ", " + classToCastTo + ")",
						(hasKotlinSupport() ? "Iterable<*>" : "java.lang.Iterable"))
				+ ").as(" + path + ").allElementsMatch(" + valueAsParam + ")";
		methodBodyWriter.addLine(postProcessJsonPathCall.apply(method));
	}

	@Override
	public void methodForEqualityCheck(BodyMatcher bodyMatcher, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path());
		Object retrievedValue = value(copiedBody, bodyMatcher);
		retrievedValue = retrievedValue instanceof RegexProperty
				? ((RegexProperty) retrievedValue).getPattern().pattern()
				: retrievedValue;
		String valueAsParam = retrievedValue instanceof String
				? quotedAndEscaped(retrievedValue.toString())
				: objectToString(retrievedValue);
		if (arrayRelated(path) && MatchingType.regexRelated(bodyMatcher.matchingType())) {
			buildCustomMatchingConditionForEachElement(path, valueAsParam);
		}
		else {
			String comparisonMethod = bodyMatcher.matchingType() == MatchingType.EQUALITY
					? "isEqualTo" : "matches";
			String classToCastTo = retrievedValue.getClass().getSimpleName()
					+ (hasKotlinSupport() ? "::class.java" : ".class");
			String method = "assertThat(parsedJson.read(" + path + ", " + classToCastTo
					+ "))." + comparisonMethod + "(" + valueAsParam + ")";
			methodBodyWriter.addLine(postProcessJsonPathCall.apply(method));
		}
	}

	private String objectToString(Object value) {
		return value instanceof Long ? String.valueOf(value).concat("L")
				: String.valueOf(value);
	}

	protected String processIfTemplateIsPresent(String method,
			DocumentContext parsedRequestBody) {
		if (textContainsJsonPathTemplate(method) && hasRequestBody()) {
			// Unquoting the values of non strings
			String jsonPathEntry = templateProcessor.jsonPathFromTemplateEntry(method);
			Object object = parsedRequestBody.read(jsonPathEntry);
			if (!(object instanceof String)) {
				return method
						.replace('"' + contractTemplate.escapedOpeningTemplate(),
								contractTemplate.escapedOpeningTemplate())
						.replace(contractTemplate.escapedClosingTemplate() + '"',
								contractTemplate.escapedClosingTemplate())
						.replace('"' + contractTemplate.openingTemplate(),
								contractTemplate.openingTemplate())
						.replace(contractTemplate.closingTemplate() + '"',
								contractTemplate.closingTemplate());
			}
		}
		return method;
	}

	@Override
	public void methodForCommandExecution(BodyMatcher bodyMatcher, Object copiedBody) {
		String path = quotedAndEscaped(bodyMatcher.path());
		// assert that path exists
		retrieveObjectByPath(copiedBody, bodyMatcher.path());
		ExecutionProperty property = (ExecutionProperty) bodyMatcher.value();
		methodBodyWriter.addLine(postProcessJsonPathCall
				.apply(property.insertValue("parsedJson.read(" + path + ")")));
	}

	@Override
	public void methodForNullCheck(BodyMatcher bodyMatcher) {
		String quotedAndEscapedPath = quotedAndEscaped(bodyMatcher.path());
		String method = "assertThat("
				+ castJsonPathToClass("parsedJson.read(" + quotedAndEscapedPath + ")",
						(hasKotlinSupport() ? "Any" : "Object"))
				+ ").isNull()";
		methodBodyWriter.addLine(postProcessJsonPathCall.apply(method));
	}

	private boolean arrayRelated(String path) {
		return path.contains("[*]") || path.contains("..");
	}

	@Override
	public void methodForTypeCheck(BodyMatcher bodyMatcher, Object copiedBody) {
		Object elementFromBody = value(copiedBody, bodyMatcher);
		if (bodyMatcher.minTypeOccurrence() != null
				|| bodyMatcher.maxTypeOccurrence() != null) {
			checkType(bodyMatcher, elementFromBody);
			String quotedAndEscaptedPath = quotedAndEscaped(bodyMatcher.path());
			String classToCastTo = "java.util.Collection"
					+ (hasKotlinSupport() ? "::class.java" : ".class");

			String method = "assertThat("
					+ castJsonPathToClass(
							"parsedJson.read(" + quotedAndEscaptedPath + ", "
									+ classToCastTo + ")",
							(hasKotlinSupport() ? "Iterable<*>" : "java.lang.Iterable"))
					+ ")." + sizeCheckMethod(bodyMatcher, quotedAndEscaptedPath);
			methodBodyWriter.addLine(postProcessJsonPathCall.apply(method));
		}
		else {
			checkType(bodyMatcher, elementFromBody);
		}
	}

	private static Object value(Object body, BodyMatcher bodyMatcher) {
		if (bodyMatcher.matchingType() == MatchingType.EQUALITY
				|| bodyMatcher.value() == null) {
			return retrieveObjectByPath(body, bodyMatcher.path());
		}
		return bodyMatcher.value();
	}

	private static Object retrieveObjectByPath(Object body, String path) {
		try {
			return JsonPath.parse(body).read(path);
		}
		catch (PathNotFoundException e) {
			throw new IllegalStateException("Entry for the provided JSON path <" + path
					+ "> doesn't exist in the body <" + JsonOutput.toJson(body) + ">", e);
		}
	}

	private Closure<Object> returnReferencedEntries(
			TestSideRequestTemplateModel templateModel) {
		return MapConverter.fromFunction(entry -> {
			if (!(entry instanceof String) || templateModel == null) {
				return entry;
			}
			String entryAsString = (String) entry;
			if (this.templateProcessor.containsTemplateEntry(entryAsString)
					&& !this.templateProcessor
							.containsJsonPathTemplateEntry(entryAsString)) {
				// TODO: HANDLEBARS LEAKING VIA request.
				String justEntry = minus(entryAsString,
						contractTemplate.escapedOpeningTemplate());
				justEntry = minus(justEntry, contractTemplate.openingTemplate());
				justEntry = minus(justEntry, contractTemplate.escapedClosingTemplate());
				justEntry = minus(justEntry, contractTemplate.closingTemplate());
				justEntry = minus(justEntry, FROM_REQUEST_PREFIX);
				if (FROM_REQUEST_BODY.equalsIgnoreCase(justEntry)) {
					// the body should be transformed by standard mechanism
					return contractTemplate.escapedOpeningTemplate() + FROM_REQUEST_PREFIX
							+ "escapedBody" + contractTemplate.escapedClosingTemplate();
				}
				try {
					Object result = new PropertyUtilsBean().getProperty(templateModel,
							justEntry);
					// Path from the Test model is an object and we'd like to return its
					// String representation
					if (FROM_REQUEST_PATH.equals(justEntry)) {
						return result.toString();
					}
					return result;
				}
				catch (Exception ignored) {
					return entry;
				}
			}
			return entry;
		});
	}

	private static String minus(CharSequence self, Object target) {
		String s = self.toString();
		String text = target.toString();
		int index = s.indexOf(text);
		if (index == -1) {
			return s;
		}
		int end = index + text.length();
		if (s.length() > end) {
			return s.substring(0, index) + s.substring(end);
		}
		return s.substring(0, index);
	}

	private boolean textContainsJsonPathTemplate(String method) {
		return templateProcessor.containsTemplateEntry(method)
				&& templateProcessor.containsJsonPathTemplateEntry(method);
	}

	/**
	 * Appends to {@link BlockBuilder} parsing of the JSON Path document
	 */
	private void appendJsonPath(MethodBodyWriter methodBodyWriter, String json) {
		// @formatter:off
		methodBodyWriter
				.declareVariable("parsedJson", "DocumentContext")
				.assignValue("JsonPath.parse(" + json + ")")
				.addEndingIfNotPresent();
		// @formatter:on
	}

	private String sizeCheckPrefix(BodyMatcher bodyMatcher, String quotedAndEscapedPath) {
		String description = "as(" + quotedAndEscapedPath + ").";
		String prefix = description + "has";
		if (arrayRelated(bodyMatcher.path())) {
			prefix = prefix + "Flattened";
		}
		return prefix + "Size";
	}

	private void doBodyMatchingIfPresent(BodyMatchers bodyMatchers,
			MethodBodyWriter methodBodyWriter, Object responseBody) {
		if (bodyMatchers != null && bodyMatchers.hasMatchers()) {
			methodBodyWriter.addEmptyLine();
			addBodyMatchingBlock(bodyMatchers.matchers(), methodBodyWriter, responseBody);
		}
	}

	private String castJsonPathToClass(String jsonPath, String clazz) {
		return hasKotlinSupport() ? jsonPath + " as " + clazz
				: "(" + clazz + ") " + jsonPath;
	}

}
