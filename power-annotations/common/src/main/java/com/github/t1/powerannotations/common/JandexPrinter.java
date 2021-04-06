package com.github.t1.powerannotations.common;

import static com.github.t1.powerannotations.common.PowerAnnotations.log;

import java.util.function.Consumer;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

public class JandexPrinter {

    private final IndexView index;

    public JandexPrinter(IndexView index) {
        this.index = index;
    }

    public void run() {
        log.info("------------------------------------------------------------");
        ((Index) index).printAnnotations();
        log.info("------------------------------------------------------------");
        ((Index) index).printSubclasses();
        log.info("------------------------------------------------------------");
        index.getKnownClasses().forEach(new Consumer<ClassInfo>() {
            @Override
            public void accept(ClassInfo classInfo) {
                if (!classInfo.name().toString().startsWith("test."))
                    return;
                log.info(classInfo.name() + ":");
                classInfo.methods().forEach(new Consumer<MethodInfo>() {
                    @Override
                    public void accept(MethodInfo method) {
                        log.info("    " + method.name() + " [" + method.defaultValue() + "]");
                    }
                });
            }
        });
        log.info("------------------------------------------------------------");
    }
}
