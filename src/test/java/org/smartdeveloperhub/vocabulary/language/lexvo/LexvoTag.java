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
package org.smartdeveloperhub.vocabulary.language.lexvo;

import java.util.Objects;

import org.smartdeveloperhub.vocabulary.language.spi.Tag;

import com.google.common.collect.ComparisonChain;

final class LexvoTag implements Tag {

	private static final long serialVersionUID = -842475884873052522L;

	private static final String BASE_NAMESPACE_PARTS_1_3_5 = "http://lexvo.org/id/";
	private static final String BASE_NAMESPACE_PART_2      = "http://id.loc.gov/vocabulary/";

	private static final String ISO639_PREFIX="iso639-";

	private final String code;
	private final Part part;

	private LexvoTag(final Part part, final String value) {
		this.part=part;
		this.code=value;
	}

	@Override
	public String uri() {
		String base=BASE_NAMESPACE_PARTS_1_3_5;
		if(this.part.code()==2) {
			base=BASE_NAMESPACE_PART_2;
		}
		return base+ISO639_PREFIX+this.part.code()+"/"+this.code;
	}

	@Override
	public Part part() {
		return this.part;
	}

	@Override
	public String code() {
		return this.code;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.part,this.code,uri());
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof Tag) {
			final Tag that=(Tag)obj;
			result=
				this.part.equals(that.part()) &&
				this.code.equals(that.code()) &&
				uri().equals(that.uri());
		}
		return result;
	}

	@Override
	public String toString() {
		return "{ISO 639-"+this.part+"}:"+this.code;
	}

	@Override
	public int compareTo(final Tag that) {
		return
			ComparisonChain.
				start().
					compare(this.part,that.part()).
					compare(this.code,that.code()).
					compare(uri(),that.uri()).
					result();
	}

	static LexvoTag create(final Tag.Part part, final String value) {
		return new LexvoTag(part,value);
	}

}