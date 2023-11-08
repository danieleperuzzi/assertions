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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Consumer;

public class ApiAssertion<R> {

    private R response;
    private Predicate<R> p;
    private Consumer<R> ok;
    private Consumer<R> ko;

    public ApiAssertion(R response) {
        this.response = response;
        this.p = null;
        this.ok = null;
        this.ko = null;
    }

    public ApiAssertion<R> isSuccessful(Predicate<R> p) {
        this.p = p;

        return this;
    }

    public ApiAssertion<R> onSuccess(Consumer<R> ok) {
        this.ok = ok;

        return this;
    }

    public ApiAssertion<R> onFailure(Consumer<R> ko) {
        this.ko = ko;

        return this;
    }

    private void testOk(R response) {
        Optional.of(response)
                .filter(r -> p.test(r))
                .ifPresent(r -> ok.accept(r));
    }

    private void testKo(R response) {
        Optional.of(response)
                .filter(r -> !p.test(r))
                .ifPresent(r -> ko.accept(r));
    }

    private void testOkKo(R response) {
        Optional.of(response)
                .filter(r -> p.test(r))
                .ifPresentOrElse(r -> ok.accept(r), () -> ko.accept(response));
    }

    public void test() throws Exception {
        Optional.ofNullable(p)
                .orElseThrow(() -> new Exception("Define at least API predicate"));

        if (Objects.isNull(ok) && Objects.isNull(ko)) {
            throw new Exception("Define at least API onSuccess or onFailure assertions");
        }

        if (!Objects.isNull(ok) && !Objects.isNull(ko)) {
            testOkKo(response);
        }

        if (!Objects.isNull(ok) && Objects.isNull(ko)) {
            testOk(response);
        }

        if (Objects.isNull(ok) && !Objects.isNull(ko)) {
            testKo(response);
        }
    }
}
