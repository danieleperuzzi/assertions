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

package com.danieleperuzzi.assertion.api;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Consumer;

public class ApiAssertion<R> {

    private R response;
    private Predicate<R> isSuccessfulPredicate;
    private Consumer<R> successAssertions;
    private Consumer<R> failureAssertions;
    private Map<Predicate<R>, Consumer<R>> failureAssertionMap = new HashMap<>();

    public ApiAssertion(R response) {
        this.response = response;
        this.isSuccessfulPredicate = null;
        this.successAssertions = null;
        this.failureAssertions = null;
    }

    public ApiAssertion<R> isSuccessful(Predicate<R> p) throws Exception {
        if (!Objects.isNull(isSuccessfulPredicate)) {
            throw new Exception("Define only one isSuccessful predicate");
        }

        isSuccessfulPredicate = p;

        return this;
    }

    public ApiAssertion<R> onSuccess(Consumer<R> ok) throws Exception {
        if (!Objects.isNull(successAssertions)) {
            throw new Exception("Define only one onSuccess assertion");
        }

        successAssertions = ok;

        return this;
    }

    public ApiAssertion<R> onFailure(Consumer<R> ko) throws Exception {
        if (!Objects.isNull(failureAssertions)) {
            throw new Exception("Define only one onFailure assertion");
        }

        failureAssertions = ko;

        return this;
    }

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
