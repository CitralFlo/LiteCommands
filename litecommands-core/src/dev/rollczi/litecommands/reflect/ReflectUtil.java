package dev.rollczi.litecommands.reflect;

import dev.rollczi.litecommands.LiteCommandsException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ReflectUtil {

    private static final Map<Class<?>, Class<?>> WRAPPERS_TO_PRIMITIVES = new HashMap<>();

    static {
        WRAPPERS_TO_PRIMITIVES.put(Boolean.class, boolean.class);
        WRAPPERS_TO_PRIMITIVES.put(Byte.class, byte.class);
        WRAPPERS_TO_PRIMITIVES.put(Character.class, char.class);
        WRAPPERS_TO_PRIMITIVES.put(Double.class, double.class);
        WRAPPERS_TO_PRIMITIVES.put(Float.class, float.class);
        WRAPPERS_TO_PRIMITIVES.put(Integer.class, int.class);
        WRAPPERS_TO_PRIMITIVES.put(Long.class, long.class);
        WRAPPERS_TO_PRIMITIVES.put(Short.class, short.class);
        WRAPPERS_TO_PRIMITIVES.put(Void.class, void.class);
    }

    private ReflectUtil() {}

    public static boolean instanceOf(Object obj, Class<?> instanceOf) {
        return instanceOf(obj.getClass(), instanceOf);
    }

    public static boolean instanceOf(Class<?> clazz, Class<?> instanceOf) {
        if (instanceOf.isAssignableFrom(clazz)) {
            return true;
        }

        if (clazz.isPrimitive()) {
            return WRAPPERS_TO_PRIMITIVES.get(instanceOf) == clazz;
        }

        if (instanceOf.isPrimitive()) {
            return WRAPPERS_TO_PRIMITIVES.get(clazz) == instanceOf;
        }

        return false;
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field declaredField = clazz.getDeclaredField(fieldName);

            declaredField.setAccessible(true);
            return declaredField;
        }
        catch (NoSuchFieldException exception) {
            if (clazz.getSuperclass() != null) {
                try {
                    return getField(clazz.getSuperclass(), fieldName);
                }
                catch (LiteCommandsException ignored) { }
            }

            throw new LiteCommandsReflectException("Unable to find field " + fieldName + " in " + clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getClass(String className) {
        try {
            return (Class<? extends T>) Class.forName(className, true, ReflectUtil.class.getClassLoader());
        }
        catch (ClassNotFoundException exception) {
            throw new LiteCommandsReflectException("Unable to find class " + className, exception);
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            Method declaredMethod = clazz.getDeclaredMethod(methodName, params);

            declaredMethod.setAccessible(true);
            return declaredMethod;
        }
        catch (NoSuchMethodException exception) {
            if (clazz.getSuperclass() != null) {
                try {
                    return getMethod(clazz.getSuperclass(), methodName, params);
                }
                catch (LiteCommandsException ignored) {}
            }

            throw new LiteCommandsReflectException(String.format("Unable to find method %s(%s) in %s", methodName, Arrays.toString(params), clazz));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Method method, Object instance, Object... params) {
        try {
            return (T) method.invoke(instance, params);
        }
        catch (Exception exception) {
            throw new LiteCommandsReflectException("Unable to invoke method " + method.getName() + " in " + instance.getClass(), exception);
        }
    }

    public static <T> T invokeStaticMethod(Method method, Object... params) {
        return invokeMethod(method, null, params);
    }

    public static void setValue(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        }
        catch (Exception exception) {
            throw new LiteCommandsReflectException("Unable to set field " + field.getName() + " in " + instance.getClass(), exception);
        }
    }

    public static void setStaticValue(Field field, Object value) {
        setValue(field, null, value);
    }

    public static Object getValue(Field field, Object instance) {
        try {
            return field.get(instance);
        }
        catch (Exception exception) {
            throw new LiteCommandsReflectException("Unable to get field " + field.getName() + " in " + instance.getClass(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFromField(Object instance, String fieldName) {
        return (T) getValue(getField(instance.getClass(), fieldName), instance);
    }

    public static <T> T getFromMethod(Object instance, String methodName, Object... params) {
        return invokeMethod(getMethod(instance.getClass(), methodName, getClasses(params)), instance, params);
    }

    private static Class<?>[] getClasses(Object[] params) {
        Class<?>[] classes = new Class<?>[params.length];

        for (int i = 0; i < params.length; i++) {
            classes[i] = params[i].getClass();
        }

        return classes;
    }


}
