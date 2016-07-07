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
package org.smartdeveloperhub.vocabulary.ci;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.smartdeveloperhub.vocabulary.ci.DataSet;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;

public class VocabularyTest {

	static class Execution {

		private String id;
		private DateTime finished;
		private Build build;

		private Execution(Build build, String id, DateTime finished) {
			this.build = build;
			this.id = id;
			this.finished = finished;
		}

		Build build() {
			return this.build;
		}

		String id() {
			return this.id;
		}

		DateTime finished() {
			return this.finished;
		}

	}

	static class BrokenPeriod {

		private final Build build;
		private final Execution failedExecution;
		private final Execution passedExecution;

		public BrokenPeriod(Build build, Execution failed, Execution passed) {
			this.build = build;
			this.failedExecution = failed;
			this.passedExecution = passed;
		}

		Build build() {
			return build;
		}

		Execution failedExecution() {
			return this.failedExecution;
		}

		Execution passedExecution() {
			return this.passedExecution;
		}

		Duration duration() {
			return new Duration(this.failedExecution.finished(), this.passedExecution.finished());
		}

	}

	static class Build {

		private final List<BrokenPeriod> periods;
		private final String id;

		private Build(String id) {
			this.id=id;
			this.periods=Lists.newArrayList();
		}

		String id() {
			return this.id;
		}

		Execution createExecution(String id, DateTime finished) {
			return new Execution(this,id,finished);
		}

		BrokenPeriod addBrokenPeriod(Execution failed, Execution passed) {
			BrokenPeriod brokenPeriod = new BrokenPeriod(this,failed,passed);
			this.periods.add(brokenPeriod);
			return brokenPeriod;
		}

		Duration averageBrokenTime() {
			Duration d=new Duration(0);
			if(this.periods.size()>0) {
				for(BrokenPeriod p:this.periods) {
					d=d.plus(p.duration());
				}
				d=d.dividedBy(this.periods.size());
			}
			return d;
		}

	}

	private PeriodFormatter formatter;

