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
package org.smartdeveloperhub.vocabulary.publisher;

import static io.undertow.Handlers.path;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.contentNegotiation;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.methodController;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.moduleReverseProxy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import org.ldp4j.http.CharacterEncodings;
import org.ldp4j.http.MediaType;
import org.ldp4j.http.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.vocabulary.config.ConfigurationFactory;
import org.smartdeveloperhub.vocabulary.publisher.config.DocumentationConfig;
import org.smartdeveloperhub.vocabulary.publisher.config.PublisherConfig;
import org.smartdeveloperhub.vocabulary.publisher.handlers.ContentNegotiationHandler;
import org.smartdeveloperhub.vocabulary.publisher.handlers.NegotiableContent;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeployment;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeploymentFactory;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationProvider;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationProviderFactory;
import org.smartdeveloperhub.vocabulary.util.AppAssembler;
import org.smartdeveloperhub.vocabulary.util.Application;
import org.smartdeveloperhub.vocabulary.util.Catalog;
import org.smartdeveloperhub.vocabulary.util.Catalogs;
import org.smartdeveloperhub.vocabulary.util.Module;
import org.smartdeveloperhub.vocabulary.util.Module.Format;
import org.smartdeveloperhub.vocabulary.util.Result;
import org.smartdeveloperhub.vocabulary.util.SerializationManager;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import io.undertow.Undertow;
import io.undertow.server.handlers.CanonicalPathHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Methods;

public class VocabularyPublisher {

	private static final class DefaultDocumentationProviderFactory implements DocumentationProviderFactory {

		private final DocumentationStrategy strategy;

		private DefaultDocumentationProviderFactory(final DocumentationStrategy strategy) {
			this.strategy = strategy;
		}

		@Override
		public DocumentationProvider create(final Module module) {
			return ImmutableDocumentationProvider.create(this.strategy,module);
		}

	}

	private static final class DefaultDocumentationDeploymentFactory implements DocumentationDeploymentFactory {

		private final DocumentationStrategy strategy;

		private DefaultDocumentationDeploymentFactory(final DocumentationStrategy strategy) {
			this.strategy = strategy;
		}

