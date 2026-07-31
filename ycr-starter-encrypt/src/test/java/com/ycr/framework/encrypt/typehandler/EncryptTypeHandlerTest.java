package com.ycr.framework.encrypt.typehandler;

import com.ycr.framework.encrypt.context.EncryptHandlerHolder;
import com.ycr.framework.encrypt.handler.EncryptHandler;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptTypeHandlerTest {

    private EncryptHandler encryptHandler;
    private EncryptTypeHandler typeHandler;

    @BeforeEach
    void setUp() {
        encryptHandler = mock(EncryptHandler.class);
        EncryptHandlerHolder.set(encryptHandler);
        typeHandler = new EncryptTypeHandler();
    }

    @AfterEach
    void tearDown() {
        EncryptHandlerHolder.clear();
    }

    @Test
    @DisplayName("setNonNullParameter应加密后写入PreparedStatement")
    void shouldMatchExpectedBehavior001() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        when(encryptHandler.encrypt("plain")).thenReturn("cipher");

        typeHandler.setNonNullParameter(statement, 1, "plain", JdbcType.VARCHAR);

        verify(encryptHandler).encrypt("plain");
        verify(statement).setString(1, "cipher");
    }

    @Test
    @DisplayName("getNullableResult_按列名_应解密非空值")
    void shouldMatchExpectedBehavior002() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("phone")).thenReturn("cipher");
        when(encryptHandler.decrypt("cipher")).thenReturn("plain");

        String result = typeHandler.getNullableResult(resultSet, "phone");

        assertEquals("plain", result);
        verify(encryptHandler).decrypt("cipher");
    }

    @Test
    @DisplayName("getNullableResult_按列序号_应解密非空值")
    void shouldMatchExpectedBehavior003() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(2)).thenReturn("cipher");
        when(encryptHandler.decrypt("cipher")).thenReturn("plain");

        String result = typeHandler.getNullableResult(resultSet, 2);

        assertEquals("plain", result);
        verify(encryptHandler).decrypt("cipher");
    }

    @Test
    @DisplayName("getNullableResult_CallableStatement_应解密非空值")
    void shouldMatchExpectedBehavior004() throws Exception {
        CallableStatement statement = mock(CallableStatement.class);
        when(statement.getString(3)).thenReturn("cipher");
        when(encryptHandler.decrypt("cipher")).thenReturn("plain");

        String result = typeHandler.getNullableResult(statement, 3);

        assertEquals("plain", result);
        verify(encryptHandler).decrypt("cipher");
    }

    @Test
    @DisplayName("getNullableResult_数据库空值_应返回null")
    void shouldMatchExpectedBehavior005() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("phone")).thenReturn(null);

        String result = typeHandler.getNullableResult(resultSet, "phone");

        assertNull(result);
    }
}
