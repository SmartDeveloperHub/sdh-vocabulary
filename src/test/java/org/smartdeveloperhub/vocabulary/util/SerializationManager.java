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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.vocabulary.util.Module.Format;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public final class SerializationManager {

	private static final class SerializationHelper {

		private final ModuleHelper helper;
		private final Path where;
		private final String fileName;
		private final Module module;

		SerializationHelper(final Module module, final Path where) throws IOException {
			this.module = module;
			this.where = where;
			this.helper = getHelper(module);
			this.fileName = MorePaths.getFileName(module.location());
		}

		Path serialize(final Format format) throws IOException {
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

	private static final class SerializationKey {

		private final String module;
		private final String format;
		private final String charset;

		private SerializationKey(final String module, final String format, final String charset) {
			this.module = module;
			this.format = format;
			this.charset = charset;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.module,this.format,this.charset);
		}

		@Override
		public boolean equals(final Object obj) {
			boolean result = false;
			if(obj instanceof SerializationKey) {
				final SerializationKey that=(SerializationKey)obj;
				result=
					Objects.equals(this.module,that.module) &&
					Objects.equals(this.format,that.format) &&
					Objects.equals(this.charset,that.charset);
			}
			return result;
		}

		@Override
		public String toString() {
			return this.format+" ["+this.charset+"] serialization for module "+this.module;
		}

		static SerializationKey create(final Module module, final Format format, final Charset charset) {
			return new SerializationKey(module.implementationIRI(),format.getName(),charset.toString());
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(SerializationManager.class);

	private final Path location;
	private final Map<Path,Map<Format,Path>> serializations;

	private final Map<SerializationKey,Lock> serialLocks;
	private final Map<SerializationKey,Path> serialFiles;

	private SerializationManager(final Path where, final Map<Path,Map<Format,Path>> serializations) {
		this.location=where;
		this.serializations=serializations;
		this.serialFiles=Maps.newLinkedHashMap();
		this.serialLocks=Maps.newLinkedHashMap();
	}

	public Path location() {
		return this.location;
	}

	public Path getSerialization(final Module module, final Format format) {
		Map<Format,Path> moduleSerializations=this.serializations.get(module.location());
		if(moduleSerializations==null) {
			moduleSerializations=Collections.emptyMap();
		}
		return moduleSerializations.get(format);
	}

	public Path getSerialization(final Module module, final Format format, final Charset targetCharset) throws IOException {
		final SerializationKey key=SerializationKey.create(module, format, targetCharset);
		Lock lock=null;
		synchronized(this) {
			final Path path=this.serialFiles.get(key);
			if(path!=null) {
				LOGGER.debug("Found {}: {}",key,path);
				return path;
			}
			lock=this.serialLocks.get(key);
			if(lock==null) {
				lock=new ReentrantLock();
				this.serialLocks.put(key,lock);
				LOGGER.debug("Scheduled creation of {}",key);
			}
		}
		lock.lock();
		try {
			final Path target=generateSerialization(module, format, targetCharset);
			if(target!=null) {
				synchronized(this) {
					LOGGER.debug("Created {}: {}",key,target);
					this.serialFiles.put(key,target);
				}
			}
			return target;
		} finally {
			lock.unlock();
		}
	}

	private Path generateSerialization(final Module module, final Format format, final Charset targetCharset) throws IOException {
		final Path source=getSerialization(module,format);
		Path target=null;
		if(source!=null) {
			if(StandardCharsets.UTF_8.equals(targetCharset)) {
				target=source;
			} else {
				target=
					source.
						getParent().
							resolve(
								MorePaths.getFileName(source)+
								"."+targetCharset.name()+
								"."+MorePaths.getFileExtension(source));
				IOUtil.transcode(source,StandardCharsets.UTF_8,target,targetCharset);
				target.toFile().deleteOnExit();
			}
		}
		return target;
	}

	private static Map<Format,Path> generate(final Module module, final Path where) throws IOException {
		createDirectory(where);
		final SerializationHelper helper=new SerializationHelper(module,where);
		final ImmutableMap.Builder<Format,Path> builder=ImmutableMap.builder();
		for(final Format format:Format.values()) {
			builder.put(format,helper.serialize(format));
		}
		return builder.build();
	}

	private static Map<Path,Map<Format,Path>> generate(final Catalog catalog, final Path where) throws IOException {
		createDirectory(where);
		final ImmutableMap.Builder<Path,Map<Format,Path>> builder=ImmutableMap.builder();
		for(final String moduleId:catalog.modules()) {
			final Module module=catalog.get(moduleId);
			final Path moduleParent = module.location().getParent();
			final Path relativeModuleParent=catalog.getRoot().relativize(moduleParent);
			final Path target=where.resolve(relativeModuleParent);
			try {
				builder.put(module.location(),generate(module,target));
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

	public static SerializationManager create(final Module module, final Path location) throws IOException {
		final Map<Path,Map<Format,Path>> serials=
			ImmutableMap.
				<Path,Map<Format,Path>>builder().
					put(module.location(),generate(module,location)).
					build();
		return new SerializationManager(location,serials);
	}

	public static SerializationManager create(final Catalog catalog, final Path location) throws IOException {
		return new SerializationManager(location,generate(catalog,location));
	}

}
