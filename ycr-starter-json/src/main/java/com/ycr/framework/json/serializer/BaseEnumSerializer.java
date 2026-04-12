package com.ycr.framework.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ycr.framework.core.enums.BaseEnum;

import java.io.IOException;

public class BaseEnumSerializer extends JsonSerializer<BaseEnum<?>> {

    public static final BaseEnumSerializer INSTANCE = new BaseEnumSerializer();

    @Override
    public void serialize(BaseEnum<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeObject(value.getValue());
    }
}
