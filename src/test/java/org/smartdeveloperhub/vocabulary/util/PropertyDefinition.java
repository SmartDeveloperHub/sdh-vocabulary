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

import java.util.List;

import org.smartdeveloperhub.vocabulary.util.PropertyDefinition.Constraint.Decission;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

final class PropertyDefinition {

	interface Constraint<T> {
		interface Decission {
			boolean accepted();
			String explanation();
		}
		Decission check(T value);
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

	static final class Builder {

		private final String property;
		private TypeConstraint typeConstraint;
		private CardinalityConstraint minCardConstraint;
		private CardinalityConstraint maxCardConstraint;

		private Builder(final String property) {
			this.property=property;
			this.typeConstraint=
				CustomTypeConstraint.
					newInstance(
						new AnyTypeChecker());
			cardinality(0,Integer.MAX_VALUE);
		}

		Builder cardinality(final int minCard, final int maxCard) {
			Preconditions.checkArgument(minCard>=0,"Min cardinality cannot be lower than 0 (%s)",minCard);
			Preconditions.checkArgument(maxCard>=0,"Max cardinality cannot be lower than 0 (%s)",maxCard);
			Preconditions.checkArgument(minCard<=maxCard,"Max cardinality cannot be lower than min cardinality (%s<%s)",maxCard,minCard);
			this.minCardConstraint=new MinCardinalityConstraint(minCard);
			this.maxCardConstraint=new MaxCardinalityConstraint(maxCard);
			return this;
		}

		Builder minCard(final int minCard) {
			return cardinality(minCard,this.maxCardConstraint.cardinality());
		}

		Builder maxCard(final int maxCard) {
			return cardinality(this.minCardConstraint.cardinality(),maxCard);
		}

		Builder required() {
			return cardinality(Math.max(this.minCardConstraint.cardinality(), 1),this.maxCardConstraint.cardinality());
		}

		Builder optional() {
			return cardinality(0,this.maxCardConstraint.cardinality());
		}

		Builder unbound() {
			return cardinality(this.minCardConstraint.cardinality(),Integer.MAX_VALUE);
		}

		Builder resource() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new ResourceTypeChecker());
			return this;
		}

		Builder blankNode() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new BlankNodeTypeChecker());
			return this;
		}

		Builder uriRef() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new URIrefTypeChecker());
			return this;
		}

		Builder literal() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new LiteralTypeChecker());
			return this;
		}

		Builder typedLiteral(final String type) {
			this.typeConstraint=CustomTypeConstraint.newInstance(new TypedLiteralTypeChecker(type));
			return this;
		}

		Builder languageLiteral() {
			this.typeConstraint=CustomTypeConstraint.newInstance(new LanguageLiteralTypeChecker());
			return this;
		}

		PropertyDefinition build() {
			return new PropertyDefinition(this.property,this.typeConstraint,this.minCardConstraint,this.maxCardConstraint);
		}

	}

	static final class Evaluation {

		private final String resource;
		private final String property;
		private final List<String> values;
		private final List<String> failures;

		private Evaluation(final String resource, final String property, final List<String> values, final List<String> failures) {
			this.resource=resource;
			this.property=property;
			this.values=values;
			this.failures=failures;
		}

		String resource() {
			return this.resource;
		}

		String property() {
			return this.property;
		}

		String value() {
			return Iterables.getFirst(this.values,null);
		}

		List<String> values() {
			return this.values;
		}

		boolean passes() {
			return this.failures.isEmpty();
		}

		List<String> failures() {
			return this.failures;
		}

	}

	private static final class MaxCardinalityConstraint implements PropertyDefinition.CardinalityConstraint {

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

	private static final class MinCardinalityConstraint implements PropertyDefinition.CardinalityConstraint {

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

			private TypeDecission(final RDFNode value, final PropertyDefinition.TypeChecker checker) {
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

		static CustomTypeConstraint newInstance(final PropertyDefinition.TypeChecker checker) {
			return new CustomTypeConstraint(checker);
		}

	}

	private static final class AnyTypeChecker implements PropertyDefinition.TypeChecker {
		@Override
		public boolean isValid(final RDFNode value) {
			return true;
		}
		@Override
		public String type() {
			return "any";
		}
	}

	private static final class LanguageLiteralTypeChecker implements PropertyDefinition.TypeChecker {

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

	private static final class TypedLiteralTypeChecker implements PropertyDefinition.TypeChecker {

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

	private static final class LiteralTypeChecker implements PropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			return value.isLiteral();
		}

		@Override
		public String type() {
			return "literal";
		}

	}

	private static final class URIrefTypeChecker implements PropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			return value.isURIResource();
		}

		@Override
		public String type() {
			return "URIref";
		}

	}

	private static final class BlankNodeTypeChecker implements PropertyDefinition.TypeChecker {

		@Override
		public boolean isValid(final RDFNode value) {
			return value.isAnon();
		}

		@Override
		public String type() {
			return "blank node";
		}

	}

	private static final class ResourceTypeChecker implements PropertyDefinition.TypeChecker {

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
	private final TypeConstraint typeConstraint;
	private final CardinalityConstraint minCardConstraint;
	private final CardinalityConstraint maxCardConstraint;

	private PropertyDefinition(final String property, final TypeConstraint typeConstraint, final CardinalityConstraint minCard, final CardinalityConstraint maxCard) {
		this.property = property;
		this.typeConstraint = typeConstraint;
		this.minCardConstraint = minCard;
		this.maxCardConstraint = maxCard;
	}

	String property() {
		return this.property;
	}

	int minCard() {
		return this.minCardConstraint.cardinality();
	}

	int maxCard() {
		return this.maxCardConstraint.cardinality();
	}

	String type() {
		return this.typeConstraint.type();
	}

	Evaluation evaluate(final Resource resource) {
		final List<String> allValues=Lists.newArrayList();
		final List<String> values=Lists.newArrayList();
		final List<String> failures=Lists.newArrayList();
		final StmtIterator statements = resource.listProperties(ResourceFactory.createProperty(this.property));
		try {
			while(statements.hasNext()) {
				final Statement statement = statements.next();
				final RDFNode object = statement.getObject();
				final Decission check = this.typeConstraint.check(object);
				allValues.add(NodeUtils.toString(object));
				if(check.accepted()) {
					values.add(NodeUtils.toString(object));
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
		return new Evaluation(NodeUtils.toString(resource),this.property,values,failures);
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

	static Builder builder(final String property) {
		return new Builder(property);
	}

	private static String toString(final int value) {
		return
			value==Integer.MAX_VALUE?
				"unbound":
				Integer.toString(value);
	}

}