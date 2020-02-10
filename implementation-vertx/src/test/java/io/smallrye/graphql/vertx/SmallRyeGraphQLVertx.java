/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql.vertx;

import org.jboss.weld.vertx.web.WeldWebVerticle;

import io.vertx.core.Vertx;

/**
 *  Starting the Vertx web endpoint
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeGraphQLVertx {

    
    public static void main(String[] args) {
        
        Vertx vertx = Vertx.vertx();
        
        WeldWebVerticle weldVerticle = new WeldWebVerticle();
        vertx.deployVerticle(weldVerticle, result -> {
            if (result.succeeded()) {
                vertx.createHttpServer().requestHandler(weldVerticle.createRouter()::accept).listen(8080);
            } else {
                throw new IllegalStateException("VertX Weld - creating the HTTP Endpoint failed: " + result.cause());
            }
        });
        
    }
}
