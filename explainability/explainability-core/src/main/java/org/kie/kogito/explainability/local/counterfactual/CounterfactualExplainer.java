package org.kie.kogito.explainability.local.counterfactual;

import org.kie.kogito.explainability.local.LocalExplainer;
import org.kie.kogito.explainability.local.counterfactual.entities.BooleanEntity;
import org.kie.kogito.explainability.local.counterfactual.entities.CounterfactualEntity;
import org.kie.kogito.explainability.local.counterfactual.entities.DoubleEntity;
import org.kie.kogito.explainability.local.counterfactual.entities.IntegerEntity;
import org.kie.kogito.explainability.model.*;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.localsearch.LocalSearchPhaseConfig;
import org.optaplanner.core.config.localsearch.decider.acceptor.AcceptorConfig;
import org.optaplanner.core.config.localsearch.decider.forager.LocalSearchForagerConfig;
import org.optaplanner.core.config.phase.PhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CounterfactualExplainer implements LocalExplainer<List<CounterfactualEntity>> {

    private final Output goal;
    private final DataDistribution dataDistribution;

    public CounterfactualExplainer(DataDistribution dataDistribution, Output goal) {
        this.goal = goal;
        this.dataDistribution = dataDistribution;
    }

    private SolverConfig createSolverConfig() {
        SolverConfig solverConfig = new SolverConfig();

        solverConfig.withEntityClasses(IntegerEntity.class, DoubleEntity.class, BooleanEntity.class);
        solverConfig.setSolutionClass(CounterfactualSolution.class);

        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setEasyScoreCalculatorClass(CounterFactualScoreCalculator.class);
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSecondsSpentLimit(5L);
        solverConfig.setTerminationConfig(terminationConfig);

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setEntityTabuSize(70);

        LocalSearchForagerConfig localSearchForagerConfig = new LocalSearchForagerConfig();
        localSearchForagerConfig.setAcceptedCountLimit(5000);

        LocalSearchPhaseConfig localSearchPhaseConfig = new LocalSearchPhaseConfig();
        localSearchPhaseConfig.setAcceptorConfig(acceptorConfig);
        localSearchPhaseConfig.setForagerConfig(localSearchForagerConfig);

        List<PhaseConfig> phaseConfigs = new ArrayList<>();
        phaseConfigs.add(localSearchPhaseConfig);

        solverConfig.setPhaseConfigList(phaseConfigs);
        return solverConfig;
    }

    private List<CounterfactualEntity> createEntities(PredictionInput predictionInput) {
        final List<CounterfactualEntity> entities = new ArrayList<>();

        for (int i = 0 ; i < predictionInput.getFeatures().size() ; i++) {
            final Feature feature = predictionInput.getFeatures().get(i);
            if (feature.getType() == Type.NUMBER) {
                final FeatureDistribution featureDistribution = dataDistribution.getFeatureDistributions().get(i);
                final DoubleEntity doubleEntity = new DoubleEntity(feature, featureDistribution.getMin(), featureDistribution.getMax(), false);
                entities.add(doubleEntity);
            } else if (feature.getType() == Type.BOOLEAN) {
                final BooleanEntity booleanEntity = new BooleanEntity(feature, false);
                entities.add(booleanEntity);
            }
        }
        return entities;
    }

    @Override
    public CompletableFuture<List<CounterfactualEntity>> explainAsync(Prediction prediction, PredictionProvider model) {

        final List<CounterfactualEntity> entities = createEntities(prediction.getInput());

        final UUID problemId = UUID.randomUUID();
        final SolverConfig solverConfig = createSolverConfig();

        SolverManager<CounterfactualSolution, UUID> solverManager =
                SolverManager.create(solverConfig, new SolverManagerConfig());

        CounterfactualSolution problem =
                new CounterfactualSolution(entities, model, goal);


        SolverJob<CounterfactualSolution, UUID> solverJob = solverManager.solve(problemId, problem);
        CounterfactualSolution solution;
        try {
            // Wait until the solving ends
            solution = solverJob.getFinalBestSolution();
            System.out.println(solution.toString());
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Solving failed.", e);
        }

        System.out.println("The counterfactual is:");
        for (CounterfactualEntity cfEntity : solution.getEntities()) {
            System.out.println(cfEntity.asFeature().toString());
        }
        return CompletableFuture.completedFuture(solution.getEntities());
    }
}
