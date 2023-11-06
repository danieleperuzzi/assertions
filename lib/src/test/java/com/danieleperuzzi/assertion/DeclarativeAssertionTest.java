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
