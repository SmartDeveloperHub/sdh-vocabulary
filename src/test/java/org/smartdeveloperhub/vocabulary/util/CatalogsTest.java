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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class CatalogsTest {

	private static final Path ROOT=Paths.get("src","main","resources","vocabulary");
	private static final URI BASE=URI.create("http://www.smartdeveloperhub.org/vocabulary/");

	private Catalog catalog;

	@Before
	public void setUp() throws IOException {
		final Result<Catalog> result = Catalogs.loadFrom(ROOT,BASE);
		assertThat(result.isAvailable(),equalTo(true));
		this.catalog = result.get();
		assertThat(this.catalog,notNullValue());
	}

	@Test
	public void absoluteCanonicalModulesCanBeResolved() throws Exception {
		for(final String relativePath:this.catalog.canonicalModules()) {
			final URI ontology = absoluteResource(relativePath);
			final Module module=this.catalog.resolve(ontology);
			assertThat(module,notNullValue());
			assertThat(module.relativePath(),not(equalTo(relativePath)));
			assertThat(module.ontology(),equalTo(ontology.toString()));
		}
	}

	@Test
	public void absoluteVersionModulesCanBeResolved() throws Exception {
		for(final String relativePath:this.catalog.versionModules()) {
			final Module module=this.catalog.resolve(absoluteResource(relativePath));
			assertThat(module,notNullValue());
			assertThat(module.relativePath(),equalTo(relativePath));
		}
	}

	@Test
	public void absoluteExternalModulesCanBeResolved() throws Exception {
		for(final String relativePath:this.catalog.externalModules()) {
			final Module module=this.catalog.resolve(absoluteResource(relativePath));
			assertThat(module,notNullValue());
			assertThat(module.relativePath(),equalTo(relativePath));
		}
	}

	@Test
	public void absoluteExternalModuleImplementationsCanBeResolved() throws Exception {
		for(final String relativePath:this.catalog.externalModules()) {
			final Module module=this.catalog.resolve(absoluteResource(relativePath));
			assumeThat(module,notNullValue());
			assumeThat(module.relativePath(),equalTo(relativePath));
			final URI externalLocation = module.context().getImplementationEndpoint(module.location());
			final Module same=this.catalog.resolve(externalLocation);
			assertThat(same,sameInstance(module));
		}
	}

	@Test
	public void relativeCanonicalModulesCanBeResolved() throws Exception {
		for(final String relativePath:this.catalog.canonicalModules()) {
			final Module module=this.catalog.resolve(relativeResource(relativePath));
			assertThat(module,notNullValue());
			assertThat(module.relativePath(),not(equalTo(relativePath)));
			assertThat(module.ontology(),equalTo(absoluteResource(relativePath).toString()));
		}
	}

	@Test
	public void relativeVersionModulesCanBeResolved() throws Exception {
		for(final String relativePath:this.catalog.versionModules()) {
			final Module module=this.catalog.resolve(relativeResource(relativePath));
			assertThat(module,notNullValue());
			assertThat(module.relativePath(),equalTo(relativePath));
		}
	}

	@Test
	public void relativeExternalModulesCanBeResolved() throws Exception {
		for(final String relativePath:this.catalog.externalModules()) {
			final Module module=this.catalog.resolve(relativeResource(relativePath));
			assertThat(module,notNullValue());
			assertThat(module.relativePath(),equalTo(relativePath));
		}
	}

	@Test
	public void dealsNamespaces() throws IOException {
		final Result<Catalog> result = Catalogs.loadFrom(TestHelper.TEST_ROOT,TestHelper.BASE);
		assertThat(result.toString(),result.isAvailable(),equalTo(true));
		final Catalog catalog = result.get();
		assertThat(catalog,notNullValue());
		for(final String moduleId:catalog.modules()) {
			final Module module=catalog.get(moduleId);
			assertThat(module,notNullValue());
		}

		assertThat(catalog.resolve(absoluteResource("")),notNullValue());
		assertThat(catalog.resolve(absoluteResource("index")),nullValue());

		final Module hashModule = catalog.resolve(absoluteResource("hash#"));
		assertThat(hashModule,notNullValue());
		assertThat(catalog.resolve(absoluteResource("hash")),sameInstance(hashModule));
		assertThat(catalog.resolve(absoluteResource("v1/hash#")),sameInstance(hashModule));
		assertThat(catalog.resolve(absoluteResource("v1/hash")),sameInstance(hashModule));

		final Module slashModule = catalog.resolve(absoluteResource("slash/"));
		assertThat(slashModule,notNullValue());
		assertThat(catalog.resolve(absoluteResource("v1/slash/")),sameInstance(slashModule));
		assertThat(catalog.resolve(absoluteResource("v1/slash/index")),nullValue());
	}

	private static URI absoluteResource(final String relativePath) {
		return BASE.resolve(relativePath);
	}

	private static URI relativeResource(final String relativePath) {
		return URI.create(relativePath);
	}

	void generateCode() throws Exception {
		final Result<Catalog> result = Catalogs.loadFrom(ROOT,BASE);
		if(result.isAvailable()) {
			final Catalog catalog = result.get();
			System.out.println(generateStringArray(catalog.externalModules(), "externals"));
			System.out.println(generateStringArray(catalog.canonicalModules(), "canonicals"));
			System.out.println(generateStringArray(catalog.versionModules(), "versions"));
		}
	}

	private String generateStringArray(final List<String> values, final String variable) {
		final StringBuilder builder=new StringBuilder();
		builder.append("final String[] ").append(variable).append("={");
		final Iterator<String> iterator=values.iterator();
		while(iterator.hasNext()) {
			builder.append("\n  \"").append(iterator.next()).append("\"");
			if(iterator.hasNext()) {
				builder.append(",");
			}
		}
		builder.append("\n}");
		final String value = builder.toString();
		return value;
	}

}
