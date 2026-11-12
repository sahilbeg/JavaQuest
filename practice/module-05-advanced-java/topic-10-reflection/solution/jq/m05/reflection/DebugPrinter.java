package jq.m05.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugPrinter {

    public static String describe(Object o) {
        Class<?> type = o.getClass();
        List<String> parts = new ArrayList<>();
        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            try {
                parts.add(field.getName() + "=" + field.get(o));
            } catch (IllegalAccessException e) {
                parts.add(field.getName() + "=?");
            }
        }
        return type.getSimpleName() + "{" + String.join(", ", parts) + "}";
    }

    public static List<String> methodNames(Class<?> type) {
        List<String> names = new ArrayList<>();
        for (Method method : type.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                names.add(method.getName());
            }
        }
        Collections.sort(names);
        return names;
    }

    public static Object create(String className) {
        try {
            Class<?> type = Class.forName(className);
            return type.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("No class called " + className);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(className + " has no constructor without parameters");
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not create " + className + ": " + e);
        }
    }

    public static Object callNoArg(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    target.getClass().getSimpleName() + " has no public method " + methodName + "()");
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Could not call " + methodName + ": " + e);
        }
    }

    public static void main(String[] args) {
        System.out.println(describe(new Bike("B-1042", 80)));
        System.out.println(describe(new Station("Harbor", 12, 7)));
        System.out.println(methodNames(Bike.class));
        System.out.println(describe(create("jq.m05.reflection.Station")));
        System.out.println(callNoArg(new Bike("B-1042", 15), "isLow"));
    }
}
