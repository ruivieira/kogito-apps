/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
 */
package org.kie.kogito.explainability.local.counterfactual;

import org.junit.jupiter.api.Test;
import org.kie.kogito.explainability.Config;
import org.kie.kogito.explainability.TestUtils;
import org.kie.kogito.explainability.local.LocalExplanationException;
import org.kie.kogito.explainability.local.counterfactual.entities.CounterfactualEntity;
import org.kie.kogito.explainability.model.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CounterfactualExplainerTest {

    @Test
    void testNonEmptyInput() throws ExecutionException, InterruptedException, TimeoutException {
        Random random = new Random();

        final Output goal = new Output("class", Type.BOOLEAN, new Value<>(false), 1d);
        for (int seed = 0; seed < 5; seed++) {
            random.setSeed(seed);


            List<Feature> features = new LinkedList<>();
            List<FeatureDistribution> featureDistributions = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                features.add(TestUtils.getMockedNumericFeature(i));
                featureDistributions.add(new FeatureDistribution(0.0, 1000.0, 500.0, 1.0));
            }
            final DataDistribution dataDistribution = new DataDistribution(featureDistributions);
            CounterfactualExplainer counterfactualExplainer = new CounterfactualExplainer(dataDistribution, goal);

            PredictionInput input = new PredictionInput(features);
            PredictionProvider model = TestUtils.getSumSkipModel(0);
            PredictionOutput output = model.predictAsync(List.of(input))
                    .get(Config.INSTANCE.getAsyncTimeout(), Config.INSTANCE.getAsyncTimeUnit())
                    .get(0);
            Prediction prediction = new Prediction(input, output);
            List<CounterfactualEntity> counterfactualEntities = counterfactualExplainer.explainAsync(prediction, model)
                    .get(Config.INSTANCE.getAsyncTimeout(), Config.INSTANCE.getAsyncTimeUnit());
            for (CounterfactualEntity entity : counterfactualEntities) {
                System.out.println(entity);
            }
            assertNotNull(counterfactualEntities);
        }
    }

    @Test
    void testCounterfactualMatch() throws ExecutionException, InterruptedException, TimeoutException {
        Random random = new Random();

        final Output goal = new Output("inside", Type.BOOLEAN, new Value<>(true), 1d);
        for (int seed = 0; seed < 5; seed++) {
            random.setSeed(seed);


            List<Feature> features = new LinkedList<>();
            List<FeatureDistribution> featureDistributions = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                features.add(TestUtils.getMockedNumericFeature(i));
                featureDistributions.add(new FeatureDistribution(0.0, 1000.0, 500.0, 1.0));
            }
            final DataDistribution dataDistribution = new DataDistribution(featureDistributions);
            CounterfactualExplainer counterfactualExplainer = new CounterfactualExplainer(dataDistribution, goal);

            PredictionInput input = new PredictionInput(features);
            PredictionProvider model = TestUtils.getSumThresholdModel(500.0, 10.0);
            PredictionOutput output = model.predictAsync(List.of(input))
                    .get(Config.INSTANCE.getAsyncTimeout(), Config.INSTANCE.getAsyncTimeUnit())
                    .get(0);
            Prediction prediction = new Prediction(input, output);
            List<CounterfactualEntity> counterfactualEntities = counterfactualExplainer.explainAsync(prediction, model)
                    .get(Config.INSTANCE.getAsyncTimeout(), Config.INSTANCE.getAsyncTimeUnit());
            for (CounterfactualEntity entity : counterfactualEntities) {
                System.out.println(entity);
            }
            assertNotNull(counterfactualEntities);
        }
    }
}