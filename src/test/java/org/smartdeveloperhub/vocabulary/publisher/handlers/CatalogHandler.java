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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.net.URI;

import org.smartdeveloperhub.vocabulary.util.Catalog;
import org.smartdeveloperhub.vocabulary.util.Module;

final class CatalogHandler implements HttpHandler {

	private final Catalog catalog;
	private final HttpHandler next;

	CatalogHandler(final Catalog catalog, final HttpHandler next) {
		this.catalog = catalog;
		this.next = next;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		String moduleName=exchange.getRelativePath().substring(1);
		final String ext = HandlerUtil.getExtension(moduleName);
		moduleName=moduleName.substring(0,moduleName.length()-ext.length()-(ext.isEmpty()?0:1));
		final Module module=this.catalog.resolve(URI.create(moduleName));
		if(module==null) {
			System.out.printf("Accessing %s --> NOT FOUND%n",exchange.getRelativePath());
			exchange.setStatusCode(StatusCodes.NOT_FOUND);
		} else {
			System.out.printf("Accessing %s --> %s [%s]%n",exchange.getRelativePath(),resolve(moduleName),resolve(module.relativePath()));
			Attachments.setModule(exchange, module);
			Attachments.setBase(exchange, this.catalog.getBase());
			this.next.handleRequest(exchange);
		}
	}

	private URI resolve(final String path) {
		return this.catalog.getBase().resolve(path);
	}

}