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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.smartdeveloperhub.vocabulary.util.PropertyDefinition.Evaluation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class PropertyDefinitionTest {

	@Rule
	public TestName name=new TestName();

	private Model model;

	private static Model load(final String relativePath) throws IOException {
		return TestHelper.load(relativePath);
	}

	private static String uriRef(final String localPart) {
		return TestHelper.uriRef("validation",localPart);
	}

	@Before
	public void setUp() throws IOException {
		this.model = load("validation.ttl");
	}

	@Test
	public void defaultMinCardinalityIsZero() {
		final PropertyDefinition definition=
			PropertyDefinition.
				builder("property").
					build();
		assertThat(definition.minCard(),equalTo(0));
	}

	@Test
	public void defaultMaxCardinalityIsMaxInteger() {
		final PropertyDefinition definition=
			PropertyDefinition.
				builder("property").
					build();
		assertThat(definition.maxCard(),equalTo(Integer.MAX_VALUE));
	}

	@Test
	public void minCardinalityCannotBeNegative() {
		try {
			PropertyDefinition.
				builder("property").
					minCard(-1);
			fail("Should not accept negative min cardinality");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Min cardinality cannot be lower than 0 (-1)"));
		}
	}

	@Test
	public void maxCardinalityCannotBeNegative() {
		try {
			PropertyDefinition.
				builder("property").
					maxCard(-1);
			fail("Should not accept negative max cardinality");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Max cardinality cannot be lower than 0 (-1)"));
		}
	}

	@Test
	public void optionalChangesMinCardinalityToZero() {
		final PropertyDefinition definition=
			PropertyDefinition.
				builder("property").
					minCard(1).
					maxCard(10).
					optional().
					build();
		assertThat(definition.minCard(),equalTo(0));
		assertThat(definition.maxCard(),equalTo(10));
	}

	@Test
	public void requiredChangesMinCardinalityToOneIfPreviousWasZero() {
		final PropertyDefinition definition=
			PropertyDefinition.
				builder("property").
					minCard(0).
					maxCard(10).
					required().
					build();
		assertThat(definition.minCard(),equalTo(1));
		assertThat(definition.maxCard(),equalTo(10));
	}

	@Test
	public void requiredDoesNotChangeMinCardinalityIfPreviousWasEqualToOne() {
		final PropertyDefinition definition=
			PropertyDefinition.
				builder("property").
					minCard(1).
					maxCard(10).
					required().
					build();
		assertThat(definition.minCard(),equalTo(1));
		assertThat(definition.maxCard(),equalTo(10));
	}

	@Test
	public void requiredDoesNotChangeMinCardinalityIfPreviousWasGreaterThanOne() {
		final PropertyDefinition definition=
			PropertyDefinition.
				builder("property").
					minCard(3).
					maxCard(10).
					required().
					build();
		assertThat(definition.minCard(),equalTo(3));
		assertThat(definition.maxCard(),equalTo(10));
	}

	@Test
	public void unboundChangesMaxCardinalityToMaxInteger() {
		final PropertyDefinition definition=
			PropertyDefinition.
				builder("property").
					minCard(0).
					maxCard(10).
					unbound().
					build();
		assertThat(definition.minCard(),equalTo(0));
		assertThat(definition.maxCard(),equalTo(Integer.MAX_VALUE));
	}

	@Test
	public void maxCardinalityCannotBeLowerThanMinCardinality() {
		try {
			PropertyDefinition.
				builder("property").
					cardinality(5, 4);
			fail("Should not accept a max cardinality lower than min cardinality");
		} catch (final IllegalArgumentException e) {
			assertThat(e.getMessage(),equalTo("Max cardinality cannot be lower than min cardinality (4<5)"));
		}
	}

	@Test
	public void evaluationFailsIfNoResourceAreDetectedWhenRequired() throws Exception {
		final Resource resource = resource("noResource");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					resource().
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(4));
	}

	@Test
	public void evaluationFailsIfNoURIrefsAreDetectedWhenRequired() throws Exception {
		final Resource resource = resource("noURIref");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					uriRef().
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(5));
	}

	@Test
	public void evaluationFailsIfNoBlankNodesAreDetectedWhenRequired() throws Exception {
		final Resource resource = resource("noBlankNode");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					blankNode().
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(6));
	}

	@Test
	public void evaluationFailsIfNoLiteralsAreDetectedWhenRequired() throws Exception {
		final Resource resource = resource("noLiteral");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					literal().
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(3));
	}

	@Test
	public void evaluationFailsIfNoLanguageLiteralsAreDetectedWhenRequired() throws Exception {
		final Resource resource = resource("noLanguageLiteral");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					languageLiteral().
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(6));
	}

	@Test
	public void evaluationFailsIfNoTypedLiteralsAreDetectedWhenRequired() throws Exception {
		final Resource resource = resource("noTypedLiteral");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					typedLiteral(uriRef("type")).
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(7));
	}

	@Test
	public void evaluationFailsIfNotEnoughValuesAreDefined() throws Exception {
		final Resource resource = resource("notEnoughValues");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					cardinality(10,Integer.MAX_VALUE).
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(1));
	}

	@Test
	public void evaluationFailsIfTooManyValuesAreDefined() throws Exception {
		final Resource resource = resource("tooManyValues");
		final PropertyDefinition definition =
			PropertyDefinition.
				builder(uriRef("property")).
					cardinality(0,1).
					build();
		final Evaluation evaluation = evaluate(definition, resource);
		assertThat(evaluation.passes(),equalTo(false));
		assertThat(evaluation.failures().size(),equalTo(1));
	}

	private Evaluation evaluate(final PropertyDefinition definition, final Resource resource) {
		final Evaluation evaluation=definition.evaluate(resource);
		System.out.println("Test: "+this.name.getMethodName());
		System.out.println("- Definition: "+definition);
		System.out.println("- Resource..: "+resource);
		if(!evaluation.passes()) {
			System.out.println("- Evaluation: failed");
			for(final String failure:evaluation.failures()) {
				System.out.println("   + "+failure);
			}
		} else {
			System.out.println("- Evaluation: passes");
		}
		return evaluation;
	}

	private Resource resource(final String resourceName) {
		return this.model.getResource(uriRef(resourceName));
	}

}
