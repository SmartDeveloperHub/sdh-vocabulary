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

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

final class RDFXMLUtil {

	private static final DocumentBuilderFactory DOM_FACTORY;

	static {
		DOM_FACTORY=DocumentBuilderFactory.newInstance();
		DOM_FACTORY.setNamespaceAware(true);
	}

	private RDFXMLUtil() {
	}

	static boolean isStandaloneDocument(final String data) throws IOException {
		final Node root = rootNode(toDocument(data));
		String prefix=null;
		String localName=root.getNodeName();
		final String[] parts=root.getNodeName().split(":");
		if(parts.length==2) {
			prefix=parts[0];
			localName=parts[1];
		}
		final String namespace=root.lookupNamespaceURI(prefix);
		return
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespace) &&
			"RDF".equals(localName);
	}

	private static Node rootNode(final Document document) {
		final NodeList children = document.getChildNodes();
		for(int i=0;i<children.getLength();i++) {
			final Node child=children.item(i);
			if(child.getNodeType()==Node.ELEMENT_NODE) {
				return child;
			}
		}
		return null;
	}

	private static Document toDocument(final String data) throws IOException {
		try {
			final InputSource source = new InputSource(new StringReader(data));
			return DOM_FACTORY.newDocumentBuilder().parse(source);
		} catch (final Exception e) {
			throw new IOException("Could not unmarshall XML document",e);
		}
	}

}