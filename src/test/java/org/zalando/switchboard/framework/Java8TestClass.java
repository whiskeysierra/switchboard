package org.zalando.switchboard.framework;

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

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

public class Java8TestClass extends TestClass {

    public Java8TestClass(final Class<?> type) {
        super(type);
    }

    private Class<?> getType() {
        try {
            final Field field = TestClass.class.getDeclaredField("clazz");
            field.setAccessible(true);
            return (Class<?>) field.get(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    protected void scanAnnotatedMembers(final Map<Class<? extends Annotation>, List<FrameworkMethod>> methodsForAnnotations,
            final Map<Class<? extends Annotation>, List<FrameworkField>> fieldsForAnnotations) {
        final Class<?> type = getType();

        for (final Method eachMethod : type.getMethods()) {
                addToAnnotationLists(new FrameworkMethod(eachMethod), methodsForAnnotations);
            }
            // ensuring fields are sorted to make sure that entries are inserted
            // and read from fieldForAnnotations in a deterministic order
            for (final Field eachField : getSortedDeclaredFields(type)) {
                addToAnnotationLists(newFrameworkField(eachField), fieldsForAnnotations);
            }
    }

    private Method[] getDeclaredMethods(final Class<?> clazz) {
        return clazz.getMethods();
    }

    private FrameworkField newFrameworkField(final Field eachField) {
        try {
            final Constructor<FrameworkField> constructor = FrameworkField.class.getDeclaredConstructor(Field.class);
            constructor.setAccessible(true);
            return constructor.newInstance(eachField);
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new AssertionError(e);
        }
    }

    private static List<Class<?>> getSuperClasses(final Class<?> testClass) {
        final ArrayList<Class<?>> results = new ArrayList<>();
        Class<?> current = testClass;
        while (current != null) {
            results.add(current);
            current = current.getSuperclass();
        }
        return results;
    }

    private static Field[] getSortedDeclaredFields(final Class<?> clazz) {
        final Field[] declaredFields = clazz.getDeclaredFields();
        Arrays.sort(declaredFields, comparing(Field::getName));
        return declaredFields;
    }

}
