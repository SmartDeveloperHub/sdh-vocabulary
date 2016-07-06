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
package org.smartdeveloperhub.vocabulary.ci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.BeforeClass;
import org.junit.Test;

public class URITests {

	private final URI o1 = new URI("http://www.smartdeveloperhub.org/vocabulary/ontology1#term1");
	private final URI o2 = new URI("http://www.smartdeveloperhub.org/vocabulary/ontology1#term2");
	private final URI o3 = new URI("http://www.smartdeveloperhub.org/vocabulary/ontology2#term1");
	private final String o4 = "data";

	@BeforeClass
	public static void setUpBefore() {
		SesameRDFUtil.install();
	}

	@Test(expected=IllegalArgumentException.class)
	public void uriMustHaveColon() {
		new URI("Non absolute");
	}

	@Test
	public void uriIsEqualToSelf() throws Exception {
		assertThat(this.o1,equalTo(this.o1));
	}

	@Test
	public void urisWithDifferentLocalNameAreDifferent() throws Exception {
		assertThat(this.o1,not(equalTo(this.o2)));
	}

	@Test
	public void urisWithDifferentNamespaceAreDifferent() throws Exception {
		assertThat(this.o1,not(equalTo(this.o3)));
	}

	@Test
	public void uriIsNotEqualToNonUris() throws Exception {
		assertThat((Object)this.o1,not(equalTo((Object)this.o4)));
	}

	@Test
	public void uriIsNotEqualToNull() throws Exception {
		assertThat(this.o1,not(equalTo(null)));
	}

}
