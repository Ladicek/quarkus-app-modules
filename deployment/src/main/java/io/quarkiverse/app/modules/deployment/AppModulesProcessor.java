package io.quarkiverse.app.modules.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

import io.quarkiverse.app.modules.AppModule;
import io.quarkus.arc.deployment.ValidationPhaseBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class AppModulesProcessor {
    private static final String FEATURE = "app-modules";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void enforceStructure(CombinedIndexBuildItem index, ValidationPhaseBuildItem validation,
            BuildProducer<ValidationPhaseBuildItem.ValidationErrorBuildItem> errors) {
        List<Module> modules = new ArrayList<>();
        for (AnnotationInstance annotation : index.getComputingIndex().getAnnotations(AppModule.class)) {
            String name = annotation.target().asClass().name().toString().replace(".package-info", "");
            AnnotationValue dependencies = annotation.value("dependencies");
            if (dependencies != null) {
                modules.add(new Module(name, new HashSet<>(Arrays.asList(dependencies.asStringArray()))));
            } else {
                modules.add(new Module(name));
            }
        }

        for (Module module : modules) {
            for (Module search : modules) {
                if (module.equals(search)) {
                    continue;
                }
                if (search.name().startsWith(module.name())) {
                    errors.produce(new ValidationPhaseBuildItem.ValidationErrorBuildItem(new IllegalStateException(
                            "Module '" + search.name() + "' exists inside module '" + module.name() + "', this is forbidden")));
                    return;
                }
            }
        }

        Map<Module, Set<Module>> bannedModules = new HashMap<>();
        for (ClassInfo clazz : index.getComputingIndex().getKnownClasses()) {
            Module classModule = null;
            for (Module module : modules) {
                if (clazz.name().toString().startsWith(module.name() + ".")) {
                    classModule = module;
                    break;
                }
            }

            if (classModule == null) {
                continue;
            }

            Module classModuleFinal = classModule;
            Set<Module> banned = bannedModules.computeIfAbsent(classModule, ignored -> {
                Set<Module> result = new HashSet<>();
                for (Module module : modules) {
                    if (module.equals(classModuleFinal)) {
                        continue;
                    }
                    if (module.dependencies().contains(classModuleFinal.name())) {
                        continue;
                    }

                    result.add(module);
                }
                return result;
            });

            for (ClassInfo user : index.getComputingIndex().getKnownUsers(clazz.name())) {
                for (Module bannedModule : banned) {
                    if (user.name().toString().startsWith(bannedModule.name())) {
                        errors.produce(new ValidationPhaseBuildItem.ValidationErrorBuildItem(new IllegalStateException(
                                "Class '" + clazz.name() + "' (in module '" + classModuleFinal.name()
                                        + "') cannot be referenced from class '" + user.name() + "' (in module '"
                                        + bannedModule.name() + ")'")));
                    }
                }
            }
        }
    }

    private record Module(String name, Set<String> dependencies) {
        private Module(String name) {
            this(name, Set.of());
        }
    }
}
