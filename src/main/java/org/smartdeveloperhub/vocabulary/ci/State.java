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

import java.util.EnumSet;

public enum State {
	IN_PROGRESS("inProgress","Running",Verdict.UNAVAILABLE),
	CANCELED("canceled","Aborted",Verdict.UNAVAILABLE),
	COMPLETE("complete","Finished",Verdict.PASSED,Verdict.FAILED,Verdict.WARNING),
	;

	private final String[] types;
	private final String resourceName;
	private final EnumSet<Verdict> compatibleVerdicts;
	final Verdict preferredVerdict;

	private State(String name, String type,Verdict preferredVerdict, Verdict... otherCompatibleVerdicts) {
		this.resourceName = "oslc_auto:"+name;
		this.types=new String[] {
			"oslc_auto:AutomationRequest",
			"ci:Execution",
			"ci:"+type+"Execution"
		};
		this.preferredVerdict = preferredVerdict;
		this.compatibleVerdicts=EnumSet.of(preferredVerdict, otherCompatibleVerdicts);
	}

	String resourceName() {
		return this.resourceName;
	}

	String[] types() {
		return this.types;
	}

	Verdict preferredVerdict() {
		return this.preferredVerdict;
	}

	boolean isCompatible(Verdict verdict) {
		return this.compatibleVerdicts.contains(verdict);
	}

}