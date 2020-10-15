package org.kie.kogito.explainability.local.counterfactual.constraints;

import org.kie.kogito.explainability.local.counterfactual.entities.BooleanEntity;
import org.kie.kogito.explainability.local.counterfactual.entities.DoubleEntity;
import org.optaplanner.core.api.score.buildin.bendablebigdecimal.BendableBigDecimalScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import java.math.BigDecimal;

public class CounterfactualContraintsProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                booleanDistance(constraintFactory),
                doubleDistance(constraintFactory)
        };
    }

    private Constraint booleanDistance(ConstraintFactory constraintFactory) {
        return constraintFactory.from(BooleanEntity.class).filter(BooleanEntity::isChanged)
                .penalize("Boolean feature distance",
                        BendableBigDecimalScore.ofSoft(2, 1, 0, BigDecimal.ONE),
                        entity -> (int) entity.distance());
    }

    private Constraint doubleDistance(ConstraintFactory constraintFactory) {
        return constraintFactory.from(DoubleEntity.class).filter(DoubleEntity::isChanged)
                .penalize("Double feature distance",
                        BendableBigDecimalScore.ofSoft(2, 1, 0, BigDecimal.ONE),
                        entity -> (int) entity.distance());
    }

}
