package io.github.jinganix.webpb.runtime.enumeration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** EnumValuesMap */
public class EnumValuesMap {

  private static final Map<Class<?>, Map<Object, Enumeration<?>>> valuesMap =
      new ConcurrentHashMap<>();

  /**
   * getValueMap
   *
   * @param clazz Class
   * @return Map
   */
  @SuppressWarnings("unchecked")
  public static Map<Object, Enumeration<?>> getValueMap(Class<?> clazz) {
    return valuesMap.computeIfAbsent(
        clazz,
        x -> {
          if (Enumeration.class.isAssignableFrom(clazz)) {
            Map<Object, Enumeration<?>> valueMap = new HashMap<>();
            Class<Enumeration<?>> enumClass = (Class<Enumeration<?>>) clazz;
            for (Enumeration<?> value : enumClass.getEnumConstants()) {
              valueMap.put(value.getValue(), value);
              valueMap.put(String.valueOf(value.getValue()), value);
              valueMap.put(String.valueOf(value), value);
            }
            return valueMap;
          }
          return null;
        });
  }
}
