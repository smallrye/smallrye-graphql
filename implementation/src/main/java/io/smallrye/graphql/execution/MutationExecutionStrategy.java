/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.smallrye.graphql.execution;

import graphql.execution.AsyncSerialExecutionStrategy;

/**
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MutationExecutionStrategy extends AsyncSerialExecutionStrategy {

    public MutationExecutionStrategy() {
        super(new ExceptionHandler());
    }

}
