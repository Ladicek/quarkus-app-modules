package io.quarkiverse.app.modules.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import jakarta.enterprise.inject.spi.DeploymentException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.app.modules.test.mod1.Mod1Bean;
import io.quarkiverse.app.modules.test.mod2.Mod2Bean;
import io.quarkiverse.app.modules.test.mod3.Mod3Bean;
import io.quarkus.test.QuarkusUnitTest;

public class AppModulesTest {
    private static Class<?> classOfPackage(Class<?> clazz) {
        try {
            return Class.forName(clazz.getPackageName() + ".package-info");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @RegisterExtension
    static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
            .setArchiveProducer(
                    () -> ShrinkWrap.create(JavaArchive.class).addClasses(Mod1Bean.class, Mod2Bean.class, Mod3Bean.class,
                            classOfPackage(Mod1Bean.class), classOfPackage(Mod2Bean.class), classOfPackage(Mod3Bean.class)))
            .assertException(ex -> {
                assertInstanceOf(DeploymentException.class, ex);
                assertInstanceOf(IllegalStateException.class, ex.getCause());
                assertEquals("Class 'io.quarkiverse.app.modules.test.mod2.Mod2Bean' (in module"
                        + " 'io.quarkiverse.app.modules.test.mod2') cannot be referenced from class"
                        + " 'io.quarkiverse.app.modules.test.mod3.Mod3Bean' (in module"
                        + " 'io.quarkiverse.app.modules.test.mod3)'", ex.getCause().getMessage());
            });

    @Test
    public void trigger() {
    }
}
