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

import java.util.List;

import org.smartdeveloperhub.vocabulary.util.rdf.Constraint.Decission;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

final class GenericPropertyDefinition<T> implements PropertyDefinition<T> {

	private static final class DefaultAggregatorFactory implements AggregatorFactory<List<String>> {

		private static final class DefaultAggregator extends Aggregator<List<String>> {

			private final List<String> values;

			private DefaultAggregator(final Resource resource, final Property property){
				super(resource,property);
				this.values=Lists.newArrayList();
			}

			@Override
			public List<String> aggregation() {
				return this.values;
			}

			@Override
			protected Decission aggregateBlankNode(final String labelString) {
				this.values.add(labelString);
				return DecissionFactory.accept();
			}

			@Override
			protected Decission aggregateURIRef(final String uri) {
				this.values.add(uri);
				return DecissionFactory.accept();
			}

			@Override
			protected Decission aggregateStringLiteral(final String value) {
				this.values.add(value);
				return DecissionFactory.accept();
			}

			@Override
			protected Decission aggregateTypedLiteral(final String value, final String datatype) {
				this.values.add(value);
				return DecissionFactory.accept();
			}

			@Override
			protected Decission aggregateLanguageLiteral(final String value, final String language) {
				this.values.add(value);
				return DecissionFactory.accept();
			}
		}

		@Override
		public Aggregator<List<String>> newInstance(final Resource resource, final Property property) {
			return new DefaultAggregator(resource,property);
		}

	}

	interface TypeConstraint extends Constraint<RDFNode> {
		String type();
	}

	interface CardinalityConstraint extends Constraint<Integer> {
		int cardinality();
	}

	interface TypeChecker {

		boolean isValid(RDFNode value);
		String type();

	}

	static final class Builder<T> {

		private final String property;
		private final AggregatorFactory<T> factory;
		private TypeConstraint typeConstraint;
		private CardinalityConstraint minCardConstraint;
		private CardinalityConstraint maxCardConstraint;

		private Builder(final String property, final AggregatorFactory<T> factory) {
			this.property=property;
			this.factory=factory;
			this.typeConstraint=
				CustomTypeConstraint.
					newInstance(
						new AnyTypeChecker());
			cardinality(0,Integer.MAX_VALUE);
		}

		Builder<T> cardinality(final int minCard, final int maxCard) {
			Preconditions.checkArgument(minCard>=0,"Min cardinality cannot be lower than 0 (%s)",minCard);
			Preconditions.checkArgument(maxCard>=0,"Max cardinality cannot be lower than 0 (%s)",maxCard);
			Preconditions.checkArgument(minCard<=maxCard,"Max cardinality cannot be lower than min cardinality (%s<%s)",maxCard,minCard);
			this.minCardConstraint=new MinCardinalityConstraint(minCard);
			this.maxCardConstraint=new MaxCardinalityConstraint(maxCard);
			return this;
		}

		Builder<T> minCard(final int minCard) {
			return cardinality(minCard,this.maxCardConstraint.cardinality());
		}

		Builder<T> maxCard(final int maxCard) {
			return cardinality(this.minCardConstraint.cardinality(),maxCard);
		}

		Builder<T> required() {
			return cardinality(Math.max(this.minCardConstraint.cardinality(), 1),this.maxCardConstraint.cardinality());
		}

		Builder<T> optional() {
			return cardinality(0,this.maxCardConstraint.cardinality());
		}

		Builder<T> unbound() {
			return cardinality(this.minCardConstraint.cardinality(),Integer.MAX_VALUE);
		}

