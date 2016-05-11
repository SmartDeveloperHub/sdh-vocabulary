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

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.ldp4j.http.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.vocabulary.publisher.handlers.Attachments;
import org.smartdeveloperhub.vocabulary.util.Module;
import org.smartdeveloperhub.vocabulary.util.Module.Format;
import org.smartdeveloperhub.vocabulary.util.SerializationManager;
import org.xnio.IoUtils;

import com.google.common.base.Throwables;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

final class SerializationHandler implements HttpHandler {

	private final class SerializationTransfer implements Runnable {

		private final Charset charset;
		private final HttpServerExchange exchange;

		private SerializationTransfer(final Charset charset, final HttpServerExchange exchange) {
			this.charset = charset;
			this.exchange = exchange;
		}

		@Override
		public void run() {
			try {
				final Path path = getModuleSerialization(this.charset);
				transfer(this.exchange, path);
			} catch (final IOException e) {
				fail(this.exchange,e,"Could not prepare %s",serialization(this.charset));
			}
		}

		private void transfer(final HttpServerExchange exchange, final Path path) {
			LOGGER.trace("Starting '{}' transfer...",path);
			try {
				final FileChannel channel=FileChannel.open(path,StandardOpenOption.READ);
				exchange.setStatusCode(StatusCodes.OK);
				exchange.getResponseSender().transferFrom(channel,new TransferCompletion(path,channel));
			} catch (final NoSuchFileException e) {
				LOGGER.error("Could not find '{}'",path);
				exchange.setStatusCode(StatusCodes.NOT_FOUND);
				exchange.getResponseHeaders().remove(Headers.CONTENT_TYPE);
			} catch (final IOException e) {
				fail(exchange,e,"'%s' transfer failed",path);
			}
		}

		private void fail(final HttpServerExchange exchange, final Throwable failure, final String format, final Object... args) {
			final String message = String.format(format, args)+". Full stacktrace follows:";
			LOGGER.error(message,failure);
			exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain;charset=UTF-8");
			exchange.getResponseSender().send(message+System.lineSeparator()+Throwables.getStackTraceAsString(failure),StandardCharsets.UTF_8);
		}
	}

	private final class TransferCompletion implements IoCallback {

		private final Path path;
		private final FileChannel channel;

		private TransferCompletion(final Path path, final FileChannel channel) {
			this.path = path;
			this.channel = channel;
		}

		private void terminate(final FileChannel channel, final HttpServerExchange exchange) {
			try {
				IoUtils.safeClose(channel);
			} finally {
				exchange.endExchange();
			}
		}

		@Override
		public void onException(final HttpServerExchange exchange, final Sender sender, final IOException exception) {
			LOGGER.error("'{}' transfer failed. Full stacktrace follows",this.path,exception);
			terminate(this.channel, exchange);
		}

		@Override
		public void onComplete(final HttpServerExchange exchange, final Sender sender) {
			LOGGER.trace("'{}' transfer completed succesfully!!!",this.path);
			terminate(this.channel, exchange);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(SerializationHandler.class);

	private final Format format;
	private final Module module;
	private final SerializationManager manager;

	SerializationHandler(final SerializationManager manager,final Module module,final Format format) {
		this.format=format;
		this.module=module;
		this.manager=manager;
	}

	@Override
	public void handleRequest(final HttpServerExchange exchange) throws Exception {
		final Variant variant = Attachments.getVariant(exchange);
		final Charset charset = variant.charset().charset();
		final Runnable task=new SerializationTransfer(charset, exchange);
		LOGGER.debug("Requested {}...",serialization(charset));
		if(exchange.isInIoThread()) {
			exchange.dispatch(task);
		} else {
			task.run();
		}
	}

	private Path getModuleSerialization(final Charset charset) throws IOException {
		return this.manager.getSerialization(this.module,this.format,charset);
	}

	private String serialization(final Charset charset) {
		return this.format.getName()+" ["+charset+"] serialization of module "+this.module.implementationIRI();
	}

}