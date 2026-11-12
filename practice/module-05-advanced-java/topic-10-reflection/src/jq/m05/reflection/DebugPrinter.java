package jq.m05.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugPrinter {

    public static String describe(Object o) {
        // TODO 1: get o.getClass() and loop over getDeclaredFields()
        // TODO 2: skip static fields (Modifier.isStatic(field.getModifiers()))
        // TODO 3: call field.setAccessible(true), then add field.getName() + "=" + field.get(o)
        //         field.get throws IllegalAccessException, so wrap it in try and catch (IllegalAccessException e).
        // TODO 4: return the simple class name + "{" + the parts joined with ", " + "}"
        return "";
    }

    public static List<String> methodNames(Class<?> type) {
        // TODO 5: keep the names of declared methods that are public, then sort them
        return new ArrayList<>();
    }

    public static Object create(String className) {
        // TODO 6: Class.forName(className), then getDeclaredConstructor().newInstance()
        // TODO 7: turn every failure into an IllegalArgumentException with a clear message
        return null;
    }

    public static Object callNoArg(Object target, String methodName) {
        // TODO 8: find the public method with getMethod(methodName) and call invoke(target)
        return null;
    }

    public static void main(String[] args) {
        System.out.println(describe(new Bike("B-1042", 80)));
        System.out.println(describe(new Station("Harbor", 12, 7)));
        System.out.println(methodNames(Bike.class));
        System.out.println(describe(create("jq.m05.reflection.Station")));
        System.out.println(callNoArg(new Bike("B-1042", 15), "isLow"));
    }
}
