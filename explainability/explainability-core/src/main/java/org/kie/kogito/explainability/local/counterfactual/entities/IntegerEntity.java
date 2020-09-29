package org.kie.kogito.explainability.local.counterfactual.entities;

import org.kie.kogito.explainability.model.Feature;
import org.kie.kogito.explainability.model.FeatureFactory;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class IntegerEntity implements CounterfactualEntity {
    @PlanningVariable(valueRangeProviderRefs = {"intRange"})
    public Integer value;

    int intRangeMinimum;
    int intRangeMaximum;
    private Feature feature;
    private boolean constrained;

    public IntegerEntity() {
    }

    public IntegerEntity(Feature feature, int minimum, int maximum, boolean constrained) {
        this.value = (int) feature.getValue().asNumber();
        this.feature = feature;
        this.intRangeMinimum = minimum;
        this.intRangeMaximum = maximum;
        this.constrained = constrained;
    }

    public IntegerEntity(Feature feature, int minimum, int maximum) {
        this(feature, minimum, maximum, false);
    }

    @ValueRangeProvider(id = "intRange")
    public ValueRange getValueRange() {
        return ValueRangeFactory.createIntValueRange(intRangeMinimum, intRangeMaximum);
    }

    @Override
    public String toString() {
        return "IntegerFeature{"
                + "value="
                + value
                + ", intRangeMinimum="
                + intRangeMinimum
                + ", intRangeMaximum="
                + intRangeMaximum
                + ", id='"
                + feature.getName()
                + '\''
                + '}';
    }

    @Override
    public double distance() {
        return Math.abs(this.value - (int) this.feature.getValue().asNumber());
    }

    @Override
    public Feature asFeature() {
        return FeatureFactory.newNumericalFeature(feature.getName(), this.value);
    }

    @Override
    public boolean isConstrained() {
        return constrained;
    }

    @Override
    public boolean isChanged() {
        return !this.feature.getValue().getUnderlyingObject().equals(this.value);
    }
}