	private String loadResource(String resourceName) {
		try {
			InputStream resourceAsStream = getClass().getResourceAsStream(resourceName);
			if(resourceAsStream==null) {
				throw new AssertionError("Could not find resource '"+resourceName+"'");
			}
			return IOUtils.toString(resourceAsStream, Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new AssertionError("Could not load resource '"+resourceName+"'");
		}
	}

	private Model buildsDataSet() {
		String base = "http://www.smartdeveloperhub.org/vocabulary/sdh/v1/";
		String language = "TURTLE";
		DataSet dataSet =
			DataSet.
				create(base).
				withLocation("http://www.examples.org/builds/").
				withBuild("b1").
					withTitle("Build 1").
					withCreated("2015-04-28T09:00:00Z").
					dataSet().
				withBuild("b2").
					withTitle("Build 2").
					withCreated("2015-04-29T09:00:00Z").
					dataSet().
				withBuild("b3").
					withTitle("Build 3").
					withCreated("2015-04-30T09:00:00Z").
					dataSet().
				withBuild("b4").
					withTitle("Build 4").
					withCreated("2015-05-01T09:00:00Z").
					dataSet();
		return
			ModelFactory.
				createDefaultModel().
					read(
						new StringReader(dataSet.assemble()),
						null,
						language);
	}

	private Model executionsDataSet() {
		String base = "http://www.smartdeveloperhub.org/vocabulary/sdh/v1/";
		String language = "TURTLE";
		DataSet dataSet =
			DataSet.
				create(base).
				withLocation("http://www.examples.org/builds/").
				withBuild("b1").
					withTitle("Build 1").
					withCreated("2015-04-28T09:00:00Z").
					addExecution("2015-04-28T06:30:00Z").
						failed("2015-04-28T07:00:00Z").
						build().
					dataSet().
				withBuild("b2").
					withTitle("Build 2").
					withCreated("2015-04-29T09:00:00Z").
					addExecution("2015-04-29T06:30:00Z").
						failed("2015-04-29T07:00:00Z").
						build().
					addExecution("2015-04-29T09:30:00Z").
						passed("2015-04-29T10:15:00Z").
						build().
					dataSet().
				withBuild("b3").
					withTitle("Build 3").
					withCreated("2015-04-30T09:00:00Z").
					addExecution("2015-04-30T06:30:00Z").
						failed("2015-04-30T07:00:00Z").
						build().
					addExecution("2015-04-30T09:30:00Z").
						passed("2015-04-30T10:15:00Z").
						build().
					addExecution("2015-04-30T11:30:00Z").
						passed("2015-04-30T12:30:00Z").
						build().
					dataSet().
				withBuild("b4").
					withTitle("Build 4").
					withCreated("2015-05-01T09:00:00Z").
					addExecution("2015-05-01T06:30:00Z").
						failed("2015-05-01T07:00:00Z").
						build().
					addExecution("2015-05-01T09:30:00Z").
						passed("2015-05-01T10:15:00Z").
						build().
					addExecution("2015-05-01T11:30:00Z").
						failed("2015-05-01T12:30:00Z").
						build().
					addExecution("2015-05-01T13:30:00Z").
						passed("2015-05-01T14:45:00Z").
						build().
					dataSet();
		return
			ModelFactory.
				createDefaultModel().
					read(
						new StringReader(dataSet.assemble()),
						null,
						language);
	}

	private String format(Period period) {
		StringBuffer buffer = new StringBuffer();
		this.formatter.getPrinter().printTo(buffer, period, Locale.ENGLISH);
		return buffer.toString();
	}

	private String shorten(String resource) {
		return "<"+resource.replace("http://www.smartdeveloperhub.org/vocabulary/sdh/v1/", "")+">";
	}

	@BeforeClass
	public static void setUpBefore() {
		SesameRDFUtil.install();
	}

	@Before
	public void setUp() {
		this.formatter =
			new PeriodFormatterBuilder().
				appendYears().
				appendSuffix(" year", " years").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMonths().
				appendSuffix(" month", " months").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendDays().
				appendSuffix(" day", " days").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendHours().
				appendSuffix(" hour", " hours").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMinutes().
				appendSuffix(" minute", " minutes").
				appendSeparator(" and ").
				printZeroRarelyLast().
				toFormatter();
		FunctionRegistry.get().put("http://example.org/function#yearMonth", YearMonthFunction.class) ;
	}

	public static class YearMonthFunction extends FunctionBase1 {
		@Override
		public NodeValue exec(NodeValue v) {
			if(!v.isDateTime()) {
				return v;
			}
			XMLGregorianCalendar calendar = v.getDateTime();
			int year = calendar.getYear();
			int month=calendar.getMonth();
			return NodeValue.makeString(String.format("%4d-%02d",year,month));
		}
	}

	@Test
	public void testMetric_TotalBuilds() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_builds.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, buildsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long buildId = solution.getLiteral("total_builds").getLong();
				System.out.printf("Total builds: %d%n",buildId);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TotalExecutions_Global() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_executions_global.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long total_executions = solution.getLiteral("total_executions").getLong();
				System.out.printf("Total executions: %d%n",total_executions);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TotalExecutions_PerBuild() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_executions_per_build.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long total_executions = solution.getLiteral("total_executions").getLong();
				String buildId = shorten(solution.getResource("build").getURI());
				System.out.printf("Total executions of build %s: %d%n",buildId,total_executions);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TotalSuccesfulExecutions_Global() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_succesful_executions_global.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long total_executions = solution.getLiteral("total_executions").getLong();
				System.out.printf("Total succesful executions: %d%n",total_executions);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TotalSuccesfulExecutions_Global_Period() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_succesful_executions_global_period.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long total_executions = solution.getLiteral("total_executions").getLong();
				String day = solution.getLiteral("day").getString();
				System.out.printf("Total succesful executions [%s]: %d%n",day,total_executions);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TotalSuccesfulExecutions_PerBuild() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_succesful_executions_per_build.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long total_executions = solution.getLiteral("total_executions").getLong();
				String buildId = shorten(solution.getResource("build").getURI());
				System.out.printf("Total succesful executions of build %s: %d%n",buildId,total_executions);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TotalBrokenExecutions_Global() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_broken_executions_global.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long total_executions = solution.getLiteral("total_executions").getLong();
				System.out.printf("Total broken executions: %d%n",total_executions);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TotalBrokenExecutions_PerBuild() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/total_broken_executions_per_build.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				long total_executions = solution.getLiteral("total_executions").getLong();
				String buildId = shorten(solution.getResource("build").getURI());
				System.out.printf("Total broken executions of build %s: %d%n",buildId,total_executions);
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_AverageBuildExecutionTime() {
		Multimap<String,Period> executionTimes=LinkedHashMultimap.create();
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/average_build_execution_time_simulation.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				String build= solution.getResource("build").getURI();
				String execution=solution.getResource("execution").getURI();
				Period execution_time = Period.parse(solution.getLiteral("execution_time").getLexicalForm());
				executionTimes.put(build, execution_time);
				System.out.printf("Execution %s of build %s took %s%n",shorten(execution),shorten(build),format(execution_time));
			}
			for(String build:executionTimes.keySet()) {
				Period period=new Period();
				Collection<Period> buildExecutionTimes = executionTimes.get(build);
				for(Period p:buildExecutionTimes) {
					period=period.plus(p);
				}
				period=
					Duration.
						millis(
							period.toStandardDuration().getMillis()/buildExecutionTimes.size()).
							toPeriod();
				System.out.printf("Executions of build %s took an average of %s%n",shorten(build),format(period));
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

	@Test
	public void testMetric_TimeToFixBuild() {
		Query query =
			QueryFactory.
				create(
					loadResource("/metrics/build-time-to-fix.sparql"));
		QueryExecution queryExecution = null;
		try {
			queryExecution = QueryExecutionFactory.create(query, executionsDataSet());
			Map<String,Build> builds=Maps.newLinkedHashMap();
			ResultSet results = queryExecution.execSelect();
			for(; results.hasNext();) {
				QuerySolution solution = results.nextSolution();
				String buildId = solution.getResource("build").getURI();
				Build build = builds.get(buildId);
				if(build==null) {
					build=new Build(buildId);
					builds.put(build.id(), build);
				}
				Execution fixed =
					build.
						createExecution(
							solution.getResource("fixed").getURI(),
							DateTime.parse(solution.getLiteral("fix").getLexicalForm()));
				Execution broken =
					build.
						createExecution(
							solution.getResource("broken").getURI(),
							DateTime.parse(solution.getLiteral("break").getLexicalForm()));
				build.addBrokenPeriod(broken, fixed);
				System.out.printf("Build %s broken on %s (%s), fixed on %s (%s) [%s]%n",shorten(build.id()),broken.finished(),shorten(broken.id()),fixed.finished(),shorten(fixed.id()),format(Period.parse(solution.getLiteral("duration").getLexicalForm())));
			}
			for(Build build:builds.values()) {
				System.out.printf("Build %s requires an average of %s to be fixed%n",shorten(build.id()),format(build.averageBrokenTime().toPeriod()));
			}
		} finally {
			if (queryExecution != null) {
				queryExecution.close();
			}
		}
	}

}