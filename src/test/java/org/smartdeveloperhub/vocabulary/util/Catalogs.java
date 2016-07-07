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
package org.smartdeveloperhub.vocabulary.util;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public final class Catalogs {

	private static class CatalogBuilder extends SimpleFileVisitor<Path> {

		private static final String NL = System.lineSeparator();

		private final Catalog catalog;
		private final Result<Catalog> catalogResult;

		private CatalogBuilder(final URI base, final Path root) {
			this.catalog=new Catalog(Context.create(base,root));
			this.catalogResult=Result.newInstance();
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attr) {
			if(attr.isRegularFile()) {
				final Result<Module> moduleResult = this.catalog.loadModule(file);
				final List<String> errors = moduleResult.errors();
				if(!errors.isEmpty()) {
					this.catalogResult.error(message(file,errors));
				}
				final List<String> warnings = moduleResult.warnings();
				if(!warnings.isEmpty()) {
					this.catalogResult.warn(message(file,warnings));
				}
			}
			return CONTINUE;
		}

		private String message(final Path file, final List<String> messages) {
			final StringBuilder builder=new StringBuilder();
			builder.append("File '%s':");
			for(final String message:messages) {
				builder.append(NL).append("  + ").append(message);
			}
			return String.format(builder.toString(),file);
		}

		@Override
		public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
			return CONTINUE;
		}

		Result<Catalog> builtCatalog() {
			if(this.catalogResult.errors().isEmpty()) {
				this.catalogResult.value(this.catalog);
			}
			return this.catalogResult;
		}
	}

	private Catalogs() {
	}

	public static Result<Catalog> loadFrom(final Path root, final URI base) throws IOException {
		final CatalogBuilder finder = new CatalogBuilder(base,root);
		Files.walkFileTree(root, finder);
		return finder.builtCatalog();
	}

}
