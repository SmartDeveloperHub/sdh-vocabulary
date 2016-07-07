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
package org.smartdeveloperhub.vocabulary.language.lexvo;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.vocabulary.language.lexvo.Chronographer.Task;
import org.smartdeveloperhub.vocabulary.language.spi.Language;
import org.smartdeveloperhub.vocabulary.language.spi.LanguageDataSource;
import org.smartdeveloperhub.vocabulary.language.spi.Tag;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public final class LexvoDataSource implements LanguageDataSource {

	private abstract static class TypeInspector implements Task<Void,RuntimeException> {

		private final Model model;
		private final String propertyName;

		private TypeInspector(final Model model,final String propertyName) {
			this.model = model;
			this.propertyName = propertyName;
		}

		@Override
		public Void execute() throws RuntimeException {
			final StmtIterator iterator=
				this.model.
					listStatements(
						null,
						this.model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
						this.model.createProperty(this.propertyName));
			try {
				while(iterator.hasNext()) {
					handler(iterator.next());
				}
				return null;
			} finally {
				iterator.close();
			}
		}

		protected abstract void handler(Statement next);

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(LexvoDataSource.class);

	private final Map<Integer,Language> languages;
	private final Map<Tag,Integer> tag2Language;

	private String lexvoVersion;
	private String lexvoSha256;
	private String lexvoSha512;

	public LexvoDataSource() {
		this.languages=Maps.newLinkedHashMap();
		this.tag2Language=Maps.newLinkedHashMap();
	}

	@Override
	public String name() {
		return "Lexvo";
	}

	@Override
	public String version() {
		return this.lexvoVersion;
	}

	@Override
	public String sha512() {
		return this.lexvoSha512;
	}

	@Override
	public String sha256() {
		return this.lexvoSha256;
	}

	@Override
	public List<Language> languages() {
		return ImmutableList.copyOf(this.languages.values());
	}

	public LexvoDataSource load(final String version, final Path input) throws IOException {
		clean();
		findLanguages(loadModel(version,input));
		return this;
	}

	private void clean() {
		this.lexvoVersion=null;
		this.lexvoSha256=null;
		this.lexvoSha512=null;
	}

	private Model loadModel(final String version,final Path input) throws IOException {
		final byte[] rawData=readInput(input);
		final String data=stringifyRawData(rawData);
		final Model model=parseRDF(data);
		this.lexvoVersion=version;
		this.lexvoSha256=Hashing.sha256().hashBytes(rawData).toString();
		this.lexvoSha512=Hashing.sha512().hashBytes(rawData).toString();
		return model;
	}

	private void findLanguages(final Model model) {
		new Chronographer("searching for languages").
			time(
				new TypeInspector(model,"lvont:Language") {
					private int counter=0;
					@Override
					protected void handler(final Statement next) {
						final Resource subject = next.getSubject();
						if(subject.isAnon()) {
							LOGGER.warn("Language cannot be an anonymous individual");
							return;
						}
						final Set<Tag> tags=Sets.newTreeSet();
						getTag(tags,subject,"http://lexvo.org/ontology#iso639P1Code",Tag.Part.ISO_639_1);
						getTag(tags,subject,"http://lexvo.org/ontology#iso6392BCode",Tag.Part.ISO_639_2b);
						getTag(tags,subject,"http://lexvo.org/ontology#iso6392TCode",Tag.Part.ISO_639_2t);
						getTag(tags,subject,"http://lexvo.org/ontology#iso639P3PCode",Tag.Part.ISO_639_3);
						getTag(tags,subject,"http://lexvo.org/ontology#iso639P5Code",Tag.Part.ISO_639_5);
						if(tags.isEmpty()) {
							LOGGER.warn("Language {} has no tags",subject.getURI());
							return;
						}
						final Map<String, String> localizedNames = getLocalizedNames(subject);
						if(localizedNames.isEmpty()) {
							LOGGER.info("Language {} has no localized names",subject.getURI());
						}
						final Language language=
							Language.
								builder().
									withId(this.counter++).
									withTags(tags).
									withLocalizedNames(localizedNames).
									build();
						for(final Tag tag:tags) {
							LexvoDataSource.this.tag2Language.put(tag,language.id());
						}
						LexvoDataSource.this.languages.put(language.id(),language);
					}

					private void getTag(final Set<Tag> tags,final Resource resource,final String propertyName, final Tag.Part part) {
						final Property tagProperty = resource.getModel().getProperty(propertyName);
						final Statement property = resource.getProperty(tagProperty);
						if(property!=null) {
							final RDFNode object = property.getObject();
							if(object.isLiteral()) {
								final LexvoTag tag = LexvoTag.create(part,object.asLiteral().getString());
								tags.add(tag);
							}
						}
					}
					private Map<String,String> getLocalizedNames(final Resource resource) {
						final Property label = resource.getModel().getProperty("http://www.w3.org/2000/01/rdf-schema#label");
						final StmtIterator iterator = resource.listProperties(label);
						try {
							final Map<String,String> localizations=Maps.newTreeMap();
							while(iterator.hasNext()) {
								final Statement next=iterator.next();
								final RDFNode object=next.getObject();
								if(!object.isLiteral()) {
									continue;
								}
								final Literal literal=object.asLiteral();
								final String language=literal.getLanguage();
								if(language!=null) {
									localizations.put(language,literal.getString());
								}
							}
							return localizations;
						} finally {
							iterator.close();
						}
					}
				}
		);
	}

	private static String stringifyRawData(final byte[] content) {
		return
			new Chronographer("loading contents").
				time(
					new Task<String,RuntimeException>() {
						@Override
						public String execute() {
							return new String(content,StandardCharsets.UTF_8);
						}
					}
				);
	}

	private static byte[] readInput(final Path path) throws IOException {
		return
			new Chronographer("file reading").
				onStart("path %s", path).
				time(
					new Task<byte[],IOException>() {
						@Override
						public byte[] execute() throws IOException {
							return Files.readAllBytes(path);
						}
					}
				);
	}

	private static Model parseRDF(final String data) {
		return
			new Chronographer("parsing contents").
				timeIn(TimeUnit.SECONDS).
				time(
					new Task<Model,RiotException>() {
						@Override
						public Model execute() {
							final Model model=ModelFactory.createDefaultModel();
							model.read(new StringReader(data),"http://www.lexvo.org/","RDF/XML");
							return model;
						}
					}
				);
	}

}
