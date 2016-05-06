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
package org.smartdeveloperhub.vocabulary.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.smartdeveloperhub.vocabulary.util.Module.Format;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public final class Serializations {

	private Serializations() {
	}

	private static final class SerializationHelper {

		private final ModuleHelper helper;
		private final Path where;
		private final String fileName;
		private final Module module;

		private SerializationHelper(final Module module, final Path where) throws IOException {
			this.module = module;
			this.where = where;
			this.helper = getHelper(module);
			this.fileName = MorePaths.getFileName(module.location());
		}

		public Path serialize(final Format format) throws IOException {
			if(format.equals(this.module.format())) {
				return copyCanonicalSerialization();
			} else {
				return transformCanonicalSerialization(format);
			}
		}

		private Path transformCanonicalSerialization(final Format format) throws IOException {
			final Path target=this.where.resolve(this.fileName+"."+format.fileExtension());
			try {
				final String transformed=this.helper.export(format);
				Files.
					copy(
						new ByteArrayInputStream(transformed.getBytes(StandardCharsets.UTF_8)),
						target,
						StandardCopyOption.REPLACE_EXISTING);
				return target;
			} catch (final IOException e) {
				throw new IOException("Could not copy "+format.getName()+" serialization of module "+this.module.location()+" ["+this.module.format().getName()+"] to "+target, e);
			}
		}

		private Path copyCanonicalSerialization() throws IOException {
			final Path target=this.where.resolve(this.fileName+"."+MorePaths.getFileExtension(this.module.location()));
			try {
				Files.
					copy(
						this.module.location(),
						target,
						StandardCopyOption.REPLACE_EXISTING);
				return target;
			} catch (final IOException e) {
				throw new IOException("Could not copy canonical serialization of module "+this.module.location()+" ["+this.module.format().getName()+"] to "+target);
			}
		}

		private static ModuleHelper getHelper(final Module module) throws IOException {
			ModuleHelper helper;
			try {
				helper =
					new ModuleHelper(module.location()).
						load(module.locationNamespace(),module.format());
			} catch (final IOException e) {
				throw new IOException("Could not load module "+module.location()+" ["+module.format().getName()+"]",e);
			}
			return helper;
		}

	}

	public static List<Path> generate(final Module module, final Path where) throws IOException {
		final SerializationHelper helper=new SerializationHelper(module,where);
		final Builder<Path> builder=ImmutableList.builder();
		for(final Format format:Format.values()) {
			builder.add(helper.serialize(format));
		}
		return builder.build();
	}

	public static List<Path> generate(final Catalog catalog, final Path where) throws IOException {
		createDirectory(where);
		final Builder<Path> builder=ImmutableList.builder();
		for(final String moduleId:catalog.modules()) {
			final Module module=catalog.get(moduleId);
			final Path moduleParent = module.location().getParent();
			final Path relativeModuleParent=catalog.getRoot().relativize(moduleParent);
			final Path target = where.resolve(relativeModuleParent);
			try {
				createDirectory(target);
				builder.addAll(generate(module,target));
			} catch(final IOException e) {
				throw new IOException("Could not generate serializations for module "+module.location()+" ["+module.format().getName()+"]",e);
			}
		}
		return builder.build();
	}

	private static void createDirectory(final Path where) throws IOException {
		final File file = where.toFile();
		if(!file.exists()) {
			if(!file.mkdirs()) {
				throw new IOException("Could not create target directory '"+where+"'");
			}
		} else if(!file.isDirectory()) {
			throw new IOException("Target path '"+where+"' points to an existing file, not a directory");
		}
	}

}
