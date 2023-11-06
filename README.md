# assertion
Assertion helps in performing assertions in testing projects also on api responses

## Prerequisites
- Java 17+
- Gradle 8+ (gradle wrapper included)

## Installation
Using Gradle

```
dependencies {
    implementation 'com.danieleperuzzi:assertion:1.0.0'
}
```

## Build library
To build assertion library just run gradle build task:

on Linux
```
./gradlew build
```

on Windows
```
./gradlew.bat build
```

you can also build the library using your machine gradle installation but please be sure gradle version is at least 8.

## Launch tests
To launch the builtin test suite for assertion library just run gradle test task:

on Linux
```
./gradlew test
```

on Windows
```
./gradlew.bat test
```

you can also test the library using your machine gradle installation but please be sure gradle version is at least 8.

## DeclarativeAssertion

A more fashionable way to perform assertions if the condition is met or not

```java
import static com.danieleperuzzi.assertion.DeclarativeAssertion.test; // used for readability

ApiResponse apiResponse;

test(apiResponse)
        .when(response -> response.getStatus() == 200)
        .then(response -> assertEquals("OK", response.getResponseBody().getStatus()));

test(apiResponse)
        .when(response -> response.getStatus() == 400)
        .then(response -> assertEquals("Error", response.getResponseBody().getStatus()));
```

suppose api status is ```200``` then only the first assertion is performed

## ApiAssertion

Since every API has error handling it responds in different ways depending it is successful or failure and the structure 
of the response may change depending on that.

```ApiAssertion``` helps in performing right checks when API is succesful or not

```java
ApiResponse apiResponse;

try {
    new ApiAssertion<>(apiResponse)
        .isSuccessful(response -> response.getStatus() == 200)
        .onSuccess(response -> assertEquals("OK", response.getResponseBody().getStatus()))
        .onFailure(response -> assertEquals("KO", response.getResponseBody().getStatus()))
        .test();
} catch (Exception e) {
    e.printStackTrace();
}
```

depending on the api status then rights checks are performed

In case we are dealing with multiple error cases we can use ```DeclarativeAssertion``` to elegantly handle them

```java
ApiResponse apiResponse;

Consumer<ApiResponse> testError = (response) -> {
    test(response)
        .when(r -> r.getStatus() == 400)
        .then(r -> assertEquals("Error 400", r.getResponseBody().getStatus()));

    test(response)
        .when(r -> r.getStatus() == 401)
        .then(r -> assertEquals("Error 401", r.getResponseBody().getStatus()));
};

try {
    new ApiAssertion<>(apiResponse)
        .isSuccessful(response -> response.getStatus() == 200)
        .onSuccess(response -> assertEquals("OK", response.getResponseBody().getStatus()))
        .onFailure(testError)
        .test();
} catch (Exception e) {
    e.printStackTrace();
}
```

whenever api status is failure then the right check is performed depending on its status code
