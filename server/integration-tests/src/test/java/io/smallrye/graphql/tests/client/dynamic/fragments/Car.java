package io.smallrye.graphql.tests.client.dynamic.fragments;

public class Car implements Vehicle {

    private int wheelsCount;
    private int engineCylinders;

    public Car(Integer engineCyliners) {
        this.wheelsCount = 4;
        this.engineCylinders = engineCyliners;
    }

    public int getEngineCylinders() {
        return engineCylinders;
    }

    public void setEngineCylinders(int engineCylinders) {
        this.engineCylinders = engineCylinders;
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
        return "Car{" +
                "wheelsCount=" + wheelsCount +
                ", engineCylinders=" + engineCylinders +
                '}';
    }
}
