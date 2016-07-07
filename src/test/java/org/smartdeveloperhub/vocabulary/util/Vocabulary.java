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
package org.smartdeveloperhub.vocabulary.util;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

class Vocabulary {

	static final class Builder {

		private final Namespace namespace;
		private final Multimap<Namespace,Version> versions;

		private Builder(final Namespace namespace) {
			this.versions = LinkedHashMultimap.create();
			this.namespace = namespace;
		}

		Builder addVersion(final Version version) {
			Preconditions.checkArgument(this.namespace.equals(version.vocabulary()));
			this.versions.put(version.namespace(),version);
			return this;
		}

		Vocabulary build() {
			return new Vocabulary(this.namespace,this.versions);
		}
	}

	private static class Dependencies {

		private final List<List<Namespace>> cycles=Lists.newArrayList();
		private final List<List<Namespace>> brokenPaths=Lists.newArrayList();
		private final List<List<Namespace>> fullPaths=Lists.newArrayList();

		private List<Namespace> currentPath;
		private Collection<Version> current;

		Dependencies() {
		}

		void addPath(final Deque<Version> versions) {
			this.current=versions;
			this.currentPath=currentPath();
			this.fullPaths.add(this.currentPath);
		}

		void breakAt(final Namespace namespace) {
			this.brokenPaths.add(buildPath(namespace));
		}

		Deque<Version> moveTo(final Version priorVersion) {
			final Deque<Version> next=Lists.newLinkedList();
			if(hasBeenTraversed(priorVersion.namespace())) {
				cyclePath(priorVersion.namespace());
			} else {
				next.addAll(this.current);
				next.addLast(priorVersion);
			}
			return next;
		}

		boolean hasCycles() {
			return !this.cycles.isEmpty();
		}

		List<List<Namespace>> cycles() {
			return this.cycles;
		}

		boolean hasBrokenPaths() {
			return !this.brokenPaths.isEmpty();
		}

		List<List<Namespace>> brokenPaths() {
			return this.brokenPaths;
		}

		private void cyclePath(final Namespace namespace) {
			this.cycles.add(buildPath(namespace));
		}

		private List<Namespace> currentPath() {
			final List<Namespace> currentPath=Lists.newArrayList();
			for(final Version v:this.current) {
				currentPath.add(v.namespace());
			}
			return currentPath;
		}

		private ImmutableList<Namespace> buildPath(final Namespace namespace) {
			return ImmutableList.
				<Namespace>builder().
					addAll(this.currentPath).
					add(namespace).
					build();
		}

		private boolean hasBeenTraversed(final Namespace namespace) {
			return this.currentPath.contains(namespace);
		}

	}

	private final Namespace namespace;
	private final Multimap<Namespace, Version> versions;

	private Vocabulary(final Namespace namespace, final Multimap<Namespace,Version> versions) {
		this.namespace = namespace;
		this.versions = versions;
	}

	private Dependencies priorVersionDependencies(final Vocabularies vocabularies) {
		final Dependencies map=new Dependencies();
		for(final Version version:this.versions.values()) {
			final Deque<Deque<Version>> paths = bootstrap(version);
			while(!paths.isEmpty()) {
				final Deque<Version> current = paths.pollFirst();
				final Version head=current.peekLast();
				map.addPath(current);
				for(final Namespace priorVersionNS:head.priorVersions()) {
					final Version priorVersion=vocabularies.resolveVersion(priorVersionNS);
					if(priorVersion==null) {
						map.breakAt(priorVersionNS);
					} else {
						final Deque<Version> next = map.moveTo(priorVersion);
						if(!next.isEmpty()) {
							paths.addLast(next);
						}
					}
				}
			}
		}
		return map;
	}

	private Dependencies importDependencies(final Vocabularies vocabularies) {
		final Dependencies map=new Dependencies();
		for(final Version version:this.versions.values()) {
			final Deque<Deque<Version>> paths = bootstrap(version);
			while(!paths.isEmpty()) {
				final Deque<Version> current = paths.pollFirst();
				final Version head=current.peekLast();
				map.addPath(current);
				for(final Namespace dependencyNS:head.imports()) {
					final Version dependency=vocabularies.resolveVersion(dependencyNS);
					if(dependency==null) {
						map.breakAt(dependencyNS);
					} else {
						final Deque<Version> next = map.moveTo(dependency);
						if(!next.isEmpty()) {
							paths.addLast(next);
						}
					}
				}
			}
		}
		return map;
	}

	private Deque<Deque<Version>> bootstrap(final Version version) {
		final Deque<Deque<Version>> paths=Lists.newLinkedList();
		final Deque<Version> path = Lists.newLinkedList();
		path.add(version);
		paths.add(path);
		return paths;
	}

	private String missingPriorVersionFailure(final Vocabularies vocabularies, final Version version,final Namespace priorVersion) {
		final Version referredVersion=vocabularies.resolveVersion(priorVersion);
		String failure;
		if(referredVersion==null) {
			failure=
				String.format(
					"Unknown prior version %s for module %s",
					priorVersion.canonicalForm(),
					version.module().location());
		} else {
			failure=
				String.format(
					"Module %s (%s --> %s) has prior version with different canonical namespace (%s --> %s)",
					version.module().location(),
					version.namespace(),
					version.vocabulary(),
					referredVersion.namespace(),
					referredVersion.vocabulary());
		}
		return failure;
	}

	private void checkDependencies(final List<String> failures, final Dependencies map, final String tag) {
		if(map.hasCycles()) {
			for(final List<Namespace> cycle:map.cycles()) {
				failures.add(String.format("Found %s cycle: %s",tag,Joiner.on(" --> ").join(cycle)));
			}
		}
		if(map.hasBrokenPaths()) {
			for(final List<Namespace> cycle:map.brokenPaths()) {
				failures.add(String.format("Found broken %s path: %s",tag,Joiner.on(" --> ").join(cycle)));
			}
		}
	}

	List<String> validate(final Vocabularies vocabularies) {
		final List<String> failures=Lists.newArrayList();
		// Check that all prior versions are available
		for(final Version version:this.versions.values()) {
			for(final Namespace priorVersion:version.priorVersions()) {
				if(!this.versions.containsKey(priorVersion)) {
					failures.add(missingPriorVersionFailure(vocabularies, version, priorVersion));
				}
			}
		}
		// Check that there are no version cycles nor versioning gaps
		checkDependencies(failures, priorVersionDependencies(vocabularies), "versioning");
		// Check that there are no import cycles nor missing imports
		checkDependencies(failures, importDependencies(vocabularies), "import");
		return failures;
	}

	Namespace namespace() {
		return this.namespace;
	}

	Set<Namespace> versions() {
		return ImmutableSet.copyOf(this.versions.keySet());
	}

	Collection<Version> versions(final Namespace target) {
		return this.versions.get(target);
	}

	Version version(final Namespace target) {
		return Iterables.getFirst(this.versions.get(target),null);
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		builder.append("Vocabulary {").append(System.lineSeparator());
		builder.append(" - Ontology IRI: ").append(this.namespace).append(System.lineSeparator());
		builder.append(" - Versions {").append(System.lineSeparator());
		for(final Version version:this.versions.values()) {
			builder.
				append("   + ").
				append(StringUtil.indent(version.toString(),"     ")).
				append(System.lineSeparator());
		}
		builder.append("   }").append(System.lineSeparator());
		builder.append("}");
		return builder.toString();
	}

	static Vocabulary.Builder create(final Namespace namespace) {
		return new Builder(namespace);
	}

}