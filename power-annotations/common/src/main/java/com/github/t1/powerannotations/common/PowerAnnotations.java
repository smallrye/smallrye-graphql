package com.github.t1.powerannotations.common;

import org.jboss.jandex.Index;

public class PowerAnnotations {
    private final Jandex jandex;
    private final Logger log;

    public PowerAnnotations(Index index, Logger log) {
        this.jandex = new Jandex(index);
        this.log = log;
    }

    public void resolveAnnotations() {
        new MixinResolver(jandex, log).run();
        new StereotypeResolver(jandex, log).run();
    }
}
