/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.smallrye.graphql;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Blacklist {

    public static boolean ignore(String className) {
        if (defaultBlackList.contains(className))
            return true;
        for (String blacklistEntry : defaultBlackList) {
            if (className.startsWith(blacklistEntry))
                return true;
        }
        return false;
    }

    private static final List<String> defaultBlackList = Arrays.asList(new String[] {
            "java.",
            "javax.",
            "sun.",
            "com.sun.",
            "graphql.",
            "io.leangen.",
            "org.jboss.",
            "org.wildfly.",
            "io.smallrye.",
            "org.testng.",
            "nonapi.",
            "com.fasterxml.",
            "org.yaml.",
            "org.reactivestreams.",
            "io.github.classgraph.",
            "__redirected." });

}
