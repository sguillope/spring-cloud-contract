package org.springframework.cloud.contract.verifier.builder;

import java.util.List;

import org.springframework.cloud.contract.spec.internal.BodyMatcher;
import org.springframework.cloud.contract.spec.internal.BodyMatchers;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.MatchingType;
import org.springframework.cloud.contract.verifier.util.xml.XmlToXPathsConverter;

/**
 * @author Olga Maciaszek-Sharma
 * @author Tim Ysewyn
 * @since 2.1.0
 */
class XmlBodyVerificationBuilder implements BodyMethodGeneration {

	protected final MethodBodyWriter methodBodyWriter;

	XmlBodyVerificationBuilder(MethodBodyWriter methodBodyWriter) {
		this.methodBodyWriter = methodBodyWriter;
	}

	void addXmlResponseBodyCheck(Object responseBody, BodyMatchers bodyMatchers,
			String responseString) {
		addXmlProcessingLines(responseString);
		Object processedBody = XmlToXPathsConverter.removeMatchingXPaths(responseBody,
				bodyMatchers);
		List<BodyMatcher> matchers = XmlToXPathsConverter.mapToMatchers(processedBody);
		if (bodyMatchers != null && bodyMatchers.hasMatchers()) {
			matchers.addAll(bodyMatchers.matchers());
		}

		addBodyMatchingBlock(matchers, methodBodyWriter, responseBody);
	}

	private void addXmlProcessingLines(String responseString) {
		methodBodyWriter.declareVariable("documentBuilder", "DocumentBuilder")
				.assignValue("DocumentBuilderFactory.newInstance().newDocumentBuilder()");
		// In Java: documentBuilder.parse(new InputSource(new
		// StringReader([responseString])));
		// @formatter:off
		methodBodyWriter.declareVariable("parsedXml", "Document")
				.assignValue()
				.usingVariable("documentBuilder")
				.callMethod("parse")
					.withParameter(() ->
						methodBodyWriter.createInstanceOf("InputSource", inputSource -> inputSource.withArgument(() ->
								methodBodyWriter.createInstanceOf("StringReader", stringReader -> stringReader.withArgument(responseString))
						))
					)
				.closeCallAndEndStatement();
		// @formatter:on
	}

	@Override
	public void methodForNullCheck(BodyMatcher bodyMatcher) {
		String quotedAndEscapedPath = quotedAndEscaped(bodyMatcher.path());
		String method = "assertThat(nodeFromXPath(parsedXml, " + quotedAndEscapedPath
				+ ")).isNull()";
		methodBodyWriter.addLine(method.replace("$", "\\$"));
	}

	@Override
	public void methodForEqualityCheck(BodyMatcher bodyMatcher, Object body) {
		Object retrievedValue = quotedAndEscaped(
				XmlToXPathsConverter.retrieveValue(bodyMatcher, body));
		String comparisonMethod = bodyMatcher.matchingType().equals(MatchingType.EQUALITY)
				? "isEqualTo" : "matches";
		String method = "assertThat(valueFromXPath(parsedXml, "
				+ quotedAndEscaped(bodyMatcher.path()) + "))." + comparisonMethod + "("
				+ retrievedValue + ")";
		methodBodyWriter.addLine(method.replace("$", "\\$"));
	}

	@Override
	public void methodForCommandExecution(BodyMatcher bodyMatcher, Object body) {
		String retrievedValue = quotedAndEscaped(
				XmlToXPathsConverter.retrieveValueFromBody(bodyMatcher.path(), body));
		ExecutionProperty property = (ExecutionProperty) bodyMatcher.value();
		methodBodyWriter
				.addLine(property.insertValue(retrievedValue.replace("$", "\\$")));
	}

	@Override
	public void methodForTypeCheck(BodyMatcher bodyMatcher, Object copiedBody) {
		throw new UnsupportedOperationException(
				"The `getNodeValue()` method in `org.w3c.dom.Node` always returns String.");
	}

}
