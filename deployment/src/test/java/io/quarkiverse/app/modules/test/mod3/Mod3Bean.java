package io.quarkiverse.app.modules.test.mod3;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.app.modules.test.mod1.Mod1Bean;
import io.quarkiverse.app.modules.test.mod2.Mod2Bean;

@ApplicationScoped
public class Mod3Bean {
    @Inject
    Mod1Bean bean;

    @Inject
    Mod2Bean mod2Bean; // this is an error
}
