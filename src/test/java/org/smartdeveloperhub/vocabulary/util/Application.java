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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.collect.Maps;

public final class Application {

	public static boolean logContext(final String... args) {
		final Calendar calendar=Calendar.getInstance();
		final File file=
			new File(
				String.format(
					"error.%04d%02d%02d.%02d%02d.%03d.log",
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH)+1,
					calendar.get(Calendar.DAY_OF_MONTH),
					calendar.get(Calendar.HOUR_OF_DAY),
					calendar.get(Calendar.MINUTE),
					calendar.get(Calendar.MILLISECOND)));
		boolean written=true;
		try(FileOutputStream fos=new FileOutputStream(file)) {
			final PrintStream out = new PrintStream(fos);
			logContext(out, args);
			out.flush();
		} catch (final FileNotFoundException e) {
			written=false;
		} catch (final IOException e) {
			written=false;
		}
		return written;
	}

	public static void logContext(final PrintStream out, final String... args) {
		out.printf("PID: %s%n",getSunVMProcessId(getOracleCompatibleVMProcessId("<UNKNOWN>")));
		out.printf("Program arguments:%n");
		for(final String argument:args) {
			out.printf(" [%s]%n",argument);
		}
		final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		final List<String> inputArguments = bean.getInputArguments();
		if(!inputArguments.isEmpty()) {
			out.printf("VM arguments:%n");
			for(final String argument:inputArguments) {
				out.printf(" %s%n",argument);
			}
		}
		out.printf("Environment properties:%n");
		for(final Entry<String,String> entry:Application.sort(System.getenv()).entrySet()) {
			out.printf(" - %s : %s%n",entry.getKey(),entry.getValue());
		}
		out.printf("System properties:%n");
		for(final Entry<String,String> entry:Application.sort(System.getProperties()).entrySet()) {
			out.printf(" - %s : %s%n",entry.getKey(),entry.getValue());
		}
	}

	private static String getSunVMProcessId(final String fallback) {
		final RuntimeMXBean runtime=ManagementFactory.getRuntimeMXBean();
		try {
			final Field jvm = runtime.getClass().getDeclaredField("jvm");
			if(!jvm.getType().getCanonicalName().equals("sun.management.VMManagement")) {
				return fallback;
			}
			jvm.setAccessible(true);
			final Object mgmt = jvm.get(runtime);
			try {
				final Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
				pid_method.setAccessible(true);
				final int pid = (Integer) pid_method.invoke(mgmt);
				return Integer.toString(pid);
			} catch (final Exception e) {
				return fallback;
			}
		} catch (final Exception e) {
			return fallback;
		}
	}

	private static String getOracleCompatibleVMProcessId(final String fallback) {
		// something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		final int index = jvmName.indexOf('@');

		if (index < 1) {
			// part before '@' empty (index = 0) / '@' not found (index = -1)
			return fallback;
		}

		try {
			return Long.toString(Long.parseLong(jvmName.substring(0, index)));
		} catch (final NumberFormatException e) {
			// ignore
		}
		return fallback;
	}

	private static <T extends Map<?,?>> TreeMap<String, String> sort(final T properties) {
		final TreeMap<String,String> orderedProperties = Maps.newTreeMap();
		for(final Entry<?, ?> entry:properties.entrySet()) {
			orderedProperties.put(entry.getKey().toString(), entry.getValue().toString());
		}
		return orderedProperties;
	}

}
