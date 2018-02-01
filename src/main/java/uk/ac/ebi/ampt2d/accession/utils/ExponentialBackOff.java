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
public interface ExponentialBackOff {

    int DEFAULT_TOTAL_ATTEMPTS = 7;
    int DEFAULT_TIME_BASE = 1000;

    static void execute(Runnable function) {
        execute(function, DEFAULT_TOTAL_ATTEMPTS, DEFAULT_TIME_BASE);
    }

    static void execute(Runnable function, int totalAttempts, int timeBase) {
        int previousValue = 0;
        int currentValue = 1;
        for (int attempt = 0; attempt < totalAttempts; attempt++) {
            try {
                function.run();
                return;
            } catch (Exception e) {
                doWait(currentValue, timeBase);
                int nextValue = previousValue + currentValue;
                previousValue = currentValue;
                currentValue = nextValue;
            }
        }
        throw new RuntimeException("Exponential backoff max retries have been reached");
    }

    static <T> T execute(ExecutorFunction<T> function) {
        return execute(function, DEFAULT_TOTAL_ATTEMPTS, DEFAULT_TIME_BASE);
    }

    static <T> T execute(ExecutorFunction<T> function, int totalAttempts, int timeBase) {
        int previousValue = 0;
        int currentValue = 1;
        for (int attempt = 0; attempt < totalAttempts; attempt++) {
            try {
                return function.execute();
            } catch (Exception e) {
                doWait(currentValue, timeBase);
                int nextValue = previousValue + currentValue;
                previousValue = currentValue;
                currentValue = nextValue;
            }
        }
        throw new RuntimeException("Exponential backoff max retries have been reached");
    }

    static void doWait(int valueInTheSeries, int timeBase) {
        try {
            Thread.sleep(valueInTheSeries * timeBase);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
