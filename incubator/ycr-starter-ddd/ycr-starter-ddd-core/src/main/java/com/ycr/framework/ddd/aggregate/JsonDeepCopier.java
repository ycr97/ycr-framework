package com.ycr.framework.ddd.aggregate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Use Json to clone(deep copy) object.
 *
 * @author meixuesong
 */
public class JsonDeepCopier implements DeepCopier {

    private ObjectMapper mapper;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T copy(T object) {
        try {
            String json = getMapper().writeValueAsString(object);
            return getMapper().readValue(json, (Class<T>) (object.getClass()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized ObjectMapper getMapper() {
        if (mapper == null) {
            createObjectMapper();
        }
        return mapper;
    }

    private void createObjectMapper() {
        if (this.mapper == null) {
            ObjectMapper tempMapper = new ObjectMapper();
            tempMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
            tempMapper.registerModule(new JavaTimeModule());
            tempMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            this.mapper = tempMapper;
        }
    }
}
