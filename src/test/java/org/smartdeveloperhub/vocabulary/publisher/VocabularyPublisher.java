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

import static io.undertow.Handlers.path;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.allow;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.catalogResolver;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.contentNegotiation;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.smartdeveloperhub.vocabulary.publisher.handlers.Attachments;
import org.smartdeveloperhub.vocabulary.publisher.handlers.HandlerUtil;
import org.smartdeveloperhub.vocabulary.publisher.handlers.Variant;
import org.smartdeveloperhub.vocabulary.util.Catalog;
import org.smartdeveloperhub.vocabulary.util.Catalogs;
import org.smartdeveloperhub.vocabulary.util.Module;
import org.smartdeveloperhub.vocabulary.util.Result;

import com.google.common.base.Strings;

public class VocabularyPublisher {

	public static void main(final String... args) {
		if(args.length!=2) {
			System.err.printf("Invalid argument number: required 2 arguments (%d)%n", args.length);
			System.exit(-1);
		}
		try {
			final Path root = Paths.get(args[0]);
			final URI base = new URI(args[1]);
			final Result<Catalog> result = Catalogs.loadFrom(root, base);
			if(result.isAvailable()) {
				final Catalog catalog = result.get();
				System.out.printf("Found %d modules%n",catalog.size());
				for(final String moduleId:catalog.modules()) {
					final Module module = catalog.resolve(URI.create(moduleId));
					System.out.printf("- %s%n", module);
				}
				publish(catalog);
				System.out.println("Publisher terminated.");
			} else {
				System.err.println("Could not prepare catalog:\n"+result);
				System.exit(-5);
			}
		} catch (final InvalidPathException e) {
			System.err.printf("%s is not a valid root path (%s)%n", args[0],e.getMessage());
			System.exit(-2);
		} catch (final URISyntaxException e) {
			System.err.printf("%s is not a valid base URI (%s)%n", args[1],e.getMessage());
			System.exit(-3);
		} catch (final IOException e) {
			System.err.printf("Could not explore modules (%s)%n", e.getMessage());
			System.exit(-4);
		}

	}

	private static void publish(final Catalog catalog) {
		final Undertow server =
			Undertow.
				builder().
					addHttpListener(8080, "localhost").
					setHandler(
							path().
								addPrefixPath(
									"/vocabulary/",
									catalogResolver(catalog,
										allow(Methods.GET,
											contentNegotiation(new SimpleVariantProducer(),
												new HttpHandler() {
													@Override
													public void handleRequest(final HttpServerExchange exchange) throws Exception {
														if(!Strings.isNullOrEmpty(exchange.getQueryString())) {
															exchange.setStatusCode(StatusCodes.BAD_REQUEST);
															exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain; charset=\"UTF-8\"");
															exchange.getResponseSender().send("Queries not allowed");
														} else {
															final Variant variant=Attachments.getVariant(exchange);
															final Module module=Attachments.getModule(exchange);
															exchange.setStatusCode(StatusCodes.OK);
															exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,variant.format().contentType(StandardCharsets.UTF_8));
															exchange.getResponseSender().send(module.transform(HandlerUtil.canonicalURI(exchange,module.relativePath()),variant.format()));
														}
													}
												}
											)
										)
									)
								)
							).
					build();
		server.start();
		awaitTerminationRequest();
		server.stop();
	}


	private static void awaitTerminationRequest() {
		System.out.println("Hit <ENTER> to exit...");
		try(final Scanner scanner = new Scanner(System.in)) {
			String readString = scanner.nextLine();
			while(readString != null) {
				if (readString.isEmpty()) {
					break;
				}
				if (scanner.hasNextLine()) {
					readString = scanner.nextLine();
				} else {
					readString = null;
				}
			}
		}
	}

}
