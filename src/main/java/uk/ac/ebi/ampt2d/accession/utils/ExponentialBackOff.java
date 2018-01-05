/*
 *
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ampt2d.accession.utils;

/**
 * Lambda functor to execute a function with a exponential backoff
 */
public final class ExponentialBackOff {

    private static final int DEFAULT_TOTAL_ATTEMPTS = 7;
    private static final int DEFAULT_TIME_BASE = 1000;

    private ExponentialBackOff() {

    }

    public static void execute(Runnable function) {
        execute(function, DEFAULT_TOTAL_ATTEMPTS, DEFAULT_TIME_BASE);
    }

    public static void execute(Runnable function, int totalAttempts, int timeBase) {
        int firstValue = 0;
        int secondValue = 1;
        for (int attempt = 0; attempt < totalAttempts; attempt++) {
            try {
                function.run();
                return;
            } catch (Exception e) {
                doWait(attempt, timeBase);
                int nextValue = firstValue + secondValue;
                firstValue = secondValue;
                secondValue = nextValue;
            }
        }
        throw new RuntimeException("Exponential backoff max retries have been reached");
    }

    public static <T> T execute(ExecutorFunction<T> function) {
        return execute(function, DEFAULT_TOTAL_ATTEMPTS, DEFAULT_TIME_BASE);
    }

    public static <T> T execute(ExecutorFunction<T> function, int totalAttempts, int timeBase) {
        int firstValue = 0;
        int secondValue = 1;
        for (int attempt = 0; attempt < totalAttempts; attempt++) {
            try {
                return function.execute();
            } catch (Exception e) {
                doWait(attempt, timeBase);
                int nextValue = firstValue + secondValue;
                firstValue = secondValue;
                secondValue = nextValue;
            }
        }
        throw new RuntimeException("Exponential backoff max retries have been reached");
    }

    private static void doWait(int attempt, int timeBase) {
        try {
            Thread.sleep(attempt * timeBase);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
