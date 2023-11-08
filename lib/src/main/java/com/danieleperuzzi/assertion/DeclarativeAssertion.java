/*
 * Copyright 2023 Daniele Peruzzi. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.danieleperuzzi.assertion;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Simple class to perform some conditional test in declarative and easy readable mode.
 *
 * The result of the application of this class would be
 *
 * <pre>{@code
 * test(apiResponse)
 *      .when(response -> response.getStatus() == 200)
 *      .then(response -> validateSuccessResponse(response));
 * }</pre>
 *
 * @param <T>   the type of the object to be tested
 */
public class DeclarativeAssertion<T> {

    private T object;
    private Predicate<T> predicate;

    private DeclarativeAssertion(T object) {
        this.object = object;
    }

    /**
     * Used to create a new DeclarativeAssertion instance, the main purpose is to wrap object
     * instantiation with a method with an explicit name
     *
     * @param object    the object to be tested
     * @return          a new DeclarativeAssertion instance
     * @param <T>       the type of the object to be tested
     */
    public static <T> DeclarativeAssertion<T> test(T object) {
        return new DeclarativeAssertion<>(object);
    }

    /**
     * Store this predicate for later use
     *
     * @param p     the predicate that tests the object
     * @return      current DeclarativeAssertion instance
     */
    public DeclarativeAssertion<T> when(Predicate<T> p) {
        predicate = p;

        return this;
    }

    /**
     * Perform the test on the stored object only if the given
     * predicate is successful
     *
     * @param c     a consumer that represents the assertions on
     *              the stored object
     */
    public void then(Consumer<T> c) {
        if (predicate.test(object)) {
            c.accept(object);
        }
    }
}
