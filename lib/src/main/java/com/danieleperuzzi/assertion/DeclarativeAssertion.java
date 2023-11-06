package com.danieleperuzzi.assertion;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class DeclarativeAssertion<T> {

    private T object;
    private boolean status;

    private DeclarativeAssertion(T object) {
        this.object = object;
    }

    public static <T> DeclarativeAssertion<T> test(T object) {
        return new DeclarativeAssertion<>(object);
    }

    public DeclarativeAssertion<T> when(Predicate<T> p) {
        status = p.test(object);

        return this;
    }

    public void then(Consumer<T> c) {
        if (status) {
            c.accept(object);
        }
    }
}
