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
package org.smartdeveloperhub.vocabulary.publisher.handlers;

import io.undertow.server.HttpServerExchange;

import java.net.URI;
import java.nio.file.Paths;

public final class HandlerUtil {

	private HandlerUtil() {
	}

	static String getExtension(final String module) {
		final String fileName = Paths.get(module).getFileName().toString();
		final int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
	}

	static URI canonicalURI(final HttpServerExchange exchange) {
		return getBase(exchange).resolve(exchange.getRelativePath().substring(1));
	}

	public static URI canonicalURI(final HttpServerExchange exchange, final String path) {
		return getBase(exchange).resolve(path);
	}

	private static URI getBase(final HttpServerExchange exchange) {
		URI base=Attachments.getBase(exchange);
		if(base==null) {
			base=physicalBase(exchange);
		}
		return base;
	}

	private static URI physicalBase(final HttpServerExchange exchange) {
		return URI.create(String.format("%s://%s%s/",exchange.getRequestScheme(),exchange.getHostAndPort(),exchange.getResolvedPath()));
	}

}
