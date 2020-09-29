/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.explainability.local.counterfactual;


import org.kie.kogito.explainability.local.counterfactual.entities.CounterfactualEntity;
import org.kie.kogito.explainability.model.Feature;
import org.kie.kogito.explainability.model.PredictionInput;
import org.kie.kogito.explainability.model.PredictionOutput;
import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CounterFactualScoreCalculator implements EasyScoreCalculator<CounterfactualSolution> {

    private static final Logger logger =
            LoggerFactory.getLogger(CounterFactualScoreCalculator.class);

    @Override
    public BendableBigDecimalScore calculateScore(CounterfactualSolution solution) {

        int primaryHardScore = 0;
        int secondaryHardScore = 0;
        double primarySoftScore = 0.0;

        logger.info("=====================================================================");

        StringBuilder builder = new StringBuilder();

        for (CounterfactualEntity entity : solution.getEntities()) {
            primarySoftScore += entity.distance();
            final Feature f = entity.asFeature();
            builder.append(f.getName()).append("=").append(f.getValue().getUnderlyingObject()).append("(d: ").append(entity.distance()).append("),");

            if (entity.isConstrained() && (entity.isChanged())) {
                secondaryHardScore -= 1;
            }

        }

        logger.info(builder.toString());

        List<Feature> input = solution.getEntities().stream().map(CounterfactualEntity::asFeature).collect(Collectors.toList());

        PredictionInput predictionInput = new PredictionInput(input);

        List<PredictionInput> inputs = new ArrayList<>();
        inputs.add(predictionInput);
        CompletableFuture<List<PredictionOutput>> predictionAsync = solution.getModel().predictAsync(inputs);

        try {
            List<PredictionOutput> prediction = predictionAsync.get();
            if (!prediction.get(0).getOutputs().get(0).getValue().equals(solution.getGoal().getValue())) {
                primaryHardScore -= 1;
                logger.info("Penalise outcome (output: {})", prediction.get(0).getOutputs().get(0));
            } else {
                logger.info("Reward outcome (output: {})", prediction.get(0).getOutputs().get(0));
            }

        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        }

        logger.info("Soft score: " + (-Math.abs(primarySoftScore)));
        return BendableBigDecimalScore.of(
                new BigDecimal[]{
                        BigDecimal.valueOf(primaryHardScore), BigDecimal.valueOf(secondaryHardScore)
                },
                new BigDecimal[]{BigDecimal.valueOf(-Math.abs(primarySoftScore))});
    }
}
