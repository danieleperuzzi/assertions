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

import com.danieleperuzzi.assertion.util.ApiResponseMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.danieleperuzzi.assertion.DeclarativeAssertion.test;

@ExtendWith(MockitoExtension.class)
public class DeclarativeAssertionTest {

    private static ApiResponseMock apiResponseOk;

    @BeforeAll
    public static void staticSetUp() {
        apiResponseOk = new ApiResponseMock(200, "{\"status\": \"OK\", \"message\": \"response is successful\"}");
    }

    @Test
    @DisplayName("predicate is success")
    public void successPredicate() {
        AtomicInteger testOk = new AtomicInteger(0);

        test(apiResponseOk)
                .when(response -> response.getStatus() == 200)
                .then(response -> testOk.incrementAndGet());

        assertEquals(1, testOk.get());
    }

    @Test
    @DisplayName("predicate is failure")
    public void failurePredicate() {
        AtomicInteger testKo = new AtomicInteger(0);

        test(apiResponseOk)
                .when(response -> response.getStatus() == 400)
                .then(response -> testKo.incrementAndGet());

        assertEquals(0, testKo.get());
    }
}
