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
package org.smartdeveloperhub.vocabulary.publisher.handlers;

import java.util.List;

import org.ldp4j.http.CharacterEncoding;
import org.ldp4j.http.Language;
import org.ldp4j.http.MediaType;
import org.ldp4j.http.MediaTypes;
import org.ldp4j.http.Negotiable;
import org.ldp4j.http.Variant;

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

	boolean hasTypes() {
		return !this.mediaTypes.isEmpty();
	}

	boolean isAcceptable(final Variant variant) {
		final MediaType type=variant.type();
		if(type!=null) {
			for(final MediaType supported:this.mediaTypes) {
				if(MediaTypes.includes(supported,type)) {
					return true;
				}
			}
		}
		return false;
	}

	NegotiableContent merge(final NegotiableContent that) {
		final NegotiableContent result=new NegotiableContent();
		combine(result.mediaTypes,this.mediaTypes,that.mediaTypes);
		combine(result.characterEncodings,this.characterEncodings,that.characterEncodings);
		combine(result.languages,this.languages,that.languages);
		return result;
	}

	@SafeVarargs
	private static <T extends Negotiable> void combine(final List<T> target, final List<T>... sources) {
		for(final List<T> source:sources) {
			target.addAll(source);
		}
	}

	List<MediaType> clashingMediaTypes(final NegotiableContent that) {
		final List<MediaType> result=Lists.newArrayList(this.mediaTypes);
		result.retainAll(that.mediaTypes);
		return result;
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

	private <T extends Negotiable> NegotiableContent addNegotiable(final T negotiable, final List<? super T> collection) {
		if(negotiable!=null && !collection.contains(negotiable)) {
			collection.add(negotiable);
		}
		return this;
	}

	public static NegotiableContent newInstance() {
		return new NegotiableContent();
	}

}