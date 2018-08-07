package org.inspirenxe.skills.impl.content.component.apply.math;

import org.inspirenxe.skills.impl.content.component.apply.MathOperationType;

import java.math.BigDecimal;

public class MathOperation {

    private MathOperationType type;
    private BigDecimal secondValue;

    public MathOperation(MathOperationType type, BigDecimal secondValue) {
        this.type = type;
        this.secondValue = secondValue;
    }

    public BigDecimal apply(BigDecimal firstValue) {
        return this.type.apply(firstValue, this.secondValue);
    }

}
