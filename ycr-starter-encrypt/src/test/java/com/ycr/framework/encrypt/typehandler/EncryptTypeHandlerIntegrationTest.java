package com.ycr.framework.encrypt.typehandler;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ycr.framework.encrypt.context.EncryptHandlerHolder;
import com.ycr.framework.encrypt.handler.AesEncryptHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptTypeHandlerIntegrationTest {

    private JdbcDataSource dataSource;
    private SqlSessionFactory sqlSessionFactory;
    private AesEncryptHandler encryptHandler;

    @BeforeEach
    void setUp() throws Exception {
        encryptHandler = new AesEncryptHandler("1234567890abcdef");
        EncryptHandlerHolder.set(encryptHandler);

        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:encrypt_type_handler;MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        createSchema();
        sqlSessionFactory = buildSqlSessionFactory();
    }

    @AfterEach
    void tearDown() {
        EncryptHandlerHolder.clear();
    }

    @Test
    @DisplayName("mybatis写入时应落密文读取时应返回明文")
    void shouldMatchExpectedBehavior001() throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            mapper.insert(new UserRecord(1L, "13800138000"));
        }

        String storedPhone = queryStoredPhone(1L);
        assertNotEquals("13800138000", storedPhone);
        assertTrue(storedPhone.startsWith("ycr:v1:aes-gcm:default:"));
        assertEquals("13800138000", encryptHandler.decrypt(storedPhone));

        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            UserRecord user = mapper.selectById(1L);

            assertNotNull(user);
            assertEquals(1L, user.getId());
            assertEquals("13800138000", user.getPhone());
        }
    }

    @Test
    @DisplayName("mybatisPlus字段注解应能声明EncryptTypeHandler")
    void shouldMatchExpectedBehavior002() throws Exception {
        TableField tableField = MybatisPlusUser.class.getDeclaredField("phone")
                .getAnnotation(TableField.class);

        assertNotNull(tableField);
        assertEquals(EncryptTypeHandler.class, tableField.typeHandler());
    }

    private void createSchema() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("drop table if exists encrypt_user");
            statement.execute("create table encrypt_user(id bigint primary key, phone varchar(255))");
        }
    }

    private SqlSessionFactory buildSqlSessionFactory() {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("test", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.getTypeHandlerRegistry().register(EncryptTypeHandler.class);
        configuration.addMapper(UserMapper.class);
        return new SqlSessionFactoryBuilder().build(configuration);
    }

    private String queryStoredPhone(Long id) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "select phone from encrypt_user where id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
        }
    }

    interface UserMapper {

        @Insert("""
                insert into encrypt_user(id, phone)
                values(#{id}, #{phone,typeHandler=com.ycr.framework.encrypt.typehandler.EncryptTypeHandler})
                """)
        void insert(UserRecord user);

        @Select("select id, phone from encrypt_user where id = #{id}")
        @Results(id = "UserRecordMap", value = {
                @Result(column = "id", property = "id", id = true),
                @Result(column = "phone", property = "phone", typeHandler = EncryptTypeHandler.class)
        })
        UserRecord selectById(Long id);
    }

    static class UserRecord {

        private Long id;
        private String phone;

        UserRecord() {
        }

        UserRecord(Long id, String phone) {
            this.id = id;
            this.phone = phone;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    static class MybatisPlusUser {

        @TableField(typeHandler = EncryptTypeHandler.class)
        private String phone;
    }
}
