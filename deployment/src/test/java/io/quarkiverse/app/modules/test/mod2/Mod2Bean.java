package io.quarkiverse.app.modules.test.mod2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.app.modules.test.mod1.Mod1Bean;

@ApplicationScoped
public class Mod2Bean {
    @Inject
    Mod1Bean bean;
}
