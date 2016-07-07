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
package org.smartdeveloperhub.vocabulary.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.smartdeveloperhub.vocabulary.publisher.config.PublisherConfig;
import org.smartdeveloperhub.vocabulary.publisher.config.ServerConfig;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;

public class ConfigurationFactoryTest {

	public static class DocumentationConfig {

		private Path root;

		public Path getRoot() {
			return this.root;
		}

		public void setRoot(final Path root) {
			this.root = root;
		}

	}

	@ConfigurationExtension("vocab")
	public static class VocabConfig {
		private String title;
		private String date;
		private String copyright;

		public String getTitle() {
			return this.title;
		}

		public void setTitle(final String title) {
			this.title = title;
		}

		public String getDate() {
			return this.date;
		}

		public void setDate(final String date) {
			this.date= date;
		}

		public String getCopyright() {
			return this.copyright;
		}

		public void setCopyright(final String copyright) {
			this.copyright = copyright;
		}
	}

	@ConfigurationExtension("simple")
	public static class CollectConfig {

		private List<String> list;
		private Set<String> set;

		public CollectConfig() {
			this.list=Lists.newArrayList();
			this.set=Sets.newHashSet();
		}

		public List<String> getList() {
			return this.list;
		}

		public void setList(final List<String> list) {
			this.list = list;
		}

		public Set<String> getSet() {
			return this.set;
		}

		public void setSet(final Set<String> set) {
			this.set = set;
		}

	}

	@ConfigurationExtension("servers")
	public static class ServersConfig {

		private List<ServerConfig> list;

		public ServersConfig() {
			this.list=Lists.newArrayList();
		}

		public List<ServerConfig> getHosts() {
			return this.list;
		}

		public void setHosts(final List<ServerConfig> list) {
			this.list = list;
		}

	}

	@Test
	public void configurationsSavedCanBeLoaded() throws Exception {
		final VocabConfig vocabOriginal = aVocabConfig();
		final ServerConfig serverOriginal = aServerConfig();
		final DocumentationConfig docOriginal = aDocumentationConfig();

		final PublisherConfig config = aPublisherConfig(serverOriginal);
		config.setExtension("documentation",docOriginal);
		config.extend(vocabOriginal);

		final String strConfig = ConfigurationFactory.save(config);
		assertThat(strConfig,notNullValue());

		System.out.println("* Serialized configuration:");
		System.out.println(strConfig);

		final Configuration loaded = ConfigurationFactory.load(strConfig);
		assertThat(loaded,notNullValue());

		final DocumentationConfig docLoaded = loaded.extension("documentation",DocumentationConfig.class);
		assertThat(docLoaded,notNullValue());
		assertThat(docLoaded.getRoot(),equalTo(docOriginal.getRoot()));

		final VocabConfig vocabLoaded=loaded.extension(VocabConfig.class);
		assertThat(vocabLoaded,notNullValue());
		assertThat(vocabLoaded.getTitle(),equalTo(vocabOriginal.getTitle()));
		assertThat(vocabLoaded.getCopyright(),equalTo(vocabOriginal.getCopyright()));
		assertThat(vocabLoaded.getDate(),equalTo(vocabOriginal.getDate()));
	}

	@Test
	public void canLoadConfigurationsUsingAdvancedYAMLConstructs() throws Exception {
		final String strConfig=Resources.toString(Resources.getResource("config/publisher.yaml"),StandardCharsets.UTF_8);

		System.out.println("* Loaded configuration (see advanced YAML constructs):");
		System.out.println(strConfig);

		final PublisherConfig loaded = ConfigurationFactory.load(strConfig,PublisherConfig.class);
		assertThat(loaded,notNullValue());

		final DocumentationConfig docLoaded = loaded.extension("documentation",DocumentationConfig.class);
		assertThat(docLoaded,notNullValue());
		assertThat(docLoaded.getRoot(),equalTo(loaded.getRoot()));

		final VocabConfig vocabLoaded=loaded.extension(VocabConfig.class);
		assertThat(vocabLoaded,notNullValue());
		assertThat(vocabLoaded.getTitle(),equalTo("My site"));
	}

	@Test
	public void handlesSimpleCollections() throws Exception {
		final ServerConfig serverOriginal = aServerConfig();

		final PublisherConfig config = aPublisherConfig(serverOriginal);
		final CollectConfig collect = new CollectConfig();
		collect.getList().add("Value1");
		collect.getList().add("Value2");
		collect.getList().add("Value1");
		collect.getSet().add("S1");
		collect.getSet().add("S2");
		config.extend(collect);

		final String strConfig = ConfigurationFactory.save(config);
		assertThat(strConfig,notNullValue());

		System.out.println("* Serialized configuration:");
		System.out.println(strConfig);

		final Configuration loaded = ConfigurationFactory.load(strConfig);
		assertThat(loaded,notNullValue());

		final CollectConfig collectLoaded=loaded.extension(CollectConfig.class);
		assertThat(collectLoaded,notNullValue());
		assertThat(collectLoaded.getList(),equalTo(collect.getList()));
		assertThat(collectLoaded.getSet(),equalTo(collect.getSet()));
	}

	@Test
	public void handlesComplexCollections() throws Exception {
		final ServerConfig serverOriginal = aServerConfig();
		final PublisherConfig config = aPublisherConfig(serverOriginal);
		final ServersConfig servers = new ServersConfig();
		servers.getHosts().add(serverOriginal);
		final ServerConfig alternativeServer = new ServerConfig();
		alternativeServer.setHost("127.0.0.1");
		alternativeServer.setPort(1234);
		servers.getHosts().add(alternativeServer);
		config.extend(servers);

		final String strConfig = ConfigurationFactory.save(config);
		assertThat(strConfig,notNullValue());

		System.out.println("* Serialized configuration:");
		System.out.println(strConfig);

		final Configuration loaded = ConfigurationFactory.load(strConfig);
		assertThat(loaded,notNullValue());

		final ServersConfig collectLoaded=loaded.extension(ServersConfig.class);
		assertThat(collectLoaded,notNullValue());
		assertThat(collectLoaded.getHosts().size(),equalTo(servers.getHosts().size()));
	}

	private DocumentationConfig aDocumentationConfig() {
		final DocumentationConfig docConfig = new DocumentationConfig();
		docConfig.setRoot(Paths.get("src","test","resources","html"));
		return docConfig;
	}

	private PublisherConfig aPublisherConfig(final ServerConfig server) {
		final PublisherConfig config=new PublisherConfig();
		config.setBase(URI.create("http://www.smartdeveloperhub.org/vocabulary/"));
		config.setRoot(Paths.get("src","test","resources","vocabulary"));
		config.setServer(server);
		return config;
	}

	private ServerConfig aServerConfig() {
		final ServerConfig server = new ServerConfig();
		server.setHost("localhost");
		server.setPort(8080);
		return server;
	}

	private VocabConfig aVocabConfig() {
		final VocabConfig vocab=new VocabConfig();
		vocab.setTitle("My site");
		vocab.setCopyright("Me");
		vocab.setDate("2016");
		return vocab;
	}

}
