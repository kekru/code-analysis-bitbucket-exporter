package de.kekru.codeanalysisbb.config;

import de.kekru.codeanalysisbb.serviceregistry.Service;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

@Service
public class PropertyLoaderService {

  public void applyConfigProperties(final Object target, final Map<String, String> keyValues,
      final String separator, final String startWithFilter) {

    final String startWithFilterAndSeparator = startWithFilter + separator;

    keyValues.entrySet()
        .stream()
        .filter(
            entry -> StringUtils.startsWithIgnoreCase(entry.getKey(), startWithFilterAndSeparator))
        .forEach(entry -> applyConfigProperties(
            target,
            StringUtils.removeStartIgnoreCase(entry.getKey(), startWithFilterAndSeparator),
            entry.getValue(),
            separator));
  }

  public void applyConfigProperties(final Object target, final String fullKey, final String value,
      final String separator) {

    Map<String, Field> fields = FieldUtils.getAllFieldsList(target.getClass())
        .stream()
        .collect(Collectors.toMap(Field::getName, Function.identity()));

    int indexFirstSeparator = StringUtils.indexOf(fullKey, separator);

    if (indexFirstSeparator >= 0) {
      // separator in key -> setting the config on a sub-object
      String currentKey = StringUtils.substringBefore(fullKey, separator);
      String restOfString = StringUtils.substringAfter(fullKey, separator);

      try {
        Field field = fields.get(currentKey);
        field.setAccessible(true);

        if (field == null) {
          throw new RuntimeException(
              String.format("Field %s not found on type %s", currentKey, target.getClass()));
        }

        Object propertyValueOfTarget = field.get(target);
        if (propertyValueOfTarget == null) {
          propertyValueOfTarget = instantiate(field.getType());
          field.set(target, propertyValueOfTarget);
        }

        applyConfigProperties(propertyValueOfTarget, restOfString, value, separator);

      } catch (IllegalAccessException e) {
        throw new RuntimeException(String.format("Could not get property %s of %s",
            currentKey, target.getClass().getName()), e);
      }

    } else {

      Field field = fields.get(fullKey);
      field.setAccessible(true);
      try {
        field.set(target, convertType(field.getType(), value));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(String.format("Could not set %s.%s to value: %s",
            field.getDeclaringClass().getName(), field.getName(), value), e);
      }
    }
  }


  private Object instantiate(Class<?> type) {
    if (type.isAssignableFrom(List.class)) {
      return new LinkedList();
    }

    if (type.isAssignableFrom(Set.class)) {
      return new LinkedHashSet();
    }

    if (type.isAssignableFrom(Collection.class)) {
      return new LinkedList();
    }

    try {
      Constructor<?> c = type.getConstructor();
      return c.newInstance();
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(
          "Could not instantiate constructor without prameters: " + type.getName(), e);
    }
  }

  private Object convertType(final Class<?> type, final String value) {
    if (type == boolean.class || type == Boolean.class) {
      return Boolean.parseBoolean(value);
    }

    if (type == int.class || type == Integer.class) {
      return Integer.parseInt(value);
    }

    if (type == String.class) {
      return value;
    }

    if (type == List.class) {
      return Stream.of(StringUtils.split(value, ",")).map(StringUtils::trimToEmpty)
          .collect(Collectors.toList());
    }

    throw new RuntimeException("Unhandled type: " + type + " for value: " + value);
  }
}
