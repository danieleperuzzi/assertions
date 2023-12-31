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

import io.github.danieleperuzzi.assertion.util.ApiResponseMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ApiAssertionTest {

    private static ApiResponseMock apiResponseOk;
    private static ApiResponseMock apiResponseKo;

    @BeforeAll
    public static void staticSetUp() {
        apiResponseOk = new ApiResponseMock(200, "{\"status\": \"OK\", \"message\": \"response is successful\"}");
        apiResponseKo = new ApiResponseMock(400, "{\"status\": \"KO\", \"message\": \"response is failure\"}");
    }

    @Test
    @DisplayName("define API predicate exception")
    public void defineApiPredicateException() {
        Exception exception = assertThrows(Exception.class, () -> {
            new ApiAssertion<>(apiResponseOk)
                    .test();
        });

        String expectedMessage = "Define at least API predicate";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("define API onSuccess or onFailure exception")
    public void defineApiOnSuccessOnFailureException() {
        Exception exception = assertThrows(Exception.class, () -> {
            new ApiAssertion<>(apiResponseOk)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .test();
        });

        String expectedMessage = "Define at least API onSuccess or onFailure assertions";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("define only one API isSuccessful predicate exception")
    public void defineOnlyOneApiIsSuccessfulException() {
        Exception exception = assertThrows(Exception.class, () -> {
            new ApiAssertion<>(apiResponseOk)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .isSuccessful(r -> r.getStatus() == 204)
                    .test();
        });

        String expectedMessage = "Define only one isSuccessful predicate";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("define only one API onSuccess assertion exception")
    public void defineOnlyOneApiOnSuccessException() {
        AtomicInteger testOk = new AtomicInteger(0);

        Exception exception = assertThrows(Exception.class, () -> {
            new ApiAssertion<>(apiResponseOk)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .onSuccess(r -> testOk.incrementAndGet())
                    .test();
        });

        String expectedMessage = "Define only one onSuccess assertion";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("define only one API onFailure assertion exception")
    public void defineOnlyOneApiOnFailureException() {
        AtomicInteger testKo = new AtomicInteger(0);

        Exception exception = assertThrows(Exception.class, () -> {
            new ApiAssertion<>(apiResponseOk)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onFailure(r -> testKo.incrementAndGet())
                    .onFailure(r -> testKo.incrementAndGet())
                    .test();
        });

        String expectedMessage = "Define only one onFailure assertion";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("define only one API onFailure assertion or conditional onFailure assertions exception")
    public void defineOnlyOneApiOnFailureOrConditionalOnFailureException() {
        AtomicInteger testKo = new AtomicInteger(0);

        Exception exception = assertThrows(Exception.class, () -> {
            new ApiAssertion<>(apiResponseOk)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onFailure(r -> testKo.incrementAndGet())
                    .onFailure(r -> r.getStatus() == 400, r -> testKo.incrementAndGet())
                    .test();
        });

        String expectedMessage = "Define only simple or conditional failure assertions";
        String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("check onSuccess")
    public void checkOnSuccess() {
        AtomicInteger testOk = new AtomicInteger(0);

        try {
            new ApiAssertion<>(apiResponseOk)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testOk.get());

        try {
            new ApiAssertion<>(apiResponseKo)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testOk.get());
    }

    @Test
    @DisplayName("check onFailure")
    public void checkOnFailure() {
        AtomicInteger testKo = new AtomicInteger(0);

        try {
            new ApiAssertion<>(apiResponseOk)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onFailure(r -> testKo.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(0, testKo.get());

        try {
            new ApiAssertion<>(apiResponseKo)
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onFailure(r -> testKo.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testKo.get());
    }

    @Test
    @DisplayName("check conditional onFailure")
    public void checkConditionalOnFailure() {
        AtomicInteger testKo400 = new AtomicInteger(0);
        AtomicInteger testKo401 = new AtomicInteger(0);

        try {
            new ApiAssertion<>(apiResponseOk) // check response KO
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onFailure(r -> r.getStatus() == 400, r -> testKo400.incrementAndGet())
                    .onFailure(r -> r.getStatus() == 401, r -> testKo401.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(0, testKo400.get());
        assertEquals(0, testKo401.get());

        try {
            new ApiAssertion<>(apiResponseKo) // check response KO
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onFailure(r -> r.getStatus() == 400, r -> testKo400.incrementAndGet())
                    .onFailure(r -> r.getStatus() == 401, r -> testKo401.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testKo400.get());
        assertEquals(0, testKo401.get());
    }

    @Test
    @DisplayName("check onSuccess and onFailure")
    public void checkOnSuccessOnFailure() {
        AtomicInteger testOk = new AtomicInteger(0);
        AtomicInteger testKo = new AtomicInteger(0);

        try {
            new ApiAssertion<>(apiResponseOk) // check response OK
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .onFailure(r -> testKo.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testOk.get());
        assertEquals(0, testKo.get());


        try {
            new ApiAssertion<>(apiResponseKo) // check response KO
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .onFailure(r -> testKo.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testOk.get());
        assertEquals(1, testKo.get());
    }

    @Test
    @DisplayName("check onSuccess and conditional onFailure")
    public void checkOnSuccessConditionalOnFailure() {
        AtomicInteger testOk = new AtomicInteger(0);
        AtomicInteger testKo400 = new AtomicInteger(0);
        AtomicInteger testKo401 = new AtomicInteger(0);

        try {
            new ApiAssertion<>(apiResponseOk) // check response KO
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .onFailure(r -> r.getStatus() == 400, r -> testKo400.incrementAndGet())
                    .onFailure(r -> r.getStatus() == 401, r -> testKo401.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testOk.get());
        assertEquals(0, testKo400.get());
        assertEquals(0, testKo401.get());

        try {
            new ApiAssertion<>(apiResponseKo) // check response KO
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .onFailure(r -> r.getStatus() == 400, r -> testKo400.incrementAndGet())
                    .onFailure(r -> r.getStatus() == 401, r -> testKo401.incrementAndGet())
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, testOk.get());
        assertEquals(1, testKo400.get());
        assertEquals(0, testKo401.get());
    }
}
