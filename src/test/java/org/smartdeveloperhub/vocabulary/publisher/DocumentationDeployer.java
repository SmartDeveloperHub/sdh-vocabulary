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

import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.contentNegotiation;
import static org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers.methodController;

import java.nio.charset.StandardCharsets;

import org.ldp4j.http.CharacterEncodings;
import org.ldp4j.http.MediaTypes;
import org.smartdeveloperhub.vocabulary.publisher.handlers.MoreHandlers;
import org.smartdeveloperhub.vocabulary.publisher.handlers.NegotiableContent;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeployment;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationDeploymentFactory;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationProvider;
import org.smartdeveloperhub.vocabulary.publisher.spi.DocumentationProviderFactory;
import org.smartdeveloperhub.vocabulary.util.Catalog;
import org.smartdeveloperhub.vocabulary.util.Module;

import io.undertow.server.handlers.PathHandler;
import io.undertow.util.Methods;

final class DocumentationDeployer {

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
			System.out.printf("- Documentation (%s):%n",module.implementationIRI());
			System.out.printf("  + Root........: %s --> %s (%s)%n",deployment.implementationRoot(),deployment.implementationRoot().getPath(),provider.assetsPath());
			System.out.printf("  + Landing page: %s --> %s%n",deployment.implementationLandingPage(),deployment.implementationLandingPage().getPath());
			if(module.isVersion()) {
				System.out.printf("  + Redirection.: %s --> %s%n",deployment.canonicalLandingPage(),deployment.canonicalLandingPage().getPath());
			}
			pathHandler.
				addExactPath(
					deployment.implementationLandingPage().getPath(),
					methodController(
						contentNegotiation(
							new ModuleLandingPage(deployment,provider),
							NegotiableContent.
							newInstance().
								support(MediaTypes.of("text","html")).
								support(CharacterEncodings.of(StandardCharsets.UTF_8)).
								support(CharacterEncodings.of(StandardCharsets.ISO_8859_1)).
								support(CharacterEncodings.of(StandardCharsets.US_ASCII)))
						).
						allow(Methods.GET)
				).
				addPrefixPath(
					deployment.implementationRoot().getPath(),
					new ExternalAssetProvider(provider.assetsPath())
				);
			if(module.isVersion()) {
				pathHandler.
					addExactPath(
						deployment.canonicalLandingPage().getPath(),
						methodController(MoreHandlers.temporaryRedirect(deployment.implementationLandingPage())).allow(Methods.GET)
					);
			}
		}

	}

	static DocumentationDeployer create(final DocumentationDeploymentFactory deploymentFactory,final DocumentationProviderFactory providerFactory) {
		return new DocumentationDeployer(deploymentFactory,providerFactory);
	}

}