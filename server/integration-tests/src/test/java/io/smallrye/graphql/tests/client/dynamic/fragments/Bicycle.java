package io.smallrye.graphql.tests.client.dynamic.fragments;

public class Bicycle implements Vehicle {

    private int frameSize;
    private int wheelsCount;

    public Bicycle(int frameSize) {
        this.wheelsCount = 2;
        this.frameSize = frameSize;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    @Override
    public int getWheelsCount() {
        return wheelsCount;
    }

    public void setWheelsCount(int wheelsCount) {
        this.wheelsCount = wheelsCount;
    }

    @Override
    public String toString() {
        return "Bicycle{" +
                "frameSize=" + frameSize +
                ", wheelsCount=" + wheelsCount +
                '}';
    }
}
