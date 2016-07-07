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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.4.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Paths;

import org.junit.Test;

public class ModulesTest {

	@Test
	public void validatesProjectVocabularies() throws Exception {
		final Result<Catalog> result = Catalogs.loadFrom(TestHelper.MAIN_ROOT,TestHelper.BASE);
		if(result.isAvailable()) {
			final Catalog catalog=result.get();
			for(final String moduleId:catalog.modules()) {
				final Module module=catalog.get(moduleId);
				if(module.isExternal()) {
					assertThat("External module "+moduleId+" should be well located",Modules.isLocatedProperly(module),equalTo(true));
				} else {
					assertThat("Local module "+moduleId+" is not located properly",Modules.isLocatedProperly(module),equalTo(true));
				}
			}
		}
	}

	@Test
	public void validatesHashNamespaceNonVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.MAIN_CONTEXT).
				withLocation(Paths.get("src","main","resources","vocabulary","test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test#");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void validatesHashNamespaceVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.MAIN_CONTEXT).
				withLocation(Paths.get("src","main","resources","vocabulary","test","v1","test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test#").
				withVersionIRI("http://www.smartdeveloperhub.org/vocabulary/test/v1/test#");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void validatesSlashNamespaceNonVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.MAIN_CONTEXT).
				withLocation(Paths.get("src","main","resources","vocabulary","test","index.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void validatesSlashNamespaceVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.MAIN_CONTEXT).
				withLocation(Paths.get("src","main","resources","vocabulary","test","v1","index.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/").
				withVersionIRI("http://www.smartdeveloperhub.org/vocabulary/test/v1/");
		assertThat(Modules.isLocatedProperly(module),equalTo(true));
	}

	@Test
	public void rejectsSlashNamespaceNonVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.MAIN_CONTEXT).
				withLocation(Paths.get("src","main","resources","vocabulary","test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/");
		assertThat(Modules.isLocatedProperly(module),equalTo(false));
	}

	@Test
	public void rejectsSlashNamespaceVersionedLocalModules() throws Exception {
		final Module module=
			new Module(TestHelper.MAIN_CONTEXT).
				withLocation(Paths.get("src","main","resources","vocabulary","test","v1","test.ttl")).
				withOntology("http://www.smartdeveloperhub.org/vocabulary/test/").
				withVersionIRI("http://www.smartdeveloperhub.org/vocabulary/test/v1/");
		assertThat(Modules.isLocatedProperly(module),equalTo(false));
	}

}
