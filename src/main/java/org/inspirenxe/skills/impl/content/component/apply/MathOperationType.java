package org.inspirenxe.skills.impl.content.component.apply;

import java.math.BigDecimal;

public enum MathOperationType {
    ADD {
        @Override
        public BigDecimal apply(BigDecimal firstValue, BigDecimal secondValue) {
            return firstValue.add(secondValue);
        }
    },
    SUBTRACT {
        @Override
        public BigDecimal apply(BigDecimal firstValue, BigDecimal secondValue) {
            return firstValue.subtract(secondValue);
        }
    },
    MULTIPLY {
        @Override
        public BigDecimal apply(BigDecimal firstValue, BigDecimal secondValue) {
            return firstValue.multiply(secondValue);
        }
    },
    DIVIDE {
        @Override
        public BigDecimal apply(BigDecimal firstValue, BigDecimal secondValue) {
            return firstValue.divide(secondValue);
        }
    };

    public abstract BigDecimal apply(BigDecimal firstValue, BigDecimal secondValue);
}
