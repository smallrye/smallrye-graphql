package com.github.t1.annotations.tck;

public class InheritedAnnotationClasses {

    @SomeAnnotation("1")
    @RepeatableAnnotation(1)
    public interface InheritingInterface extends Base, SideBase {
    }

    @SomeAnnotation("2")
    @RepeatableAnnotation(2)
    public static class InheritingClass extends Super implements Base, SideBase {
    }

    @SomeAnnotation("3")
    @RepeatableAnnotation(3)
    public static class Super {
        @SomeAnnotation("4")
        @RepeatableAnnotation(4)
        public String field;

        @SomeAnnotation("5")
        @RepeatableAnnotation(5)
        public String method() {
            return null;
        }
    }

    @SomeAnnotation("6")
    @RepeatableAnnotation(6)
    public interface Base extends SuperBase {
        @SomeAnnotation("7")
        @RepeatableAnnotation(7)
        @Override
        String method();
    }

    @SomeAnnotation("8")
    @RepeatableAnnotation(8)
    public interface SideBase {
    }

    @SomeAnnotation("9")
    @RepeatableAnnotation(9)
    public interface SuperBase {
        @SomeAnnotation("10")
        @RepeatableAnnotation(10)
        default String method() {
            return null;
        }
    }
}
