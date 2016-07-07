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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import org.junit.Test;

public class NamespaceTest {

	private static final String HASH_CANONICAL="http://www.smartdeveloperhub.org/vocabulary/hash";
	private static final String HASH_ALTERNATIVE="http://www.smartdeveloperhub.org/vocabulary/hash#";
	private static final String SLASH="http://www.smartdeveloperhub.org/vocabulary/slash/";

	@Test
	public void hashNamespaceInvariantForCanonicalForm() throws Exception {
		final Namespace sut = Namespace.create(HASH_CANONICAL);
		assertThat(sut.uri(),equalTo(HASH_CANONICAL));
		assertThat(sut.canonicalForm(),equalTo(sut.uri()));
		assertThat(sut.variants(),hasItems(HASH_ALTERNATIVE,HASH_CANONICAL));
		assertThat(sut.variants(),hasSize(2));
	}

	@Test
	public void hashNamespaceInvariantForAlternativeForm() throws Exception {
		final Namespace sut = Namespace.create(HASH_ALTERNATIVE);
		assertThat(sut.uri(),equalTo(HASH_ALTERNATIVE));
		assertThat(sut.canonicalForm(),equalTo(HASH_CANONICAL));
		assertThat(sut.variants(),hasItems(HASH_ALTERNATIVE,HASH_CANONICAL));
		assertThat(sut.variants(),hasSize(2));
	}

	@Test
	public void slashNamespaceInvariant() throws Exception {
		final Namespace sut = Namespace.create(SLASH);
		assertThat(sut.uri(),equalTo(SLASH));
		assertThat(sut.canonicalForm(),equalTo(sut.uri()));
		assertThat(sut.variants(),hasItems(SLASH));
		assertThat(sut.variants(),hasSize(1));
	}

	@Test
	public void hashNamespacesWithSameCanonicalFormAreEqual() {
		final Namespace one = Namespace.create(HASH_CANONICAL);
		final Namespace other = Namespace.create(HASH_ALTERNATIVE);
		assertThat(one,equalTo(other));
	}

	@Test
	public void slashNamespacesWithSameURIAreEqual() {
		final Namespace one = Namespace.create(SLASH);
		final Namespace other = Namespace.create(SLASH);
		assertThat(one,equalTo(other));
	}

}
