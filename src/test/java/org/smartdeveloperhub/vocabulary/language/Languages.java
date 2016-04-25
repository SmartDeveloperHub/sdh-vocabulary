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
package org.smartdeveloperhub.vocabulary.language;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public final class Languages {

	private static final class ProxySource implements Source {

		private final String name;
		private final String version;
		private final String sha256;
		private final String sha512;
		private final DateTime generated;

		private ProxySource(final String name, final String version, final String sha256, final String sha512, final DateTime generated) {
			this.name = name;
			this.version = version;
			this.sha256 = sha256;
			this.sha512 = sha512;
			this.generated = generated;
		}

		@Override
		public String name() {
			return this.name;
		}

		@Override
		public String version() {
			return this.version;
		}

		@Override
		public String sha256() {
			return this.sha256;
		}

		@Override
		public String sha512() {
			return this.sha512;
		}

		@Override
		public DateTime generatedOn() {
			return this.generated;
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(Languages.class);

	private static final Languages INSTANCE;

	static {
		LanguagePack pack=loadPreferredPack();
		if(pack==null) {
			pack=loadDefaultPack();
		}
		INSTANCE=new Languages(pack);
	}

	private final LanguagePack pack;
	private final ProxySource source;

	private Languages(final LanguagePack pack) {
		this.pack=pack;
		this.source=
			new ProxySource(
				pack.name(),
				pack.version(),
				pack.sha256(),
				pack.sha512(),
				pack.generated());
	}

	public Source source() {
		return this.source;
	}

	public Set<String> uri(final String tag) {
		return this.pack.tagUris(tag);
	}

	public String localizedName(final String tag, final Locale locale) {
		final Optional<Integer> language = this.pack.tagLanguage(tag);
		if(!language.isPresent()) {
			return null;
		}
		return this.pack.languageName(language.get(),locale.getLanguage());
	}

	public static Languages getInstance() {
		return INSTANCE;
	}

	private static LanguagePack loadDefaultPack() throws AssertionError {
		final URL resource = Languages.class.getResource("lexvo.pack");
		if(resource==null) {
			final String errorMessage = "Could not find default language pack (classpath:"+Utils.resourceURI(Languages.class,"lexvo.pack")+")";
			LOGGER.error(errorMessage);
			throw new AssertionError(errorMessage);
		}
		try {
			final LanguagePack pack=LanguagePacker.load(resource);
			LOGGER.info("Using default language pack ({})",resource);
			return pack;
		} catch (final IOException e) {
			final String errorMessage = "Could not load default language pack ("+resource+")";
			LOGGER.error(errorMessage);
			throw new AssertionError(errorMessage,e);
		}
	}

	private static LanguagePack loadPreferredPack() {
		final String property = System.getProperty("languages.pack");
		if(property==null) {
			return null;
		}
		LanguagePack pack=null;
		final Path location=Paths.get(property);
		if(!location.toFile().isFile()) {
			LOGGER.warn("Preferred language pack {} is not a file",location);
		} else {
			try {
				pack=LanguagePacker.load(location);
				LOGGER.info("Using preferred language pack {}",location);
			} catch (final IOException e) {
				LOGGER.warn("Could not load preferred language pack {}. Full stacktrace follows",location,e);
			}
		}
		return pack;
	}

}
