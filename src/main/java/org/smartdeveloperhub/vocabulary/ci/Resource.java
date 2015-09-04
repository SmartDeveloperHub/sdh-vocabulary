/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.2.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.ci;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;

public abstract class Resource<T extends Resource<T>> {

	interface Assembler {

		void startIndividual(String id);

		void endIndividual();

		void addProperty(String property, Value... moreValues);

	}

	private final String id;

	private String title;
	private String created;

	private String location;

	private final Class<? extends T> clazz;

	Resource(String id, Class<? extends T> clazz) {
		this.id=id;
		this.clazz = clazz;
	}

	public String id() {
		return this.id;
	}

	public String title() {
		return this.title;
	}

	public String created() {
		return this.created;
	}

	public String location() {
		return this.location;
	}

	public T withTitle(String title) {
		this.title = title;
		return clazz.cast(this);
	}

	public T withCreated(String created) {
		this.created = created;
		return clazz.cast(this);
	}

	public T withLocation(String location) {
		this.location=location;
		return clazz.cast(this);
	}

	final String composePath(String base, Object child) {
		String tmp=base;
		if(!tmp.endsWith("/")) {
			tmp+="/";
		}
		tmp+=child.toString();
		return tmp;
	}

	abstract List<String> types();

	final String assemble(ValueFactory factory) {
		class LocalAssembler implements Resource.Assembler {

			private final StringBuilder builder;
			private boolean started;

			private LocalAssembler() {
				this.builder=
					new StringBuilder();
			}

			@Override
			public void startIndividual(String id) {
				this.builder.
					append("<").append(id).append(">").
					append(System.lineSeparator());
				this.started=true;
			}

			@Override
			public void endIndividual() {
				this.builder.append(" .").append(System.lineSeparator());
			}

			@Override
			public void addProperty(String property, Value... values) {
				if(values.length==0) {
					return;
				}
				if(!this.started) {
					this.builder.append(" ;").append(System.lineSeparator());
				} else {
					this.started=false;
				}
				this.builder.append("  ").append(property).append(" ");
				if(values.length==1) {
					this.builder.append(values[0].lexicalForm());
				} else {
					Iterator<Value> iterator = Iterators.forArray(values);
					do {
						this.builder.append(System.lineSeparator()).append("    ");
						this.builder.append(iterator.next().lexicalForm());
						if(iterator.hasNext()) {
							this.builder.append(",");
						}
					} while(iterator.hasNext());
				}
			}

			@Override
			public String toString() {
				return this.builder.toString();
			}

		}

		Assembler assembler=new LocalAssembler();
		assembler.startIndividual(this.id);
		assembleItem(assembler,factory);
		assembler.endIndividual();
		return assembler.toString();
	}

	void assembleItem(Resource.Assembler assembler, ValueFactory factory) {
		assembler.addProperty("a", factory.qualifiedNames(types()));
		assembler.addProperty("dcterms:identifier", factory.literal(this.id));
		assembler.addProperty("dcterms:title", factory.literal(this.title));
		assembler.addProperty("dcterms:created", factory.typedLiteral(this.created,"http://www.w3.org/2001/XMLSchema#dateTime"));
		assembler.addProperty("ci:location", factory.typedLiteral(location(),"http://www.w3.org/2001/XMLSchema#anyURI"));
	}

}