		Builder<T> resource() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new ResourceTypeChecker());
			return this;
		}

		Builder<T> blankNode() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new BlankNodeTypeChecker());
			return this;
		}

		Builder<T> uriRef() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new URIrefTypeChecker());
			return this;
		}

		Builder<T> literal() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new LiteralTypeChecker());
			return this;
		}

		Builder<T> typedLiteral(final String type) {
			this.typeConstraint=CustomTypeConstraint.newInstance(new TypedLiteralTypeChecker(type));
			return this;
		}

		<E> Builder<E> aggregator(final AggregatorFactory<E> visitor) {
			final Builder<E> result = new Builder<E>(this.property,visitor);
			result.minCardConstraint=this.minCardConstraint;
			result.maxCardConstraint=this.maxCardConstraint;
			result.typeConstraint=this.typeConstraint;
			return result;
		}

		Builder<T> languageLiteral() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new LanguageLiteralTypeChecker());
			return this;
		}

		GenericPropertyDefinition<T> build() {
			return new GenericPropertyDefinition<T>(this.property,this.typeConstraint,this.minCardConstraint,this.maxCardConstraint,this.factory);
		}

	}

	private static final class MaxCardinalityConstraint implements GenericPropertyDefinition.CardinalityConstraint {

		private final int cardinality;

		private MaxCardinalityConstraint(final int maxCard) {
			this.cardinality = maxCard;
		}

		@Override
		public Decission check(final Integer value) {
			return new Decission() {
				@Override
				public boolean accepted() {
					return value<=MaxCardinalityConstraint.this.cardinality;
				}
				@Override
				public String explanation() {
					return "Too many values: got "+value+" but expected "+MaxCardinalityConstraint.this.cardinality+" at most";
				}
			};
		}

		@Override
		public int cardinality() {
			return this.cardinality;
		}

	}

	private static final class MinCardinalityConstraint implements GenericPropertyDefinition.CardinalityConstraint {

		private final int cardinality;

		private MinCardinalityConstraint(final int minCard) {
			this.cardinality = minCard;
		}

		@Override
		public Decission check(final Integer value) {
			return new Decission() {
				@Override
				public boolean accepted() {
					return MinCardinalityConstraint.this.cardinality<=value;
				}
				@Override
				public String explanation() {
					return "Not enough values: got "+value+" but expected "+MinCardinalityConstraint.this.cardinality+" at least";
				}
			};
		}
		@Override
		public int cardinality() {
			return this.cardinality;
		}
	}

	private static final class CustomTypeConstraint implements TypeConstraint {

		private final static class TypeDecission implements Constraint.Decission {

			private final RDFNode value;
			private final TypeChecker checker;

			private TypeDecission(final RDFNode value, final GenericPropertyDefinition.TypeChecker checker) {
				this.value = value;
				this.checker = checker;
			}

			@Override
			public final boolean accepted() {
				return this.checker.isValid(this.value);
			}

			@Override
			public final String explanation() {
				return "Value '"+this.value+"' is not a "+this.checker.type();
			}

			static TypeDecission newInstance(final RDFNode value, final TypeChecker checker) {
				return new TypeDecission(value, checker);
			}

		}

		private final TypeChecker checker;

		private CustomTypeConstraint(final TypeChecker checker) {
			this.checker = checker;
		}

		@Override
		public Decission check(final RDFNode value) {
			return TypeDecission.newInstance(value,this.checker);
		}

		@Override
		public String type() {
			return this.checker.type();
		}

		static CustomTypeConstraint newInstance(final GenericPropertyDefinition.TypeChecker checker) {
			return new CustomTypeConstraint(checker);
		}

	}

	private static final class AnyTypeChecker implements GenericPropertyDefinition.TypeChecker {
		@Override
		public boolean isValid(final RDFNode value) {
			return true;
		}
		@Override
		public String type() {
			return "any";
		}
	}

	private static final class LanguageLiteralTypeChecker implements GenericPropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			if(!value.isLiteral()) {
				return false;
			}
			return !value.asLiteral().getLanguage().isEmpty();
		}

		@Override
		public String type() {
			return "language literal";
		}

	}

	private static final class TypedLiteralTypeChecker implements GenericPropertyDefinition.TypeChecker {

		private final String type;

		private TypedLiteralTypeChecker(final String type) {
			this.type = type;
		}

		@Override
		public boolean isValid(final RDFNode value) {
			if(!value.isLiteral()) {
				return false;
			}
			return this.type.equals(value.asLiteral().getDatatype());
		}

		@Override
		public String type() {
			return "literal of type '"+this.type+"'";
		}

	}

	private static final class LiteralTypeChecker implements GenericPropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			return value.isLiteral();
		}

		@Override
		public String type() {
			return "literal";
		}

	}

	private static final class URIrefTypeChecker implements GenericPropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			return value.isURIResource();
		}

		@Override
		public String type() {
			return "URIref";
		}

	}

	private static final class BlankNodeTypeChecker implements GenericPropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			return value.isAnon();
		}

		@Override
		public String type() {
			return "blank node";
		}

	}

	private static final class ResourceTypeChecker implements GenericPropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			return value.isResource();
		}

		@Override
		public String type() {
			return "resource";
		}

	}

	private final String property;
	private final AggregatorFactory<T> factory;
	private final TypeConstraint typeConstraint;
	private final CardinalityConstraint minCardConstraint;
	private final CardinalityConstraint maxCardConstraint;

	private GenericPropertyDefinition(final String property, final TypeConstraint typeConstraint, final CardinalityConstraint minCard, final CardinalityConstraint maxCard, final AggregatorFactory<T> factory) {
		this.property = property;
		this.typeConstraint = typeConstraint;
		this.minCardConstraint = minCard;
		this.maxCardConstraint = maxCard;
		this.factory = factory;
	}

	@Override
	public String property() {
		return this.property;
	}

	@Override
	public int minCard() {
		return this.minCardConstraint.cardinality();
	}

	@Override
	public int maxCard() {
		return this.maxCardConstraint.cardinality();
	}

	@Override
	public String type() {
		return this.typeConstraint.type();
	}

	@Override
	public Evaluation<T> evaluate(final Resource resource) {
		final Aggregator<T> aggregator=
			this.factory.newInstance(
					resource,
					ResourceFactory.createProperty(this.property));
		final List<RDFNode> allValues=Lists.newArrayList();
		final List<String> failures=Lists.newArrayList();
		final StmtIterator statements = resource.listProperties(aggregator.property());
		try {
			while(statements.hasNext()) {
				final Statement statement = statements.next();
				final RDFNode object = statement.getObject();
				final Decission check = this.typeConstraint.check(object);
				allValues.add(object);
				if(check.accepted()) {
					final Decission valueCheck = aggregator.check(object);
					if(!valueCheck.accepted()) {
						failures.add(valueCheck.explanation());
					}
				} else {
					failures.add(check.explanation());
				}
			}
		} finally {
			statements.close();
		}
		final Decission minCardCheck = this.minCardConstraint.check(allValues.size());
		if(!minCardCheck.accepted()) {
			failures.add(minCardCheck.explanation());
		}
		final Decission maxCardCheck = this.maxCardConstraint.check(allValues.size());
		if(!maxCardCheck.accepted()) {
			failures.add(maxCardCheck.explanation());
		}
		return Evaluation.create(NodeUtils.toString(resource),this.property,aggregator.aggregation(),failures);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("property",property()).
					add("type",type()).
					add("minCard",toString(minCard())).
					add("maxCard",toString(maxCard())).
					toString();
	}

	static Builder<List<String>> builder(final String property) {
		return new Builder<>(property,new DefaultAggregatorFactory());
	}

	private static String toString(final int value) {
		return
			value==Integer.MAX_VALUE?
				"unbound":
				Integer.toString(value);
	}

}