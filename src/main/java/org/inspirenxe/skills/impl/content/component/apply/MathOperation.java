package org.inspirenxe.skills.impl.content.component.apply;

public enum MathOperation {
    ADD {
        @Override
        public double apply(double firstValue, double secondValue) {
            return firstValue + secondValue;
        }
    },
    SUBTRACT {
        @Override
        public double apply(double firstValue, double secondValue) {
            return firstValue - secondValue;
        }
    },
    MULTIPLY {
        @Override
        public double apply(double firstValue, double secondValue) {
            return firstValue * secondValue;
        }
    },
    DIVIDE {
        @Override
        public double apply(double firstValue, double secondValue) {
            return firstValue / secondValue;
        }
    };

    public abstract double apply(double firstValue, double secondValue);
}
