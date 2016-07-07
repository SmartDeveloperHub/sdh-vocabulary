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
package org.smartdeveloperhub.vocabulary.language.spi;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public final class Language {

	public static final class Builder {

		private final Set<Tag> tags;
		private final Map<String,String> localizedNames;
		private int id;

		private Builder() {
			this.tags=Sets.newHashSet();
			this.localizedNames=Maps.newHashMap();
		}

		public Builder withId(final int id) {
			this.id = id;
			return this;
		}

		public Builder withTags(final Tag... tags) {
			return withTags(Sets.newHashSet(tags));
		}

		public Builder withTags(final Iterable<Tag> tags) {
			Iterables.addAll(this.tags,tags);
			return this;
		}

		public Builder withLocalizedName(final String locale, final String name) {
			this.localizedNames.put(locale, name);
			return this;
		}

		public Builder withLocalizedNames(final Map<String,String> localizedNames) {
			this.localizedNames.putAll(localizedNames);
			return this;
		}

		public Language build() {
			return
				new Language(
					Objects.requireNonNull(this.id),
					this.tags,
					this.localizedNames);
		}

	}

	private final int id;
	private final ImmutableSortedSet<Tag> tags;
	private final ImmutableSortedMap<String, String> localizedNames;

	private Language(final int id, final Set<Tag> tags, final Map<String, String> localizations) {
		this.id=id;
		this.tags=ImmutableSortedSet.copyOf(tags);
		this.localizedNames=ImmutableSortedMap.copyOf(localizations);
	}

	public int id() {
		return this.id;
	}

	public Set<Tag> tags() {
		return this.tags;
	}

	public Map<String,String> localizedNames() {
		return this.localizedNames;
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		builder.append("Language(").append(this.id).append(") {").append(System.lineSeparator());
		builder.append("  Tags {").append(System.lineSeparator());
		for(final Tag tag:this.tags) {
			builder.append("    - ").append(tag).append(" : ").append(tag.uri()).append(System.lineSeparator());
		}
		builder.append("  }").append(System.lineSeparator());
		builder.append("  Localizations {").append(System.lineSeparator());
		for(final Entry<String, String> entry:this.localizedNames.entrySet()) {
			builder.append("    - ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(System.lineSeparator());
		}
		builder.append("  }").append(System.lineSeparator());
		builder.append("}");
		return builder.toString();
	}

	public static Builder builder() {
		return new Builder();
	}

}