		@Override
		public DocumentationDeployment create(final Module module) {
			return ImmutableDocumentationDeployment.create(this.strategy,module);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(VocabularyPublisher.class);

	private static final MediaType HTML = MediaTypes.of("text","html");

	public static void main(final String... args) throws FileNotFoundException, IOException {
		if(args.length!=1) {
			System.err.printf("Invalid argument number: 1 argument required (%d)%n", args.length);
			System.err.printf("USAGE: %s <path-to-config-file>%n",AppAssembler.applicationName(VocabularyPublisher.class));
			System.err.printf("  <path-to-config-file> : Path Vocabulary Publisher configuration file is available.%n");
			Application.logContext(args);
			System.exit(-1);
		}
		System.out.printf("Vocabulary Publisher%s%n",serviceVersion());
		try {
			final Path configFile = Paths.get(args[0]);
			final PublisherConfig config =
				ConfigurationFactory.
					load(
						Resources.toString(configFile.toUri().toURL(), StandardCharsets.UTF_8),
						PublisherConfig.class);
			System.out.printf("- Base URI: %s%n",config.getBase());
			System.out.printf("- Server..: %s:%s%n",config.getServer().getHost(),config.getServer().getPort());
			System.out.printf("- Source directory: %s%n",config.getRoot().toAbsolutePath());
			setup(config);
		} catch (final InvalidPathException e) {
			System.err.printf("%s is not a valid root path (%s)%n", args[0],e.getMessage());
			System.exit(-2);
		} catch (final IOException e) {
			System.err.printf("Could not explore modules (%s)%n", e.getMessage());
			System.exit(-3);
		} catch (final RuntimeException e) {
			System.err.println("Unexpected publisher failure\n. Full stacktrace follows");
			e.printStackTrace(System.err);
			System.exit(-4);
		}
	}

	private static void setup(final PublisherConfig config) throws IOException {
		final Result<Catalog> result = Catalogs.loadFrom(config.getRoot(), config.getBase());
		if(result.isAvailable()) {
			final Catalog catalog = result.get();
			showCatalog(catalog);
			try {
				publish(
					catalog,
					config.getBase().getPath(),
					".cache",
					config.getServer().getPort(),
					config.getServer().getHost(),
					getDocumentationStrategy(config));
			} finally {
				System.out.println("Publisher terminated.");
			}
		} else {
			System.err.println("Could not prepare catalog:\n"+result);
			System.exit(-5);
		}
	}

	private static String serviceVersion() {
		final String build=serviceBuild();
		final String version=System.getProperty("service.version","");
		if(version.isEmpty()) {
			return version;
		}
		return " v"+version+build;
	}

	private static String serviceBuild() {
		String build = System.getProperty("service.build","");
		if(!build.isEmpty()) {
			build="-b"+build;
		}
		return build;
	}

	private static void showCatalog(final Catalog catalog) {
		LOGGER.debug("Found {} modules",catalog.size());
		final List<Module> externals=Lists.newArrayList();
		for(final String moduleId:catalog.modules()) {
			final Module module = catalog.get(moduleId);
			if(module.isExternal()) {
				externals.add(module);
				continue;
			}
			showModule(module);
		}
		for(final Module module:externals) {
			showDependency(module);
		}
	}

	private static void showDependency(final Module module) {
		final StringBuilder builder=
			new StringBuilder("- [EXTERNAL] Module '").
				append(module.location()).
				append("' [").
				append(module.relativePath()).
				append("] (").
				append(module.format().getName()).
				append(")");
		if(module.isOntology()) {
			builder.append(" refers to ");
			if(module.isVersion()) {
				builder.append("version '").append(module.versionIRI()).append("' of ");
			}
			builder.append("IRI '").append(module.ontology()).append("'");
			LOGGER.debug(builder.toString());
		}
	}

	private static void showModule(final Module module) {
		LOGGER.debug("- Module '{}':",module.location());
		LOGGER.debug("  + Relative path: {}",module.relativePath());
		LOGGER.debug("  + Format: {}",module.format().getName());
		if(module.isOntology()) {
			LOGGER.debug("  + Ontology:");
			LOGGER.debug("    * IRI: {}",module.ontology());
			if(module.isVersion()) {
				LOGGER.debug("    * VersionIRI: {}",module.versionIRI());
			}
			if(module.hasImports()) {
				LOGGER.debug("    * Imports:");
				for(final String importedModule:module.imports()) {
					LOGGER.debug("      - {}",importedModule);
				}
			}
		}
	}

	private static void publish(final Catalog catalog,final String basePath, final String serializationCachePath, final int port, final String host, final DocumentationStrategy strategy) throws IOException {
		LOGGER.debug("* Publishing vocabularies under {}",basePath);
		final PathHandler pathHandler=path();
		// Module serializations
		final SerializationManager manager=publishSerializations(catalog,pathHandler,serializationCachePath);
		// Catalog documentation
		final DocumentationDeploymentFactory factory = publishDocumentation(catalog,pathHandler,strategy);
		// Canonical namespaces
		publishCanonicalNamespace(catalog, basePath, pathHandler, manager, factory);
		final Undertow server =
			Undertow.
				builder().
					addHttpListener(port,host).
					setHandler(new CanonicalPathHandler(pathHandler)).
					build();
		server.start();
		awaitTerminationRequest();
		server.stop();
	}

	private static void publishCanonicalNamespace(final Catalog catalog, final String basePath, final PathHandler pathHandler, final SerializationManager manager, final DocumentationDeploymentFactory factory) {
		final ContentNegotiationHandler contentNegotiation = contentNegotiation().
			negotiate(
				negotiableModuleContent(),
				new ModuleRepresentionGenerator(manager));
		if(factory!=null) {
			contentNegotiation.
				negotiate(
					NegotiableContent.newInstance().support(HTML),
					new ModuleDocumentationRedirector(factory));
		}
		pathHandler.
			addPrefixPath(
				basePath,
				moduleReverseProxy(
					catalog,
					methodController(
						contentNegotiation).
						allow(Methods.GET)
				)
			);
	}

	private static DocumentationDeploymentFactory publishDocumentation(final Catalog catalog, final PathHandler pathHandler, final DocumentationStrategy strategy) {
		if(strategy==null) {
			return null;
		}
		final DocumentationDeploymentFactory deploymentFactory=new DefaultDocumentationDeploymentFactory(strategy);
		final DocumentationDeployer deployer=
			DocumentationDeployer.
				create(
					deploymentFactory,
					new DefaultDocumentationProviderFactory(strategy));
		deployer.deploy(catalog,pathHandler);
		return deploymentFactory;
	}

	private static SerializationManager publishSerializations(final Catalog catalog, final PathHandler pathHandler, final String cachePath) throws IOException {
			final SerializationManager manager=SerializationManager.create(catalog,Paths.get(cachePath));
			for(final String moduleId:catalog.modules()) {
				final Module module=catalog.get(moduleId);
				LOGGER.debug("- Module ({}):",module.implementationIRI(),module.location());
				for(final Format format:Format.values()) {
					final String resourceName = module.relativePath()+"."+format.fileExtension();
					final URI location=catalog.getBase().resolve(resourceName);
					LOGGER.debug("  + {} : {} --> {} ({})",format.getName(),location,location.getPath(),manager.getSerialization(module, format).toAbsolutePath());
					pathHandler.
						addExactPath(
							location.getPath(),
							methodController(
								contentNegotiation().
									negotiate(
										NegotiableContent.
											newInstance().
												support(Formats.toMediaType(format)).
												support(CharacterEncodings.of(StandardCharsets.UTF_8)).
												support(CharacterEncodings.of(StandardCharsets.ISO_8859_1)).
												support(CharacterEncodings.of(StandardCharsets.US_ASCII)),
										new SerializationHandler(manager,module,format))).
							allow(Methods.GET));
				}
			}
			return manager;
	}

	private static DocumentationStrategy getDocumentationStrategy(final PublisherConfig config) {
		final DocumentationConfig docConfig = getDocumentationConfig(config);
		if(docConfig==null) {
			return null;
		}
		return new DocumentationStrategy(docConfig.getRoot(),docConfig.getRelativePath());
	}

	private static DocumentationConfig getDocumentationConfig(final PublisherConfig config) {
		final DocumentationConfig docConfig = config.extension(DocumentationConfig.class);
		if(docConfig==null) {
			return null;
		}
		if(docConfig.getRoot()==null) {
			docConfig.setRoot(config.getRoot().getParent().resolve("docs/"));
		}
		if(docConfig.getRelativePath()==null) {
			docConfig.setRelativePath("html");
		}
		System.out.printf("- Documentation directory....: %s%n",docConfig.getRoot().toAbsolutePath());
		System.out.printf("- Documentation relative path: %s%n",docConfig.getRelativePath());
		return docConfig;
	}

	private static NegotiableContent negotiableModuleContent() {
		return
			NegotiableContent.
				newInstance().
					support(Formats.toMediaType(Format.TURTLE)).
					support(Formats.toMediaType(Format.RDF_XML)).
					support(Formats.toMediaType(Format.JSON_LD)).
					support(CharacterEncodings.of(StandardCharsets.UTF_8)).
					support(CharacterEncodings.of(StandardCharsets.ISO_8859_1)).
					support(CharacterEncodings.of(StandardCharsets.US_ASCII));
	}

	private static void awaitTerminationRequest() {
		System.out.println("Hit <ENTER> to exit...");
		try(final Scanner scanner = new Scanner(System.in)) {
			String readString = scanner.nextLine();
			while(readString != null) {
				if (readString.isEmpty()) {
					break;
				}
				if (scanner.hasNextLine()) {
					readString = scanner.nextLine();
				} else {
					readString = null;
				}
			}
		}
		System.out.println("<ENTER> detected.");
	}

}
