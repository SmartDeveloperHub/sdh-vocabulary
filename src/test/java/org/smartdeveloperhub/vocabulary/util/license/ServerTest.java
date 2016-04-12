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
package org.smartdeveloperhub.vocabulary.util.license;

import static com.jayway.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import java.io.StringReader;
import java.net.InetAddress;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ServerTest {
	@Test
	public void testServer() throws Exception {
		final InetAddress localHost = InetAddress.getLocalHost();
		final Server sut = new Server(localHost.getHostAddress(), 12345);
		sut.start();
		try {
			final String location = sut.publish("http://creativecommons.org/licenses/by-nc-sa/3.0/");
			final String body=
			when().
				get(location).
			then().
				contentType("application/rdf+xml;charset=UTF-8").
				extract().
					asString();
			final List<Literal> licenses = getLicenses(location, body);
			assertThat(licenses,hasSize(1));
			assertThat(licenses.get(0).getLexicalForm(),equalTo("http://creativecommons.org/licenses/by-nc-sa/3.0/"));
		} finally {
			sut.stop();
		}
	}

	private List<Literal> getLicenses(final String location, final String body) {
		final List<Literal> licenses=Lists.newArrayList();
		final Model model = parse(body, location);
		final StmtIterator iterator=
			model.
				listStatements(
					model.getResource(location),
					model.createProperty("http://purl.org/dc/terms/license"),
					(RDFNode)null);
		try {
			while(iterator.hasNext()) {
				final Statement next = iterator.next();
				assertThat(next.getObject(),instanceOf(Literal.class));
				licenses.add(next.getObject().asLiteral());
			}
		} finally {
			iterator.close();
		}
		return licenses;
	}

	private Model parse(final String content, final String base) {
		final Model model =ModelFactory.createDefaultModel();
		final RDFReader reader = model.getReader("RDF/XML");
		reader.setProperty("error-mode", "strict-fatal");
		reader.read(model,new StringReader(content),base);
		return model;
	}

}
