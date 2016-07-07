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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.jena.riot.RiotException;
import org.smartdeveloperhub.vocabulary.util.Module.Format;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public final class ModuleHelper {

	private final Path file;
	private Model model;
	private Format format;

	public ModuleHelper(final Path file) {
		this.file = file;
	}

	Format format() {
		return this.format;
	}

	boolean isAvailable() {
		return this.model!=null;
	}

	public ModuleHelper load(final URI base,final Format format) throws IOException {
		final String data=
			new String(
				Files.readAllBytes(this.file),
				StandardCharsets.UTF_8);
		if(!format.equals(Format.RDF_XML) || RDFXMLUtil.isStandaloneDocument(data)) {
			try {
				this.model =ModelFactory.createDefaultModel();
				this.format=format;
				final RDFReader reader = this.model.getReader(format.lang.getLabel());
				reader.setProperty("error-mode", "strict-fatal");
				reader.read(this.model,new StringReader(data),base.toString());
			} catch (final RiotException e) {
				this.model=null;
				this.format=format;
				throw new IOException("Parsing failed",e);
			}
		}
		return this;
	}

	String export(final Format format) throws IOException {
		Preconditions.checkState(this.model!=null);
		final StringWriter writer = new StringWriter();
		this.model.write(writer,format.lang.getLabel());
		return writer.toString();
	}

	public Model export() {
		Preconditions.checkState(this.model!=null);
		return this.model;
	}

	List<Resource> getResourcesOfType(final String type) {
		Preconditions.checkState(this.model!=null);
		final ResIterator iterator =
			this.model.
				listResourcesWithProperty(
					this.model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
					NodeFactory.createURI(type));
		try {
			return iterator.toList();
		} finally {
			iterator.close();
		}
	}

	<T> List<T> getPropertyValues(final Resource resource, final String property, final Function<Statement,Optional<T>> filter) {
		Preconditions.checkState(this.model!=null);
		final StmtIterator stmts = resource.listProperties(this.model.createProperty(property));
		try {
			final List<T> values=Lists.newArrayList();
			while(stmts.hasNext()) {
				final Statement st=stmts.nextStatement();
				final Optional<T> result = filter.apply(st);
				if(result.isPresent()) {
					values.add(result.get());
				}
			}
			return values;
		} finally {
			stmts.close();
		}
	}

}