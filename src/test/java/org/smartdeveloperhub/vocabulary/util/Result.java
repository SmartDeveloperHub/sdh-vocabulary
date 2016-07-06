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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public final class Result<E> {

	private final List<String> errors;
	private final List<String> warnings;
	private E value;

	private Result() {
		this.errors=Lists.newArrayList();
		this.warnings=Lists.newArrayList();
	}

	Result<E> value(final E value) {
		this.value = value;
		return this;
	}

	Result<E> warn(final String format, final Object... args) {
		this.warnings.add(String.format(format,args));
		return this;
	}

	Result<E> error(final String format, final Object... args) {
		this.errors.add(String.format(format,args));
		return this;
	}

	public boolean isAvailable() {
		return this.value!=null;
	}

	public List<String> errors() {
		return Collections.unmodifiableList(this.errors);
	}

	public List<String> warnings() {
		return Collections.unmodifiableList(this.warnings);
	}

	public E get() {
		return this.value;
	}

	@Override
	public String toString() {
		final StringBuilder builder=new StringBuilder();
		toString(builder,this.errors, "errors");
		toString(builder,this.warnings, "warnings");
		return builder.toString();
	}

	private void toString(final StringBuilder builder, final List<String> messages, final String title) {
		if(messages.isEmpty()) {
			builder.append("No ").append(title).append(" found.").append(System.lineSeparator());
		} else {
			builder.append(messages.size()).append(" ").append(title).append(" found.").append(System.lineSeparator());
			for(final String message:messages) {
				builder.append("- ").append(message).append(System.lineSeparator());
			}
		}
	}

	static <E> Result<E> newInstance() {
		return new Result<E>();
	}

}