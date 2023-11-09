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

package io.github.danieleperuzzi.assertion.api;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Consumer;

/**
 * This class helps in performing assertions specifically on response APIs because the pattern is always the same: on check
 * for a success case and multiple checks depending on the error triggered
 *
 * The result of the application of this api assertion would be
 *
 * <pre>{@code
 * new ApiAssertion<>(apiResponse)
 *      .isSuccessful(r -> r.getStatus() == 200)
 *      .onSuccess(r -> testOk(r))
 *      .onFailure(r -> r.getStatus() == 400, r -> testKo400(r))
 *      .onFailure(r -> r.getStatus() == 401, r -> testKo401(r))
 *      .test();
 * }</pre>
 *
 * @param <R>   the type of the API response to be tested
 */
public class ApiAssertion<R> {

    private R response;
    private Predicate<R> isSuccessfulPredicate;
    private Consumer<R> successAssertions;
    private Consumer<R> failureAssertions;
    private Map<Predicate<R>, Consumer<R>> failureAssertionMap = new HashMap<>();

    /**
     * Creates a new ApiAssertion
     *
     * @param response      the API response to test
     */
    public ApiAssertion(R response) {
        this.response = response;
        this.isSuccessfulPredicate = null;
        this.successAssertions = null;
        this.failureAssertions = null;
    }

    /**
     * Defines the predicate that checks whenever the API response to be tested is a successful response
     * or a failure response
     *
     * @param p             the predicate that performs the is successful test on the API response
     * @return              this class instance to chain more actions
     * @throws Exception    exception thrown in case we already defined one isSuccessful predicate
     */
    public ApiAssertion<R> isSuccessful(Predicate<R> p) throws Exception {
        if (!Objects.isNull(isSuccessfulPredicate)) {
            throw new Exception("Define only one isSuccessful predicate");
        }

        isSuccessfulPredicate = p;

        return this;
    }

    /**
     * Defines the assertions to be performed when the API response to be tested is a successful response
     *
     * @param ok            the test to be performed in case the API response is success
     * @return              this class instance to chain more actions
     * @throws Exception    exception thrown in case we already defined one onSuccess assertion
     */
    public ApiAssertion<R> onSuccess(Consumer<R> ok) throws Exception {
        if (!Objects.isNull(successAssertions)) {
            throw new Exception("Define only one onSuccess assertion");
        }

        successAssertions = ok;

        return this;
    }

    /**
     * Defines the assertions to be performed when the API response to be tested is a failure response
     *
     * @param ko            the test to be performed in case the API response is failure
     * @return              this class instance to chain more actions
     * @throws Exception    exception thrown in case we already defined one onFailure assertion
     */
    public ApiAssertion<R> onFailure(Consumer<R> ko) throws Exception {
        if (!Objects.isNull(failureAssertions)) {
            throw new Exception("Define only one onFailure assertion");
        }

        failureAssertions = ko;

        return this;
    }

    /**
     * Defines multiple one failure assertions based on specific conditions. This is useful because the api response can fail
     * in multiple ways so assertions may change between error cases
     *
     * @param p     the predicate that checks if the current assertions are the right ones for this specific error case
     * @param ko    the test to be performed in case the API response is failure and the predicate is satisfied
     * @return      this class instance to chain more actions
     */
    public ApiAssertion<R> onFailure(Predicate<R> p, Consumer<R> ko) {
        failureAssertionMap.put(p, ko);

        return this;
    }

    private void testOk(R response) {
        Optional.of(response)
                .filter(r -> isSuccessfulPredicate.test(r))
                .ifPresent(r -> successAssertions.accept(r));
    }

    private void testConditionalKo(R response) {
        failureAssertionMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().test(response))
                .forEach(entry -> entry.getValue().accept(response));
    }

    private void testKo(R response) {
        Optional.of(response)
                .filter(r -> !isSuccessfulPredicate.test(r) && !Objects.isNull(failureAssertions) && failureAssertionMap.size() == 0)
                .ifPresent(r -> failureAssertions.accept(r));

        Optional.of(response)
                .filter(r -> !isSuccessfulPredicate.test(r) && Objects.isNull(failureAssertions) && failureAssertionMap.size() > 0)
                .ifPresent(this::testConditionalKo);
    }

    private void testOkKo(R response) {
        testOk(response);
        testKo(response);
    }

    /**
     * Perform the API response test
     *
     * @throws Exception    exceptions are thrown in these cases:
     *
     *                      <ul>
     *                          <li>the is successful check isn't defined</li>
     *                          <li>no success or failure assertions are defined</li>
     *                          <li>both simple and conditional on failure assertions are defined</li>
     *                      </ul>
     */
    public void test() throws Exception {
        Optional.ofNullable(isSuccessfulPredicate)
                .orElseThrow(() -> new Exception("Define at least API predicate"));

        if (Objects.isNull(successAssertions) && Objects.isNull(failureAssertions) && failureAssertionMap.size() == 0) {
            throw new Exception("Define at least API onSuccess or onFailure assertions");
        }

        if (!Objects.isNull(failureAssertions) && failureAssertionMap.size() > 0) {
            throw new Exception("Define only simple or conditional failure assertions");
        }

        // perform success and failure assertions
        if (!Objects.isNull(successAssertions) && (!Objects.isNull(failureAssertions) || failureAssertionMap.size() > 0)) {
            testOkKo(response);
        }

        // perform only success assertions
        if (!Objects.isNull(successAssertions) && Objects.isNull(failureAssertions) && failureAssertionMap.size() == 0) {
            testOk(response);
        }

        // perform only failure assertions
        if (Objects.isNull(successAssertions) && (!Objects.isNull(failureAssertions) || failureAssertionMap.size() > 0)) {
            testKo(response);
        }
    }
}
