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
package org.smartdeveloperhub.vocabulary.publisher;

import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.ldp4j.http.Variant;
import org.smartdeveloperhub.vocabulary.publisher.handlers.Attachments;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeployment;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationProvider;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

final class ModuleLandingPage implements HttpHandler {

	private final DocumentationDeployment doc;
	private final DocumentationProvider provider;
	private final boolean reference;

	ModuleLandingPage(final DocumentationDeployment doc, final DocumentationProvider provider, final boolean reference) {
		this.doc = doc;
		this.provider = provider;
		this.reference = reference;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if(!Strings.isNullOrEmpty(exchange.getQueryString())) {
			exchange.setStatusCode(StatusCodes.BAD_REQUEST);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
			exchange.getResponseSender().send("Queries not allowed");
			exchange.endExchange();
		} else {
			final Variant variant=Attachments.getVariant(exchange);
			final File file = findIndexFile(variant);
			String representation;
			if(file.isFile()) {
				final List<String> readLines=IOUtils.readLines(new FileReader(file));
				representation=Joiner.on("\n").join(readLines);
			} else {
				representation=defaultLandingPage(variant);
			}
			if(this.reference) {
				exchange.getResponseHeaders().add(Headers.CONTENT_LOCATION,this.doc.implementationLandingPage().toString());
			}
			exchange.setStatusCode(StatusCodes.OK);
			exchange.getResponseSender().send(ByteBuffer.wrap(representation.getBytes(variant.charset().charset())));
		}
	}

	private String defaultLandingPage(final Variant variant) {
		return "<html><head><body>TODO: Generate HTML documentation for ontology "+this.doc.module().implementationIRI()+" in "+variant.language().locale().getDisplayLanguage()+"</body></head></html>";
	}

	private File findIndexFile(final Variant variant) {
		final String primaryTag = variant.language().primaryTag();
		File file=this.provider.assetsPath().resolve("index-"+primaryTag+".html").toFile();
		if(!file.isFile()) {
			file=this.provider.assetsPath().resolve("index.html").toFile();
		}
		return file;
	}
}