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
package org.smartdeveloperhub.vocabulary.util.rdf;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class Aggregator<T> implements Constraint<RDFNode> {

	private final class Visitor implements RDFVisitor {

		@Override
		public final Decission visitBlank(final Resource resource, final AnonId id) {
			return aggregateBlankNode(resource,id.getLabelString());
		}

		@Override
		public final Decission visitURI(final Resource resource, final String uri) {
			return aggregateURIRef(resource,uri);
		}

		@Override
		public final Object visitLiteral(final Literal literal) {
			final String language = literal.getLanguage();
			final String datatype = literal.getDatatypeURI();
			if(!language.isEmpty()) {
				return aggregateLanguageLiteral(literal.getString(),language);
			} else if(datatype!=null) {
				return aggregateTypedLiteral(literal.getLexicalForm(),datatype);
			} else {
				return aggregateStringLiteral(literal.getString());
			}
		}
	}

	private final Resource resource;
	private final Property property;

	public Aggregator(final Resource resource, final Property property) {
		this.resource = resource;
		this.property = property;
	}

	@Override
	public final Decission check(final RDFNode value) {
		return (Decission)value.visitWith(new Visitor());
	}

	public abstract T aggregation();

	protected final Resource resource() {
		return this.resource;
	}

	protected final Property property() {
		return this.property;
	}

	protected Decission aggregateBlankNode(final Resource resource, final String labelString) {
		return DecissionFactory.accept();
	}

	protected Decission aggregateURIRef(final Resource resource, final String uri) {
		return DecissionFactory.accept();
	}

	protected Decission aggregateStringLiteral(final String value) {
		return DecissionFactory.accept();
	}

	protected Decission aggregateTypedLiteral(final String value, final String datatype) {
		return DecissionFactory.accept();
	}

	protected Decission aggregateLanguageLiteral(final String value, final String language) {
		return DecissionFactory.accept();
	}

}