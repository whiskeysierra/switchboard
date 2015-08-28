package org.zalando.switchboard;

/*
 * ⁣​
 * Switchboard
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

import java.util.List;

final class Broadcast implements DeliveryMode {

    public static final DeliveryMode INSTANCE = new Broadcast();

    private Broadcast() {
        // singleton
    }

    @Override
    public <S, T> List<Answer<S, T, ?>> distribute(final List<Answer<S, T, ?>> deliveries) {
        return deliveries;
    }

    @Override
    public String toString() {
        return "broadcast()";
    }

}
