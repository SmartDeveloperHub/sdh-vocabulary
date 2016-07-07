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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.3.0
 *   Bundle      : sdh-vocabulary-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.publisher.handlers;

import java.util.Set;

import com.google.common.collect.Sets;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

final class AllowedMethodsHandler implements HttpHandler {

	private final Set<String> methods;
	private final HttpHandler next;

	private AllowedMethodsHandler(final HttpHandler aHandler) {
		this.next=aHandler;
		this.methods=Sets.newLinkedHashSet();
	}

	AllowedMethodsHandler allow(final HttpString method) {
		if(method!=null) {
			this.methods.add(method.toString());
			if(method.equals(Methods.GET)) {
				this.methods.add(Methods.HEAD_STRING);
			}
		}
		return this;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		if(this.methods.contains(exchange.getRequestMethod().toString())) {
			if(this.next!=null) {
				this.next.handleRequest(exchange);
			}
		} else {
			exchange.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			// TODO: I18N
			exchange.getResponseSender().send(String.format("Unsupported method (%s)",exchange.getRequestMethod()));
			exchange.getResponseHeaders().putAll(Headers.ALLOW,this.methods);
			exchange.endExchange();
		}
	}

	static AllowedMethodsHandler create(final HttpHandler aHandler) {
		return new AllowedMethodsHandler(aHandler);
	}

}