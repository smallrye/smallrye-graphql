package test.unit;

class Outside {
    static String packagePrivateMethod() {
        return "package-private-method-value";
    }

    @SuppressWarnings("unused")
    private static String privateMethod() {
        return "private-method-value";
    }
}
