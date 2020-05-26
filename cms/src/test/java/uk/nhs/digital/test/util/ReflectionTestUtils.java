package uk.nhs.digital.test.util;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.ReflectionUtils;

import java.time.Clock;

import static org.apache.commons.lang3.reflect.FieldUtils.getDeclaredField;
import static uk.nhs.digital.test.util.ExceptionTestUtils.wrapCheckedException;

public class ReflectionTestUtils {

    @SuppressWarnings("unchecked")
    public static <T> T readField(final Object targetObject, final String fieldName) {
        return (T) wrapCheckedException(()-> FieldUtils.readField(targetObject, fieldName, true));
    }

    public static <T> void setField(final Object target, final String fieldName, final Object value) {
        wrapCheckedException(() -> getDeclaredField(target.getClass(), fieldName, true).set(target, value));
    }
}
