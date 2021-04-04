package com.github.t1.powerannotations.common;

import org.jboss.jandex.Index;

public class PowerAnnotations {
    private final Jandex jandex;
    @SuppressWarnings({ "Convert2Lambda", "Anonymous2MethodRef" }) // wouldn't work in a Maven Plugin
    public static Logger log = new Logger() {
        @Override
        public void info(String message) {
            System.out.println(message);
        }
    };

    public PowerAnnotations(Index index) {
        this.jandex = new Jandex(index);
    }

    public void resolveAnnotations() {
        new MixinResolver(jandex).run();
        new StereotypeResolver(jandex).run();
    }
}
