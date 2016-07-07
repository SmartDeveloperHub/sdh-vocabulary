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
package org.smartdeveloperhub.vocabulary.language;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.smartdeveloperhub.vocabulary.language.spi.LanguageDataSource;
import org.smartdeveloperhub.vocabulary.language.util.LocalizedProperties;

public final class LanguagePacker {

	private LanguagePacker() {
	}


	public static void store(final Path output, final LanguageDataSource source) throws IOException {
		final FileOutputStream stream=new FileOutputStream(output.toFile());
		try {
			final LanguagePack pack = LanguagePack.fromSource(source);
			pack.export().store(stream,StandardCharsets.UTF_8);
		} finally {
			stream.close();
		}
	}

	static LanguagePack load(final URL url) throws IOException {
		return load(url.openStream());
	}

	static LanguagePack load(final Path path) throws IOException {
		return load(new FileInputStream(path.toFile()));
	}

	private static LanguagePack load(final InputStream stream) throws IOException {
		try {
			final LocalizedProperties export=new LocalizedProperties();
			export.load(stream,StandardCharsets.UTF_8);
			return LanguagePack.fromExport(export);
		} finally {
			stream.close();
		}
	}

}