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
package org.smartdeveloperhub.vocabulary.language;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.DateTime;
import org.smartdeveloperhub.vocabulary.language.spi.Language;
import org.smartdeveloperhub.vocabulary.language.spi.LanguageDataSource;
import org.smartdeveloperhub.vocabulary.language.spi.Tag;
import org.smartdeveloperhub.vocabulary.language.spi.Tag.Part;
import org.smartdeveloperhub.vocabulary.language.util.LocalizedProperties;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableSortedSet.Builder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

final class LanguagePack {

	private static final String HEADER_VERSION        = ".h.p.v";
	private static final String HEADER_GENERATED      = ".h.p.d";
	private static final String HEADER_SOURCE_NAME    = ".h.s.n";
	private static final String HEADER_SOURCE_VERSION = ".h.s.v";
	private static final String HEADER_SOURCE_SHA512  = ".h.s.s512";
	private static final String HEADER_SOURCE_SHA256  = ".h.s.s256";

	private static final String HEADER_LANG           = ".l";

	private final LocalizedProperties properties;

	private static final String PACK_VERSION="1.0";

	private LanguagePack(final LocalizedProperties properties){
		this.properties = properties;
	}

	private String getProperty(final String property, final String message) {
		final String value = this.properties.getProperty(property);
		if(value==null) {
			throw new CorruptedLanguagePackException(message);
		}
		return value;
	}

	DateTime generated() {
		final String property = getProperty(HEADER_GENERATED,"Could not find generation date");
		try {
			return DateTime.parse(property);
		} catch (final IllegalArgumentException e) {
			throw new CorruptedLanguagePackException("Invalid generation date ("+property+")",e);
		}
	}

	String name() {
		return getProperty(HEADER_SOURCE_NAME,"Could not find source name");
	}

	String version() {
		return getProperty(HEADER_SOURCE_VERSION,"Could not find source version");
	}

	String sha256() {
		return getProperty(HEADER_SOURCE_SHA256,"Could not find source SHA-256 digest");
	}

	String sha512() {
		return getProperty(HEADER_SOURCE_SHA512,"Could not find source SHA-256 digest");
	}

