package com.ycr.framework.security.aspect;

import com.ycr.framework.context.holder.UserContextHolder;
import com.ycr.framework.context.model.UserContext;
import com.ycr.framework.security.annotation.RequireLogin;
import com.ycr.framework.security.annotation.RequirePermission;
import com.ycr.framework.security.annotation.RequireRole;
import com.ycr.framework.security.autoconfigure.SecurityAutoConfiguration;
import com.ycr.framework.security.exception.AuthException;
import com.ycr.framework.security.exception.ForbiddenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AuthorizeAspect 集成测试。
 *
 * @author ycr
 */
class AuthorizeAspectTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class, SecurityAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void service方法权限注解应生效() {
        runner.run(context -> {
            setUser("admin", "order:create");
            assertEquals("created", context.getBean(TestService.class).create());

            assertThatThrownBy(() -> context.getBean(TestService.class).update())
                    .isInstanceOf(ForbiddenException.class);
        });
    }

    @Test
    void controller方法未登录应抛AuthException() {
        runner.run(context -> assertThatThrownBy(() -> context.getBean(TestController.class).profile())
                .isInstanceOf(AuthException.class));
    }

    @Test
    void 类级注解应作为默认要求且方法级可覆盖() {
        runner.run(context -> {
            setUser("user", "order:create");

            assertThatThrownBy(() -> context.getBean(ClassSecuredService.class).list())
                    .isInstanceOf(ForbiddenException.class);
            assertEquals("created", context.getBean(ClassSecuredService.class).create());
        });
    }

    private void setUser(String role, String permission) {
        UserContext userContext = new UserContext();
        userContext.setUserId(1L);
        userContext.setRoles(Set.of(role));
        userContext.setPermissions(Set.of(permission));
        UserContextHolder.set(userContext);
    }

    @Configuration
    static class TestConfig {

        @Bean
        TestService testService() {
            return new TestService();
        }

        @Bean
        TestController testController() {
            return new TestController();
        }

        @Bean
        ClassSecuredService classSecuredService() {
            return new ClassSecuredService();
        }
    }

    static class TestService {

        @RequirePermission("order:create")
        public String create() {
            return "created";
        }

        @RequirePermission("order:update")
        public String update() {
            return "updated";
        }
    }

    static class TestController {

        @RequireLogin
        public String profile() {
            return "profile";
        }
    }

    @RequireRole("admin")
    static class ClassSecuredService {

        public String list() {
            return "list";
        }

        @RequirePermission("order:create")
        public String create() {
            return "created";
        }
    }
}
