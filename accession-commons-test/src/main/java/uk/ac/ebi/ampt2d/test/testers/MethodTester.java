/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.ampt2d.test.testers;

import uk.ac.ebi.ampt2d.test.utils.VoidThrowingSupplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class MethodTester implements IMethodTester{

    private Exception exception;

    public MethodTester(VoidThrowingSupplier functionCall) {
        try {
            functionCall.get();
        } catch (Exception e) {
            this.exception = e;
        }
    }

    public void assertNoException() {
        if (exception != null) {
            fail("Unexpected exception thrown '" + exception.getClass().getName() + "'");
        }
    }

    public void assertThrow(Class<? extends Throwable> exception) {
        assertNotNull("No exception was thrown", exception);
        assertThatThrownBy(() -> {
            throw this.exception;
        }).isInstanceOf(Exception.class);
    }

}
