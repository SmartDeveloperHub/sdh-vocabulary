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
package org.smartdeveloperhub.vocabulary.language.lexvo;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

final class Chronographer {

	interface Task<T,E extends Throwable> {
		T execute() throws E;
	}

	private final Stopwatch stopwatch;
	private final String taskName;
	private String startedMessage;
	private TimeUnit unit;

	Chronographer(final String taskName) {
		this.taskName = taskName;
		this.unit=TimeUnit.MILLISECONDS;
		this.startedMessage="";
		this.stopwatch=Stopwatch.createUnstarted();
	}

	Chronographer onStart(final String message, final Object... args) {
		this.startedMessage=String.format(message,args);
		return this;
	}

	Chronographer timeIn(final TimeUnit unit) {
		this.unit=unit;
		return this;
	}

	private static String toString(final TimeUnit unit) {
		switch(unit) {
		case NANOSECONDS:
			return "nanos";
		case MICROSECONDS:
			return "micros";
		case MILLISECONDS:
			return "millis";
		case SECONDS:
			return "seconds";
		case MINUTES:
			return "minutes";
		case HOURS:
			return "hours";
		default:
			return "days";
		}
	}

	<T,E extends Throwable> T time(final Task<T,E> task) throws E {
		final Logger logger=LoggerFactory.getLogger(task.getClass());
		boolean failed=true;
		try {
			logger.debug(">> Started "+this.taskName+this.startedMessage+" ...");
			this.stopwatch.start();
			final T doExecute = task.execute();
			failed=false;
			return doExecute;
		} finally {
			this.stopwatch.stop();
			String prefix=">> Completed ";
			if(failed) {
				prefix=">> Failed ";
			}
			logger.debug(prefix+this.taskName+" after {} {}",this.stopwatch.elapsed(this.unit),toString(this.unit));
		}
	}

}