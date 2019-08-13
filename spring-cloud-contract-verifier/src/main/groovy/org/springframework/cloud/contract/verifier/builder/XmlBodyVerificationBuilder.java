package org.springframework.cloud.contract.verifier.builder;

import java.util.List;
import java.util.Optional;

import org.springframework.cloud.contract.spec.Contract;
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

	private final Contract contract;

	private final Optional<String> lineSuffix;

	XmlBodyVerificationBuilder(Contract contract, Optional<String> lineSuffix) {
		this.contract = contract;
		this.lineSuffix = lineSuffix;
	}

	void addXmlResponseBodyCheck(BlockBuilder blockBuilder,
			SingleMethodBuilder methodBuilder, Object responseBody,
			BodyMatchers bodyMatchers, String responseString,
			boolean shouldCommentOutBDDBlocks) {
		addXmlProcessingLines(blockBuilder, methodBuilder, responseString);
		Object processedBody = XmlToXPathsConverter.removeMatchingXPaths(responseBody,
				bodyMatchers);
		List<BodyMatcher> matchers = new XmlToXPathsConverter()
				.mapToMatchers(processedBody);
		if (bodyMatchers != null && bodyMatchers.hasMatchers()) {
			matchers.addAll(bodyMatchers.matchers());
		}

		addBodyMatchingBlock(matchers, blockBuilder, responseBody,
				shouldCommentOutBDDBlocks);
	}

	private void addXmlProcessingLines(final BlockBuilder blockBuilder,
			SingleMethodBuilder methodBuilder, String responseString) {
		methodBuilder.variable("documentBuilder", "DocumentBuilder");
		blockBuilder
				.appendWithSpace(
						"= DocumentBuilderFactory.newInstance().newDocumentBuilder()")
				.addEndingIfNotPresent().addEmptyLine();
		methodBuilder.variable("parsedXml", "Document");
		String value = KotlinClassMetaData.hasKotlinSupport()
				? "documentBuilder.parse(InputSource(StringReader(" + responseString
						+ ")))"
				: "documentBuilder.parse(new InputSource(new StringReader("
						+ responseString + ")))";
		blockBuilder.appendWithSpace("=").appendWithSpace(value).addEndingIfNotPresent()
				.addEmptyLine();
	}

	@Override
	public void methodForNullCheck(BodyMatcher bodyMatcher, BlockBuilder bb) {
		String quotedAndEscapedPath = quotedAndEscaped(bodyMatcher.path());
		String method = "assertThat(nodeFromXPath(parsedXml, " + quotedAndEscapedPath
				+ ")).isNull()";
		bb.addLine(method.replace("$", "\\$"));
		addColonIfRequired(lineSuffix, bb);
	}

	@Override
	public void methodForEqualityCheck(BodyMatcher bodyMatcher, BlockBuilder bb,
			Object body) {
		Object retrievedValue = quotedAndEscaped(
				XmlToXPathsConverter.retrieveValue(bodyMatcher, body));
		String comparisonMethod = bodyMatcher.matchingType().equals(MatchingType.EQUALITY)
				? "isEqualTo" : "matches";
		String method = "assertThat(valueFromXPath(parsedXml, "
				+ quotedAndEscaped(bodyMatcher.path()) + "))." + comparisonMethod + "("
				+ retrievedValue + ")";
		bb.addLine(method.replace("$", "\\$"));
		addColonIfRequired(lineSuffix, bb);
	}

	@Override
	public void methodForCommandExecution(BodyMatcher bodyMatcher, BlockBuilder bb,
			Object body) {
		Object retrievedValue = quotedAndEscaped(
				XmlToXPathsConverter.retrieveValueFromBody(bodyMatcher.path(), body));
		ExecutionProperty property = (ExecutionProperty) bodyMatcher.value();
		bb.addLine(property.insertValue(((String) retrievedValue).replace("$", "\\$")));
		addColonIfRequired(lineSuffix, bb);
	}

	@Override
	public void methodForTypeCheck(BodyMatcher bodyMatcher, BlockBuilder bb,
			Object copiedBody) {
		throw new UnsupportedOperationException(
				"The `getNodeValue()` method in `org.w3c.dom.Node` always returns String.");
	}

}
