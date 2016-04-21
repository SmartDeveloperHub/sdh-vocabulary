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
package org.smartdeveloperhub.vocabulary.publisher.handlers;

import java.util.List;

import org.ldp4j.http.CharacterEncoding;
import org.ldp4j.http.Language;
import org.ldp4j.http.MediaType;
import org.ldp4j.http.Negotiable;

import com.google.common.collect.Lists;

public final class NegotiableContent {

	private final List<MediaType> mediaTypes;
	private final List<CharacterEncoding> characterEncodings;
	private final List<Language> languages;

	private NegotiableContent() {
		this.mediaTypes=Lists.newArrayList();
		this.characterEncodings=Lists.newArrayList();
		this.languages=Lists.newArrayList();
	}

	public NegotiableContent support(final MediaType mediaType) {
		return addNegotiable(mediaType, this.mediaTypes);
	}

	public NegotiableContent support(final CharacterEncoding characterEncoding) {
		return addNegotiable(characterEncoding,this.characterEncodings);
	}

	public NegotiableContent support(final Language language) {
		return addNegotiable(language,this.languages);
	}

	private <T extends Negotiable> NegotiableContent addNegotiable(final T mediaType, final List<? super T> collection) {
		if(mediaType!=null) {
			collection.add(mediaType);
		}
		return this;
	}

	public static NegotiableContent newInstance() {
		return new NegotiableContent();
	}

	List<MediaType> mediaTypes() {
		return this.mediaTypes;
	}

	List<CharacterEncoding> characterEncodings() {
		return this.characterEncodings;
	}

	List<Language> languages() {
		return this.languages;
	}

}