/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.3.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.util.license;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

final class LicensiusClient {

	private static final Logger LOGGER=LoggerFactory.getLogger(LicensiusClient.class);

	private static final String APPLICATION_RDF_XML = "application/rdf+xml";
	private static final String APPLICATION_JSON = "application/json";

	private static final String BASE_PLACEHOLDER = "http://www.example.org/ontology#";

	private static final String LICENSIUS_ENDPOINT = "http://www.licensius.com/api/";
	private static final String FIND_LICENSE_IN_RDF = "license/findlicenseinrdf";
	private static final String GET_LICENSE_INFO = "license/getlicenseinfo";
	private static final String URI_PARAM = "?uri=";

	private static Server server;

	public static final ConcurrentMap<String,Optional<String>> RESOLVED_LICENSES=Maps.newConcurrentMap();

	private LicensiusClient() {
	}

	static synchronized void start(final String host, final int port) {
		if(LicensiusClient.server==null) {
			LicensiusClient.server=new Server(host,port);
			LicensiusClient.server.start();
			LOGGER.info("Started local server {}:{}",host,port);
		}
	}

	static synchronized void shutdown() {
		if(LicensiusClient.server!=null) {
			LicensiusClient.server.stop();
			LOGGER.info("Stopped local server");
		}
	}

	static Optional<LicenseInfo> getLicenseInfo(final String licenseId) throws IOException {
		final CloseableHttpClient client = HttpClients.createDefault();
		try {
			final HttpGet get = createGetLicenseInfoGetRequest(licenseId);
			final CloseableHttpResponse response = client.execute(get);
			try {
				return processGetLicenseInfoResponse(licenseId, response);
			} finally {
				closeQuietly(response);
			}
		} catch(final IOException e) {
			LOGGER.error("Could not retrieve information for license '{}'. Full stacktrace follows",licenseId,e);
			throw e;
		} finally {
			closeQuietly(client);
		}
	}

	static Optional<String> findLicense(final String licenseURI) throws IOException {
		final Optional<String> cached = RESOLVED_LICENSES.get(licenseURI);
		if(cached!=null) {
			LOGGER.trace("Found cached resolution '{}' for license '{}'",cached,licenseURI);
			return cached;
		}
		LOGGER.debug("License '{}' has not been resolved yet. Starting resolution...",licenseURI);
		final CloseableHttpClient client = HttpClients.createDefault();
		try {
			final String publicURI = LicensiusClient.server.publish(licenseURI);
			final HttpGet get = createFindLicenseInRDFGetRequest(publicURI);
			final CloseableHttpResponse response = client.execute(get);
			try {
				final Optional<String> result = processFindLicenseInRDFResponse(licenseURI,response);
				RESOLVED_LICENSES.putIfAbsent(publicURI, result);
				LOGGER.trace("Resolved '{}' for license '{}'",result,licenseURI);
				return result;
			} finally {
				closeQuietly(response);
			}
		} catch(final IOException e) {
			LOGGER.error("Could not find license for '{}'. Full stacktrace follows",licenseURI,e);
			throw e;
		} finally {
			closeQuietly(client);
		}
	}

	static boolean isAvailable() {
		final CloseableHttpClient client = HttpClients.createDefault();
		try {
			final HttpGet get=new HttpGet(LICENSIUS_ENDPOINT+"license/list");
			final CloseableHttpResponse response = client.execute(get);
			try {
				return response.getStatusLine().getStatusCode()==200;
			} finally {
				closeQuietly(response);
			}
		} catch (final IOException e) {
			LOGGER.trace("Licensius is not available. Root cause follows.",Throwables.getRootCause(e));
			return false;
		} finally {
			closeQuietly(client);
		}
	}

	static Optional<String> findLicensePost(final String licenseURI) throws IOException {
		final CloseableHttpClient client = HttpClients.createDefault();
		try {
			final HttpPost post = createFindLicenseInRDFPostRequest(licenseURI);
			final CloseableHttpResponse response = client.execute(post);
			try {
				return processFindLicenseInRDFResponse(licenseURI,response);
			} finally {
				closeQuietly(response);
			}
		} finally {
			closeQuietly(client);
		}
	}

