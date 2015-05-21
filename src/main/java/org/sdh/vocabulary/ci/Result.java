/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://smartdeveloperhub.github.io/
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
 *   Artifact    : org.sdh.vocabulary:sdh-vocabulary:1.0.0-SNAPSHOT
 *   Bundle      : sdh-vocabulary-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.sdh.vocabulary.ci;

import java.util.List;

import com.google.common.collect.ImmutableList;

public final class Result extends Resource<Result> {

	private final Execution execution;
	private final Verdict verdict;

	Result(Execution execution, Verdict verdict) {
		super(execution.id()+"/result",Result.class);
		this.execution = execution;
		this.verdict = verdict;
		withTitle("Result of "+execution.title());
	}

	@Override
	List<String> types() {
		return ImmutableList.copyOf(this.verdict.types());
	}

	public Execution execution() {
		return this.execution;
	}

	public Verdict verdict() {
		return this.verdict;
	}

	@Override
	void assembleItem(Assembler assembler, ValueFactory factory) {
		super.assembleItem(assembler, factory);
		assembler.addProperty("oslc_auto:state", factory.qualifiedName(this.execution.state().resourceName()));
		assembler.addProperty("oslc_auto:verdict", factory.qualifiedName(this.verdict.resourceName()));
		assembler.addProperty("oslc_auto:producedByAutomationRequest", factory.relativeUri(this.execution.id()));
		assembler.addProperty("oslc_auto:reportsOnAutomationPlan", factory.relativeUri(this.execution.build().id()));
	}

}