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
 *   Artifact    : org.smartdeveloperhub.vocabulary:sdh-vocabulary:0.1.0
 *   Bundle      : sdh-vocabulary-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.vocabulary.ci;

import java.util.List;

import com.google.common.collect.ImmutableList;

public final class Execution extends Resource<Execution> {

	private final Build build;

	private State state;
	private String finished;
	private Result result;

	private long executionNumber;

	Execution(Build build, long id) {
		super(build.id()+"/"+id,Execution.class);
		this.build = build;
		this.executionNumber = id;
		withTitle("Execution "+id+" of '"+build.title()+"'");
		inProgress();
	}

	@Override
	List<String> types() {
		return ImmutableList.copyOf(this.state.types());
	}

	private void setState(State state, Verdict verdict, String finished) {
		this.state=state;
		this.finished=finished;
		this.result=
			new Result(this,verdict).
				withCreated(created());
	}

	private String resultLocation() {
		return composePath(location(),"result");
	}

	public Build build() {
		return this.build;
	}

	public State state() {
		return this.state;
	}

	public String finished() {
		return this.finished;
	}

	public Result result() {
		return this.result.withLocation(resultLocation());
	}

	public String location() {
		String result = super.location();
		if(result==null) {
			result=composePath(this.build.location(),this.executionNumber);
		}
		return result;
	}

	public Execution failed(String finished) {
		setState(State.COMPLETE, Verdict.FAILED, finished);
		return this;
	}

	public Execution passed(String finished) {
		setState(State.COMPLETE, Verdict.PASSED, finished);
		return this;
	}

	public Execution warning(String finished) {
		setState(State.COMPLETE, Verdict.WARNING, finished);
		return this;
	}

	public Execution  inProgress() {
		setState(State.IN_PROGRESS, Verdict.UNAVAILABLE,null);
		return this;
	}

	public Execution canceled() {
		setState(State.CANCELED, Verdict.UNAVAILABLE,null);
		return this;
	}

	@Override
	void assembleItem(Assembler assembler, ValueFactory factory) {
		super.assembleItem(assembler, factory);
		assembler.addProperty("oslc_auto:state", factory.qualifiedName(this.state.resourceName()));
		assembler.addProperty("oslc_auto:executesAutomationPlan", factory.relativeUri(this.build.id()));
		if(finished!=null) {
			assembler.addProperty("ci:finished", factory.typedLiteral(this.finished,"http://www.w3.org/2001/XMLSchema#dateTime"));
		}
		assembler.addProperty("ci:hasResult", factory.relativeUri(this.result.id()));
	}

}