package backend;

import java.lang.reflect.Field;

public class ReflectionUtils {
    public static Field getFieldFromClassHierarchy(Class<?> cls, String fieldName) throws NoSuchFieldException {
        while (cls != null) {
            try {
                return cls.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // this is probably a bad way of doing it
                // if it throws an exception, it doesnt exist. we move up one and try again
                cls = cls.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
