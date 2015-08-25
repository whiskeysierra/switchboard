package org.zalando.circuit;

/*
 * ⁣​
 * Circuit
 * ⁣⁣
 * Copyright (C) 2015 Zalando SE
 * ⁣⁣
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
 * ​⁣
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public final class OrdinalsTest {
    
    private final int value;
    private final String ordinal;

    public OrdinalsTest(int value, String ordinal) {
        this.value = value;
        this.ordinal = ordinal;
    }

    @Parameters(name = "{0} -> {1}")
    public static Iterable<Object[]> data() {

        return Arrays.asList(new Object[][]{
                {0, "th"},
                {1, "st"},
                {2, "nd"},
                {3, "rd"},
                {4, "th"},
                {5, "th"},
                {6, "th"},
                {7, "th"},
                {8, "th"},
                {9, "th"},
                {10, "th"},
                {11, "th"},
                {12, "th"},
                {13, "th"},
                {14, "th"},
                {15, "th"},
                {16, "th"},
                {17, "th"},
                {18, "th"},
                {19, "th"},
                {20, "th"},
        });
    }

    @Test
    public void test() {
        assertThat(Ordinals.valueOf(value), is(ordinal));
    }

}