	private static Optional<LicenseInfo> processGetLicenseInfoResponse(final String licenseId, final CloseableHttpResponse response) throws IOException {
		final int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode==200) {
			final LicenseInfo info = parseEntity(response, LicenseInfo.class);
			LOGGER.trace("Information for license '{}' found: {}",licenseId,info);
			return Optional.of(info);
		} else if(statusCode==404) {
			LOGGER.trace("Unknown license '{}'",licenseId);
			return Optional.absent();
		} else {
			final Failure failure=parseEntity(response, Failure.class);
			throw new IOException("Licensius server failed to provide information for '"+licenseId+"' : "+failure.getMessage()+" ("+failure.getCode()+")");
		}
	}

	private static Optional<String> processFindLicenseInRDFResponse(final String licenseURI, final CloseableHttpResponse response) throws IOException {
		final int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode==200) {
			final List<Guess> guesses = parseEntityList(response, Guess.class);
			final Optional<Guess> bestGuess = findBestAlternative(guesses);
			if(!guesses.isEmpty()) {
				LOGGER.trace("Could guess details for license '{}': {}",licenseURI,bestGuess.orNull());
			} else {
				LOGGER.trace("Could not guess details for license '{}'",licenseURI);
			}
			return getLicense(bestGuess);
		} else {
			final Failure failure=parseEntity(response, Failure.class);
			throw new IOException("Licensius server failed to find information for '"+licenseURI+"' : "+failure.getMessage()+" ("+failure.getCode()+")");
		}
	}

	private static Optional<String> getLicense(final Optional<Guess> best) {
		String guessed=null;
		if(best.isPresent()) {
			guessed=best.orNull().getLicense();
		}
		return Optional.fromNullable(guessed);
	}

	private static Optional<Guess> findBestAlternative(final List<Guess> guesses) throws IOException {
		Guess best=null;
		if(!guesses.isEmpty()) {
			for(final Guess guess:guesses) {
				if(best==null) {
					best=guess;
				} else if(isBetter(best.getConfidence(),guess.getConfidence())) {
					best=guess;
				}
			}
		}
		return Optional.fromNullable(best);
	}

	private static boolean isBetter(final String bestConfidence, final String guessConfidence) throws IOException {
		try {
			return Double.compare(Double.parseDouble(bestConfidence), Double.parseDouble(guessConfidence))>0;
		} catch (final NumberFormatException e) {
			throw new IOException("Invalid confidence value",e);
		}
	}

	private static HttpGet createGetLicenseInfoGetRequest(final String licenseId){
		return createGetMethod(GET_LICENSE_INFO, licenseId);
	}

	private static HttpGet createFindLicenseInRDFGetRequest(final String licenseURI) {
		return createGetMethod(FIND_LICENSE_IN_RDF, licenseURI);
	}

	private static HttpPost createFindLicenseInRDFPostRequest(final String licenseURI) {
		final HttpPost post=new HttpPost(LICENSIUS_ENDPOINT+FIND_LICENSE_IN_RDF);
		post.addHeader(HttpHeaders.ACCEPT,APPLICATION_JSON);
		post.setEntity(
			new StringEntity(
				Server.createMockOntology(BASE_PLACEHOLDER, licenseURI),
				ContentType.create(APPLICATION_RDF_XML, StandardCharsets.UTF_8)));
		return post;
	}

	private static HttpGet createGetMethod(final String operation, final String parameter) throws AssertionError {
		try {
			final HttpGet get=new HttpGet(LICENSIUS_ENDPOINT+operation+URI_PARAM+URLEncoder.encode(parameter,"UTF-8"));
			get.addHeader(HttpHeaders.ACCEPT,APPLICATION_JSON);
			return get;
		} catch (final UnsupportedEncodingException e) {
			throw new AssertionError("Should be able to encode '"+parameter+"' to UTF-8", e);
		}
	}

	private static <T> T parseEntity(final CloseableHttpResponse response, final Class<? extends T> valueType) throws IOException {
		try {
			return
				new ObjectMapper().
					readValue(
						EntityUtils.toString(response.getEntity()),
						valueType);
		} catch (final IOException e) {
			throw new IOException("Could not process response ",e);
		}
	}

	private static <T> List<T> parseEntityList(final CloseableHttpResponse response, final Class<? extends T> class1) throws IOException {
		try {
			final String responseBody = EntityUtils.toString(response.getEntity());
			final String fixedResponseBody = responseBody.replaceAll(",(\\n|\\r\\n)\\]","\\]");
			return
				new ObjectMapper().
					readValue(
					fixedResponseBody,
					TypeFactory.
						defaultInstance().
							constructCollectionType(List.class,Guess.class));
		} catch (final IOException e) {
			throw new IOException("Could not process response ",e);
		}
	}

	private static void closeQuietly(final Closeable client) {
		try {
			client.close();
		} catch (final IOException e) {
			// Ignore
		}
	}
}
