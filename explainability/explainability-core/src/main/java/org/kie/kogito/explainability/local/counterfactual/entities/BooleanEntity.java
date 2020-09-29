package org.kie.kogito.explainability.local.counterfactual.entities;

import org.kie.kogito.explainability.model.Feature;
import org.kie.kogito.explainability.model.FeatureFactory;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class BooleanEntity implements CounterfactualEntity {
    @PlanningVariable(valueRangeProviderRefs = {"booleanRange"})
    private Boolean value;

    private Feature feature;

    private boolean constrained;

    public BooleanEntity() {
    }

    public BooleanEntity(Feature feature, boolean constrained) {
        this.value = (Boolean) feature.getValue().getUnderlyingObject();
        this.feature = feature;
        this.constrained = constrained;
    }

    public BooleanEntity(Feature feature) {
        this(feature, false);
    }

    public double distance() {
        return value.equals(feature.getValue().getUnderlyingObject()) ? 0.0 : 1.0;
    }

    @Override
    public Feature asFeature() {
        return FeatureFactory.newBooleanFeature(feature.getName(), this.value);
    }

    @Override
    public boolean isConstrained() {
        return constrained;
    }

    @Override
    public boolean isChanged() {
        return !this.feature.getValue().getUnderlyingObject().equals(this.value);
    }

    @ValueRangeProvider(id = "booleanRange")
    public ValueRange getValueRange() {
        return ValueRangeFactory.createBooleanValueRange();
    }

    @Override
    public String toString() {
        return "BooleanFeature{" + "value=" + value + ", id='" + feature.getName() + '\'' + '}';
    }
}
