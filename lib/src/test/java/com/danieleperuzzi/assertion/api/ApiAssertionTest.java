package com.danieleperuzzi.assertion.api;

import com.danieleperuzzi.assertion.util.ApiResponseMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.danieleperuzzi.assertion.DeclarativeAssertion.test;

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
    }

    @Test
    @DisplayName("check onFailure")
    public void checkOnFailure() {
        AtomicInteger testKo = new AtomicInteger(0);

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
    @DisplayName("check multiple error cases")
    public void checkMultipleErrorCases() {
        AtomicInteger testOk = new AtomicInteger(0);
        AtomicInteger testKo400 = new AtomicInteger(0);
        AtomicInteger testKo401 = new AtomicInteger(0);

        Consumer<ApiResponseMock> testError = (response) -> {
            test(response)
                    .when(r -> r.getStatus() == 400)
                    .then(r -> testKo400.incrementAndGet());

            test(response)
                    .when(r -> r.getStatus() == 401)
                    .then(r -> testKo401.incrementAndGet());
        };

        try {
            new ApiAssertion<>(apiResponseKo) // check response KO
                    .isSuccessful(r -> r.getStatus() == 200)
                    .onSuccess(r -> testOk.incrementAndGet())
                    .onFailure(testError)
                    .test();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(0, testOk.get());
        assertEquals(1, testKo400.get());
        assertEquals(0, testKo401.get());
    }
}
