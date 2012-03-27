/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.cloudifysource.dsl.autoscaling;

import org.cloudifysource.dsl.Service;
import org.cloudifysource.dsl.internal.CloudifyDSLEntity;
import org.cloudifysource.dsl.internal.DSLReader;


/**
 * Domain Object for defining SLA based on statistics and thresholds that triggers 
 * scale out or scale in action.
 * 
 * @author itaif
 * @since 2.1
 * @see Service
 */
@CloudifyDSLEntity(name = "autoScaling", clazz = AutoScalingDetails.class, allowInternalNode = true, allowRootNode = false,
parent = "service")
public class AutoScalingDetails {
	
	private static final long DEFAULT_SAMPLING_PERIOD_SECONDS = 60;

	private static final long DEFAULT_TIME_WINDOW_SECONDS = 5*DEFAULT_SAMPLING_PERIOD_SECONDS;

	/**
	 * This helper static member is automatically imported to the DSL recipe
	 * @see #getStatisticsFactory() for more details
	 */
	private static final AutoScalingStatisticsFactory statisticsFactory = new AutoScalingStatisticsFactory();
	
	private static final AutoScalingStatistics DEFAULT_TIME_STATISTICS = statisticsFactory.average();
	
	/**
	 * default statistics is average, since it is unwise to trigger an auto scale operation
	 * based on a single instance gone wild.
	 */
	private static final AutoScalingStatistics DEFAULT_INSTANCES_STATISTICS = statisticsFactory.average();
	
	private long samplingPeriodSeconds = DEFAULT_SAMPLING_PERIOD_SECONDS;
	
	private String metric;
	
	private long timeWindowSeconds = DEFAULT_TIME_WINDOW_SECONDS;
	
	private AutoScalingStatistics timeStatistics = DEFAULT_TIME_STATISTICS;
	
	private AutoScalingStatistics instancesStatistics = DEFAULT_INSTANCES_STATISTICS;
	
	private Comparable<?> highThreshold;
	
	private Comparable<?> lowThreshold;
	
	
	public AutoScalingStatistics getTimeStatistics() {
		return timeStatistics;
	}

	/**
	 * (Optional)
	 * The algorithm for aggregating metric samples in the specified time window.
     * Metric samples are aggregated separately per instance.
     * Default: statistics.average
     * Possible values: statistics.average, statistics.minimum, statistics.maximum, statistics.percentile(n)
     */
	public void setTimeStatistics(AutoScalingStatistics timeStatistics) {
		this.timeStatistics = timeStatistics;
	}

	public AutoScalingStatistics getInstancesStatistics() {
		return instancesStatistics;
	}

    /**
     * (Optional)
     * The algorithm use to aggregate timeStatistics from all of the instances
     * Default value: statistics.average
     * Possible values: statistics.average, statistics.minimum, statistics.maximum, statistics.percentile(n)
     */
	public void setInstancesStatistics(AutoScalingStatistics instancesStatistics) {
		this.instancesStatistics = instancesStatistics;
	}

	public Comparable<?> getHighThreshold() {
		return highThreshold;
	}

	/**
	 * The instancesStatistics value over which the number of instances is increased
	 */
	public void setHighThreshold(Comparable<?> highThreshold) {
		this.highThreshold = highThreshold;
	}

	/**
	 * The instancesStatistics value below which the number of instances is increased or decreased
	 */
	public Comparable<?> getLowThreshold() {
		return lowThreshold;
	}

	public void setLowThreshold(Comparable<?> lowThreshold) {
		this.lowThreshold = lowThreshold;
	}

	public long getTimeWindowSeconds() {
		return timeWindowSeconds;
	}

	/**
	 * The sliding time window (in seconds) for aggregating per-instance metric samples
     * The number of samples in the time windows equals the time window divided by the sampling period
	 */
	public void setTimeWindowSeconds(long timeWindowSeconds) {
		this.timeWindowSeconds = timeWindowSeconds;
	}

	public String getMetric() {
		return metric;
	}

	/**
	 * The name of the metric that is the basis for the scale rule decision
	 */
	public void setMetric(String metric) {
		this.metric = metric;
	}

	public long getSamplingPeriodSeconds() {
		return samplingPeriodSeconds;
	}

	/**
	 * The time (in seconds) between two consecutive metric samples
	 */
	public void setSamplingPeriodSeconds(long samplingPeriodSeconds) {
		this.samplingPeriodSeconds = samplingPeriodSeconds;
	}

	/**
	 * This helper method is automatically imported to the DSL recipe
	 * import org.cloudifysource.dsl.autoscaling.AtuoScalingDetails.statisticsFatory as statistics
	 * @see DSLReader#createCompilerConfiguration() for more details
	 */
	public AutoScalingStatisticsFactory getStatisticsFactory() {
		return statisticsFactory;
	}
}