	int size() {
		final String value = getProperty(HEADER_LANG,"Could not find number of languages");
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			throw new CorruptedLanguagePackException("Invalid language number ("+value+")",e);
		}
	}

	Set<String> tagUris(final String tag) {
		final Optional<Integer> langId = tagLanguage(tag);
		if(!langId.isPresent()) {
			return Collections.emptySet();
		}
		final Set<Part> tagParts = tagParts(tag);
		if(tagParts.isEmpty()) {
			throw new CorruptedLanguagePackException("Missing parts for tag '"+tag+"'");
		}
		final Builder<String> builder = ImmutableSortedSet.<String>naturalOrder();
		for(final Tag.Part part:tagParts) {
			builder.add(getProperty(languagePartUriProperty(langId.get(), part),"Coult not find uri for tag '"+tag+"' of ISO 639-"+part));
		}
		return builder.build();
	}

	Set<Tag.Part> tagParts(final String tag) {
		final String property = this.properties.getProperty(tagPartsProperty(tag));
		if(property==null) {
			return Collections.emptySet();
		}
		final Builder<Tag.Part> builder = ImmutableSortedSet.<Tag.Part>naturalOrder();
		for(final String rawPart:Splitter.on(',').splitToList(property)) {
			builder.add(part(rawPart));
		}
		return builder.build();
	}

	Optional<Integer> tagLanguage(final String tag) {
		final String value=this.properties.getProperty(tagLanguageProperty(tag));
		try {
			if(value==null) {
				return Optional.absent();
			}
			return Optional.of(Integer.parseInt(value));
		} catch (final NumberFormatException e) {
			throw new CorruptedLanguagePackException("Invalid language identifier ("+value+")",e);
		}
	}

	String languageName(final int langId, final String locale) {
		return this.properties.getProperty(localizedLanguageNameProperty(langId,locale));
	}

	String languageTag(final int langId) {
		return this.properties.getProperty(languageTagProperty(langId));
	}

	String languageUri(final int langId) {
		return this.properties.getProperty(languageUriProperty(langId));
	}

	Set<Tag.Part> languageParts(final int langId) {
		final String property = this.properties.getProperty(languagePartsProperty(langId));
		if(property==null) {
			return Collections.emptySet();
		}
		final Builder<Tag.Part> builder = ImmutableSortedSet.<Tag.Part>naturalOrder();
		for(final String rawPart:Splitter.on(',').splitToList(property)) {
			builder.add(part(rawPart));
		}
		return builder.build();
	}

	String languageTag(final int langId, final Tag.Part part) {
		return this.properties.getProperty(languagePartTagProperty(langId,part));
	}

	String languageUri(final int langId, final Tag.Part part) {
		return this.properties.getProperty(languagePartUriProperty(langId,part));
	}

	LocalizedProperties export() {
		return new LocalizedProperties(this.properties);
	}

	static LanguagePack fromExport(final LocalizedProperties export) {
		return new LanguagePack(new LocalizedProperties(export));
	}

	static LanguagePack fromSource(final LanguageDataSource source) {
		return new LanguagePack(pack(source));
	}

	private static Tag.Part part(final String partId) {
		for(final Tag.Part part:Tag.Part.values()) {
			if(partId.equals(part.toString())) {
				return part;
			}
		}
		throw new CorruptedLanguagePackException("Invalid part identifier '"+partId+"'");
	}

	private static String tagLanguageProperty(final String tag) {
		return tag;
	}

	private static String tagPartsProperty(final String tag) {
		return tag+".p";
	}

	private static String langKey(final int langId) {
		return String.format("%08x",langId);
	}

	private static String localizedLanguageNameProperty(final int langId, final String locale) {
		return ".l."+langKey(langId)+".n."+locale;
	}

	private static String languageNameProperty(final int langId) {
		return ".l."+langKey(langId)+".d.n";
	}

	private static String languageTagProperty(final int langId) {
		return ".l."+langKey(langId)+".d.t";
	}

	private static String languageUriProperty(final int langId) {
		return ".l."+langKey(langId)+".d.u";
	}

	private static String languagePartsProperty(final int langId) {
		return ".l."+langKey(langId)+".p";
	}

	private static String languagePartTagProperty(final int langId, final Tag.Part part) {
		return ".l."+langKey(langId)+".p."+part+".t";
	}

	private static String languagePartUriProperty(final int langId, final Tag.Part part) {
		return ".l."+langKey(langId)+".p."+part+".u";
	}

	private static LocalizedProperties pack(final LanguageDataSource support) {
		final LocalizedProperties ps=new LocalizedProperties();
		final List<Language> languages = support.languages();
		for(final Language tag:languages) {
			ps.putAll(toProperties(tag));
		}
		ps.setProperty(HEADER_VERSION,PACK_VERSION);
		ps.setProperty(HEADER_GENERATED,new DateTime().toString());
		ps.setProperty(HEADER_SOURCE_NAME,support.name());
		ps.setProperty(HEADER_SOURCE_VERSION,support.version());
		ps.setProperty(HEADER_SOURCE_SHA256,support.sha256());
		ps.setProperty(HEADER_SOURCE_SHA512,support.sha512());
		ps.setProperty(HEADER_LANG,Integer.toString(languages.size()));
		return ps;
	}

	private static Map<String,String> toProperties(final Language info) {
		final Map<String,String> result=Maps.newLinkedHashMap();
		final StringBuilder builder=new StringBuilder();
		boolean first=true;
		final Set<Tag> tags = info.tags();
		for(final Tag tag:tags) {
			result.put(tagLanguageProperty(tag.code()),Integer.toString(info.id()));
			result.put(tagPartsProperty(tag.code()),partIds(tag,tags));
			result.put(languagePartUriProperty(info.id(),tag.part()),tag.uri());
			result.put(languagePartTagProperty(info.id(),tag.part()),tag.code());
			if(first) {
				result.put(languageUriProperty(info.id()),tag.uri());
				result.put(languageTagProperty(info.id()),tag.code());
				String value = info.localizedNames().get(tag.code());
				if(value==null) {
					value = info.localizedNames().get("en");
				}
				if(value==null) {
					value = info.localizedNames().get("fr");
				}
				if(value==null) {
					value = info.localizedNames().get("de");
				}
				if(value!=null) {
					result.put(languageNameProperty(info.id()),value);
				}
				first=false;
			} else {
				builder.append(",");
			}
			builder.append(tag.part());
		}
		if(!tags.isEmpty()) {
			result.put(languagePartsProperty(info.id()),builder.toString());
		}
		for(final Entry<String,String> entry:info.localizedNames().entrySet()) {
			result.put(localizedLanguageNameProperty(info.id(),entry.getKey()),entry.getValue());
		}
		return result;
	}

	private static String partIds(final Tag tag, final Set<Tag> tags) {
		final Set<Part> partIds=Sets.newTreeSet();
		for(final Tag other:tags) {
			if(tag.code().equals(other.code())) {
				partIds.add(other.part());
			}
		}
		return Joiner.on(',').join(partIds);
	}

}