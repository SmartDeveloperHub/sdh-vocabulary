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
package org.smartdeveloperhub.vocabulary.publisher;

import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.contentNegotiation;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.methodController;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.ldp4j.http.CharacterEncodings;
import org.ldp4j.http.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers;
import org.smartdeveloperhub.vocabulary.publisher.handlers.NegotiableContent;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeployment;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeploymentFactory;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationProvider;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationProviderFactory;
import org.smartdeveloperhub.vocabulary.publisher.util.Location;
import org.smartdeveloperhub.vocabulary.util.Catalog;
import org.smartdeveloperhub.vocabulary.util.Module;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Methods;

final class DocumentationDeployer {

	private static final Logger LOGGER=LoggerFactory.getLogger(DocumentationDeployer.class);

	private final DocumentationDeploymentFactory deploymentFactory;
	private final DocumentationProviderFactory providerFactory;

	private DocumentationDeployer(
			final DocumentationDeploymentFactory deploymentFactory,
			final DocumentationProviderFactory providerFactory) {
		this.deploymentFactory = deploymentFactory;
		this.providerFactory = providerFactory;
	}

	void deploy(final Catalog catalog, final PathHandler pathHandler) {
		for(final String moduleId:catalog.modules()) {
			final Module module=catalog.get(moduleId);
			if(!module.isLocal()) {
				continue;
			}
			final DocumentationDeployment deployment=this.deploymentFactory.create(module);
			final DocumentationProvider provider=this.providerFactory.create(module);

			logModuleDeployment(module,deployment,provider);

			pathHandler.
				addExactPath(
					deployment.implementationLandingPage().getPath(),
					methodController(
						contentNegotiation().
							negotiate(
								negotiableContent(),
								new ModuleLandingPage(deployment,provider,false))).
						allow(Methods.GET)
				).
				addPrefixPath(
					deployment.implementationRoot().getPath(),
					methodController(
						documentationAssetHandler(deployment, provider)
						).allow(Methods.GET)
				);
			if(module.isVersion()) {
				final URI r1=
					Location.
						relativize(
							module.context().base(),
							deployment.canonicalLandingPage(),
							deployment.implementationLandingPage());
				pathHandler.
					addExactPath(
						deployment.canonicalLandingPage().getPath(),
						methodController(MoreHandlers.temporaryRedirect(r1)).allow(Methods.GET)
					);
				final URI canonicalRoot = deployment.canonicalLandingPage().resolve(".");
				if(!canonicalRoot.equals(deployment.canonicalLandingPage())) {
					final URI r2=
						Location.
							relativize(
								module.context().base(),
								canonicalRoot,
								deployment.implementationLandingPage());
					pathHandler.
						addExactPath(
							canonicalRoot.getPath(),
							methodController(MoreHandlers.temporaryRedirect(r2)).allow(Methods.GET)
						);
				}
			}
		}
	}

	private void logModuleDeployment(final Module module, final DocumentationDeployment deployment, final DocumentationProvider provider) {
		LOGGER.debug("- Documentation ({}):",module.implementationIRI());
		LOGGER.debug("  + Root........: {} --> {} ({})",deployment.implementationRoot(),deployment.implementationRoot().getPath(),provider.assetsPath());
		LOGGER.debug("  + Landing page: {} --> {}",deployment.implementationLandingPage(),deployment.implementationLandingPage().getPath());
		if(!deployment.implementationRoot().equals(deployment.implementationLandingPage())) {
			LOGGER.debug("  + Reference...: {} --> {}",deployment.implementationRoot(),deployment.implementationLandingPage().getPath());
		}
		if(module.isVersion()) {
			LOGGER.debug("  + Redirection.: {} --> {}",deployment.canonicalLandingPage(),deployment.implementationLandingPage().getPath());
			final URI canonicalRoot = deployment.canonicalLandingPage().resolve(".");
			if(!canonicalRoot.equals(deployment.canonicalLandingPage())) {
				LOGGER.debug("  + Redirection.: {} --> {}",canonicalRoot,deployment.implementationLandingPage().getPath());
			}
		}
	}

	private HttpHandler documentationAssetHandler(final DocumentationDeployment deployment, final DocumentationProvider provider) {
		final HttpHandler assetHandler=new ExternalAssetProvider(provider);
		if(!deployment.implementationRoot().equals(deployment.implementationLandingPage())) {
			return
				new HttpHandler() {
					private final HttpHandler contentHandler=
						contentNegotiation().
							negotiate(
								negotiableContent(),
								new ModuleLandingPage(deployment,provider,true));
					@Override
					public void handleRequest(final HttpServerExchange exchange) throws Exception {
						if(exchange.getRelativePath().equals("/")) {
							this.contentHandler.handleRequest(exchange);
						} else {
							assetHandler.handleRequest(exchange);
						}
					}
				};
		} else {
			return assetHandler;
		}
	}

	private NegotiableContent negotiableContent() {
		return
			NegotiableContent.
				newInstance().
					support(MediaTypes.of("text","html")).
					support(CharacterEncodings.of(StandardCharsets.UTF_8)).
					support(CharacterEncodings.of(StandardCharsets.ISO_8859_1)).
					support(CharacterEncodings.of(StandardCharsets.US_ASCII));
	}

	static DocumentationDeployer create(final DocumentationDeploymentFactory deploymentFactory,final DocumentationProviderFactory providerFactory) {
		return new DocumentationDeployer(deploymentFactory,providerFactory);
	}

}