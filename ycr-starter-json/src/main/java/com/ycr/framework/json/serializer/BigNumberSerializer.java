package com.ycr.framework.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigInteger;

public class BigNumberSerializer extends JsonSerializer<Number> {

    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer();

    private static final long JS_SAFE_INTEGER_MAX = 9007199254740991L;
    private static final long JS_SAFE_INTEGER_MIN = -9007199254740991L;
    private static final BigInteger JS_SAFE_INTEGER_MAX_BIG = BigInteger.valueOf(JS_SAFE_INTEGER_MAX);
    private static final BigInteger JS_SAFE_INTEGER_MIN_BIG = BigInteger.valueOf(JS_SAFE_INTEGER_MIN);

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if (value instanceof BigInteger bigInteger) {
            if (isOutOfSafeRange(bigInteger)) {
                gen.writeString(bigInteger.toString());
                return;
            }
            gen.writeNumber(bigInteger);
            return;
        }

        long longValue = value.longValue();
        if (isOutOfSafeRange(longValue)) {
            gen.writeString(Long.toString(longValue));
            return;
        }
        gen.writeNumber(longValue);
    }

    private boolean isOutOfSafeRange(long value) {
        return value > JS_SAFE_INTEGER_MAX || value < JS_SAFE_INTEGER_MIN;
    }

    private boolean isOutOfSafeRange(BigInteger value) {
        return value.compareTo(JS_SAFE_INTEGER_MAX_BIG) > 0 || value.compareTo(JS_SAFE_INTEGER_MIN_BIG) < 0;
    }
}
