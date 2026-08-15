package com.ycr.framework.ddd.aggregate.deepequals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;

/**
 * The default option for DeepEquals
 * @author meixuesong
 */
public class DeepEqualsDefaultOption extends DeepEqualsOption {
    /**
     * The offset of BigDecimal. If the diff of two BigDecimal value less than the offset, regard them as equal.
     */
    private BigDecimal bigDecimalOffset = new BigDecimal("0.000001");

    public DeepEqualsDefaultOption() {
        getUseComparatorClasses().put(BigDecimal.class, getBigDecimalComparator());
    }

    private Comparator getBigDecimalComparator() {
        return (o1, o2) -> {
            BigDecimal value1 = (BigDecimal) o1;
            BigDecimal value2 = (BigDecimal) o2;

            return  (value1.subtract(value2, MathContext.DECIMAL128)
                    .abs()
                    .subtract(bigDecimalOffset, MathContext.DECIMAL128)
                    .stripTrailingZeros()
                    .compareTo(BigDecimal.ZERO) <= 0) ? 0 : 1;
        };
    }

    public BigDecimal getBigDecimalOffset() {
        return bigDecimalOffset;
    }

    public void setBigDecimalOffset(BigDecimal bigDecimalOffset) {
        this.bigDecimalOffset = bigDecimalOffset;
    }
}
