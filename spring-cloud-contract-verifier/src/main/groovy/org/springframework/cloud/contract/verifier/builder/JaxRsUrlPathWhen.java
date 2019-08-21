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

import org.springframework.cloud.contract.spec.internal.DslProperty;
import org.springframework.cloud.contract.spec.internal.ExecutionProperty;
import org.springframework.cloud.contract.spec.internal.MatchingStrategy;
import org.springframework.cloud.contract.spec.internal.QueryParameter;
import org.springframework.cloud.contract.spec.internal.QueryParameters;
import org.springframework.cloud.contract.spec.internal.Request;
import org.springframework.cloud.contract.verifier.file.SingleContractMetadata;
import org.springframework.cloud.contract.verifier.util.MapConverter;

class JaxRsUrlPathWhen implements When, JaxRsAcceptor, QueryParamsResolver {

	private static final String QUERY_PARAM_METHOD = "queryParam";

	private final GeneratedClassMetaData generatedClassMetaData;

	protected final MethodBodyWriter methodBodyWriter;

	JaxRsUrlPathWhen(MethodBodyWriter methodBodyWriter, GeneratedClassMetaData metaData) {
		this.methodBodyWriter = methodBodyWriter;
		this.generatedClassMetaData = metaData;
	}

	@Override
	public MethodVisitor<When> apply(SingleContractMetadata metadata) {
		Request request = metadata.getContract().getRequest();
		// @formatter:off
		methodBodyWriter
				.withIndentation().continueWithNewMethodCall("path")
					.withParameter(urlFrom(request))
				.closeCall();
		// @formatter:on
		appendQueryParametersForRequest(request);
		return this;
	}

	private String urlFrom(Request request) {
		if (request.getUrl() != null) {
			return concreteUrl(request.getUrl());
		}
		else if (request.getUrlPath() != null) {
			return concreteUrl(request.getUrlPath());
		}
		throw new IllegalStateException("No URL specified for request");
	}

	private String concreteUrl(DslProperty url) {
		Object testSideUrl = MapConverter.getTestSideValues(url);
		if (!(testSideUrl instanceof ExecutionProperty)) {
			return '"' + testSideUrl.toString() + '"';
		}
		return testSideUrl.toString();
	}

	private void appendQueryParametersForRequest(Request request) {
		QueryParameters queryParameters = null;
		if (request.getUrl() != null) {
			queryParameters = request.getUrl().getQueryParameters();
		}
		else if (request.getUrlPath() != null) {
			queryParameters = request.getUrlPath().getQueryParameters();
		}
		if (queryParameters == null || queryParameters.getParameters().isEmpty()) {
			return;
		}
		appendQueryParameters(queryParameters);
	}

	private void appendQueryParameters(QueryParameters queryParameters) {
		methodBodyWriter.addEmptyLine();
		Iterator<QueryParameter> iterator = queryParameters.getParameters().stream()
				.filter(this::allowedQueryParameter).iterator();
		while (iterator.hasNext()) {
			QueryParameter param = iterator.next();
			// @formatter:off
			methodBodyWriter
					.withIndentation()
					.continueWithNewMethodCall(QUERY_PARAM_METHOD)
						.withParameter("\"" + param.getName() + "\"")
						.withParameter("\"" + resolveParamValue(param) + "\"")
					.closeCall();
			// @formatter:on
			if (iterator.hasNext()) {
				methodBodyWriter.addEmptyLine();
			}
		}
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	private boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.getServerValue());
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	private boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.getType() != MatchingStrategy.Type.ABSENT;
	}

	/**
	 * @return {@code true} if the query parameter is allowed
	 */
	private boolean allowedQueryParameter(Object o) {
		if (o instanceof QueryParameter) {
			return allowedQueryParameter((QueryParameter) o);
		}
		else if (o instanceof MatchingStrategy) {
			return allowedQueryParameter((MatchingStrategy) o);
		}
		else if (o instanceof DslProperty) {
			return allowedQueryParameter(((DslProperty) o).getServerValue());
		}
		return true;
	}

	@Override
	public boolean accept(SingleContractMetadata metadata) {
		return acceptType(this.generatedClassMetaData, metadata);
	}

}
