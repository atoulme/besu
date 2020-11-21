/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.metrics.prometheus;

import org.hyperledger.besu.metrics.opentelemetry.OpenTelemetrySystem;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;

import java.util.Set;
import java.util.function.Supplier;

import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

public class PrometheusMetricsSystem extends OpenTelemetrySystem {

  private final CollectorRegistry registry = new CollectorRegistry(true);

  public PrometheusMetricsSystem(
      final Set<MetricCategory> enabledCategories,
      final boolean timersEnabled,
      final String jobName) {
    super(enabledCategories, timersEnabled, jobName);
    PrometheusCollector.builder()
        .setMetricProducer(getMeterSdkProvider().getMetricProducer())
        .build()
        .register(registry);
  }

  CollectorRegistry getRegistry() {
    return registry;
  }

  public void addCollector(
      final MetricCategory category, final Supplier<Collector> metricSupplier) {
    if (isCategoryEnabled(category)) {
      metricSupplier.get().register(registry);
    }
  }
}
