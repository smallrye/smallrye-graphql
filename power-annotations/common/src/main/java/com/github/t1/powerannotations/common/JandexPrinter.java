package com.github.t1.powerannotations.common;

import java.util.function.Consumer;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

public class JandexPrinter {

    private final IndexView index;
    private final Logger logger;

    public JandexPrinter(IndexView index, Logger logger) {
        this.index = index;
        this.logger = logger;
    }

    public void run() {
        logger.info("------------------------------------------------------------");
        ((Index) index).printAnnotations();
        logger.info("------------------------------------------------------------");
        ((Index) index).printSubclasses();
        logger.info("------------------------------------------------------------");
        index.getKnownClasses().forEach(new Consumer<ClassInfo>() {
            @Override
            public void accept(ClassInfo classInfo) {
                if (!classInfo.name().toString().startsWith("test."))
                    return;
                logger.info(classInfo.name() + ":");
                classInfo.methods().forEach(new Consumer<MethodInfo>() {
                    @Override
                    public void accept(MethodInfo method) {
                        logger.info("    " + method.name() + " [" + method.defaultValue() + "]");
                    }
                });
            }
        });
        logger.info("------------------------------------------------------------");
    }
}
