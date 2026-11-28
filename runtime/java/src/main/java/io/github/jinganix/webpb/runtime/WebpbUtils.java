/*
 * Copyright (c) 2020 The Webpb Authors, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/jinganix/webpb
 */

package io.github.jinganix.webpb.runtime;

import static org.springframework.util.StringUtils.hasLength;

import io.github.jinganix.webpb.commons.SegmentGroup;
import io.github.jinganix.webpb.commons.UrlSegment;
import io.github.jinganix.webpb.runtime.common.JacksonConfig;
import io.github.jinganix.webpb.runtime.common.MessageContext;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** Utilities for webpb java runtime. */
public class WebpbUtils {

  private record ContextKey(Class<?> type, String method, String context, String path) {}

  private static final Map<ContextKey, MessageContext> contextCache = new ConcurrentHashMap<>();

  private static final ObjectMapper urlObjectMapper = JacksonConfig.createUrlObjectMapper();

  private static final ObjectMapper transportMapper = JacksonConfig.createTransportObjectMapper();

  private WebpbUtils() {}

  /**
   * Create an {@link ObjectMapper} for formatting.
   *
   * @return {@link ObjectMapper}
   */
  public static ObjectMapper createUrlObjectMapper() {
    return JacksonConfig.createUrlObjectMapper();
  }

  /**
   * Create an {@link ObjectMapper} for transport serialization.
   *
   * @return {@link ObjectMapper}
   */
  public static ObjectMapper createTransportMapper() {
    return JacksonConfig.createTransportObjectMapper();
  }

  /**
   * Read WebpbMeta from a webpb message.
   *
   * @param type Class
   * @return WebpbMeta
   */
  public static WebpbMeta readWebpbMeta(Class<? extends WebpbMessage> type) {
    try {
      Field field = type.getDeclaredField("WEBPB_META");
      return (WebpbMeta) field.get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return null;
    }
  }

  /**
   * Format request url from API base url and {@link WebpbMeta}.
   *
   * @param message {@link WebpbMessage}
   * @return formatted url
   */
  public static String formatUrl(WebpbMessage message) {
    return formatUrl(urlObjectMapper, message);
  }

  /**
   * Format request url from API base url and {@link WebpbMeta}.
   *
   * @param objectMapper objectMapper to extract message properties
   * @param message {@link WebpbMessage}
   * @return formatted url
   */
  public static String formatUrl(ObjectMapper objectMapper, WebpbMessage message) {
    MessageContext context = getContext(message);
    String path = context.getPath();
    if (!context.getSegmentGroup().isEmpty()) {
      JsonNode data = objectMapper.convertValue(message, JsonNode.class);
      path = formatPath(data, context.getSegmentGroup(), null);
    }
    if (!path.startsWith("/")) {
      return path;
    }
    return joinPath(context.getContext(), path);
  }

  /**
   * Format request url from API base url and {@link WebpbMeta}.
   *
   * @param baseUrl {@link URL}
   * @param objectMapper objectMapper to extract message properties
   * @param message {@link WebpbMessage}
   * @return formatted url
   */
  public static String formatUrl(URL baseUrl, ObjectMapper objectMapper, WebpbMessage message) {
    if (baseUrl == null) {
      return formatUrl(objectMapper, message);
    }
    MessageContext context = getContext(message);
    if (!context.getPath().startsWith("/")) {
      throw new RuntimeException(
          String.format("Can not concat baseUrl: %s with path: %s", baseUrl, context.getPath()));
    }
    String path = context.getPath();
    if (!context.getSegmentGroup().isEmpty()) {
      JsonNode data = objectMapper.convertValue(message, JsonNode.class);
      path = formatPath(data, context.getSegmentGroup(), baseUrl.getQuery());
    }
    return concatUrl(baseUrl, joinPath(baseUrl.getPath(), context.getContext(), path));
  }

  private static String concatUrl(URL baseUrl, String file) {
    if ("/".equals(file)) {
      return baseUrl.toString();
    }
    try {
      URL url = new URL(baseUrl.getProtocol(), baseUrl.getHost(), baseUrl.getPort(), file, null);
      return url.toString();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test if a path is valid for webpb.
   *
   * @param path path to test
   * @return true if valid
   */
  public static boolean isValidPath(String path) {
    if (path == null) {
      return false;
    }
    if (!hasLength(path) || "/".equals(path)) {
      return true;
    }
    if (path.startsWith("/") && path.contains("//")) {
      return false;
    }
    try {
      String url = path.startsWith("/") ? "https://a" + path : path;
      new URL(url.replaceAll("[{}]", ""));
    } catch (MalformedURLException e) {
      return false;
    }
    return true;
  }

  /** Clear context cache. */
  public static void clearContextCache() {
    contextCache.clear();
  }

  /**
   * Get or create context for {@link WebpbMessage}.
   *
   * @param message {@link WebpbMessage}
   * @return {@link MessageContext}
   */
  public static MessageContext getContext(WebpbMessage message) {
    WebpbMeta meta = message.webpbMeta();
    ContextKey key;
    if (meta == null) {
      key = new ContextKey(message.getClass(), null, null, null);
    } else {
      String method = meta.getMethod();
      key =
          new ContextKey(
              message.getClass(),
              method == null ? null : method.toUpperCase(Locale.ROOT),
              meta.getContext(),
              meta.getPath());
    }
    MessageContext context =
        contextCache.computeIfAbsent(
            key,
            k -> {
              if (meta == null) {
                return MessageContext.NULL_CONTEXT;
              }
              if (!hasLength(meta.getMethod())) {
                return MessageContext.NULL_CONTEXT;
              }
              if (!isValidPath(meta.getPath())) {
                return MessageContext.NULL_CONTEXT;
              }
              return new MessageContext()
                  .setMethod(HttpMethod.valueOf(meta.getMethod().toUpperCase(Locale.ROOT)))
                  .setContext(meta.getContext())
                  .setPath(meta.getPath())
                  .setSegmentGroup(SegmentGroup.of(meta.getPath()));
            });
    if (context == MessageContext.NULL_CONTEXT) {
      throw new RuntimeException("Invalid meta method or meta path.");
    }
    return context;
  }

  private static String formatPath(JsonNode data, SegmentGroup group, String query) {
    StringBuilder builder = new StringBuilder();
    for (UrlSegment segment : group.getPathSegments()) {
      builder.append(segment.getPrefix());
      String value = resolve(data, segment.getValue());
      if (!hasLength(value)) {
        throw new RuntimeException(
            String.format("Path variable '%s' not found", segment.getValue()));
      }
      builder.append(UriUtils.encodePathSegment(value, StandardCharsets.UTF_8));
    }
    builder.append(group.getSuffix());
    String link = "?";
    if (StringUtils.hasLength(query)) {
      builder.append("?").append(query);
      link = "&";
    }
    for (UrlSegment segment : group.getQuerySegments()) {
      if (segment.isAccessor()) {
        String value = resolve(data, segment.getValue());
        if (hasLength(value) && !"null".equals(value)) {
          builder
              .append(link)
              .append(segment.getKey())
              .append("=")
              .append(UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8));
          link = "&";
        }
      } else if (StringUtils.hasLength(segment.getKey())) {
        builder.append(link).append(segment.getKey()).append("=").append(segment.getValue());
        link = "&";
      } else {
        builder.append(link).append(segment.getValue());
        link = "&";
      }
    }
    return builder.toString();
  }

  private static String resolve(JsonNode jsonNode, String accessor) {
    for (String part : accessor.split("\\.")) {
      jsonNode = jsonNode.get(part);
      if (jsonNode == null) {
        return null;
      }
    }
    return jsonNode.asText();
  }

  /**
   * Update a message extends from {@link WebpbMessage}.
   *
   * @param message message extends from {@link WebpbMessage}
   * @param variablesMap map of variables
   * @param <T> type extends from {@link WebpbMessage}
   * @return T
   */
  public static <T extends WebpbMessage> T updateMessage(
      T message, Map<String, List<String>> variablesMap) {
    if (CollectionUtils.isEmpty(variablesMap)) {
      return message;
    }

    WebpbMeta meta = message.webpbMeta();
    if (meta == null) {
      return message;
    }
    SegmentGroup group = SegmentGroup.of(meta.getPath());
    ObjectNode objectNode = urlObjectMapper.createObjectNode();
    for (UrlSegment segment : group.getSegments()) {
      if (!segment.isAccessor()) {
        continue;
      }
      String key = segment.getKey();
      String accessor = segment.getValue();
      List<String> values = variablesMap.get(StringUtils.hasLength(key) ? key : accessor);
      if (values == null || values.isEmpty()) {
        continue;
      }
      String[] accessors = accessor.split("\\.");
      ObjectNode targetNode = findNode(objectNode, accessors);
      String fieldName = accessors[accessors.length - 1];
      boolean collection = isCollectionField(message.getClass(), accessors);
      if (collection) {
        var arrayNode = urlObjectMapper.createArrayNode();
        for (String value : values) {
          if (value != null) {
            arrayNode.add(value);
          }
        }
        targetNode.set(fieldName, arrayNode);
      } else {
        if (values.size() > 1) {
          throw new IllegalArgumentException(
              "Duplicate query parameter '"
                  + (StringUtils.hasLength(key) ? key : accessor)
                  + "' for single-value field '"
                  + accessor
                  + "'");
        }
        String value = values.get(0);
        if (value != null) {
          targetNode.put(fieldName, value);
        }
      }
    }
    try {
      return urlObjectMapper.readerForUpdating(message).readValue(objectNode);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isCollectionField(Class<?> messageClass, String[] accessors) {
    Class<?> current = messageClass;
    for (int i = 0; i < accessors.length; i++) {
      Field field = findField(current, accessors[i]);
      if (field == null) {
        return false;
      }
      if (i == accessors.length - 1) {
        Class<?> type = field.getType();
        return Collection.class.isAssignableFrom(type) || type.isArray();
      }
      current = field.getType();
      if (current == null) {
        return false;
      }
    }
    return false;
  }

  private static Field findField(Class<?> clazz, String name) {
    Class<?> current = clazz;
    while (current != null && current != Object.class) {
      try {
        Field field = current.getDeclaredField(name);
        field.setAccessible(true);
        return field;
      } catch (NoSuchFieldException e) {
        current = current.getSuperclass();
      }
    }
    return null;
  }

  /**
   * Serialize a {@link WebpbMessage} to string.
   *
   * @param message {@link WebpbMessage}
   * @return value
   */
  public static String serialize(WebpbMessage message) {
    return transportMapper.writeValueAsString(message);
  }

  /**
   * Deserialize a {@link WebpbMessage} from string value.
   *
   * @param value string value
   * @param type message type
   * @param <T> type
   * @return message
   */
  public static <T extends WebpbMessage> T deserialize(String value, Class<T> type) {
    return transportMapper.readValue(value, type);
  }

  private static ObjectNode findNode(ObjectNode objectNode, String[] accessors) {
    for (int i = 0; i < accessors.length - 1; i++) {
      String accessor = accessors[i];
      ObjectNode subNode = (ObjectNode) objectNode.get(accessor);
      if (subNode == null) {
        subNode = urlObjectMapper.createObjectNode();
        objectNode.set(accessor, subNode);
      }
      objectNode = subNode;
    }
    return objectNode;
  }

  private static String joinPath(String... segments) {
    if (segments == null || segments.length == 0) {
      return "/";
    }
    StringBuilder builder = new StringBuilder();
    for (String segment : segments) {
      segment = (segment == null) ? "" : segment;
      trimSlash(builder);
      if (segment.startsWith("/")) {
        builder.append(segment);
      } else {
        builder.append("/").append(segment);
      }
    }
    trimSlash(builder);
    return URI.create(builder.toString()).normalize().toString();
  }

  private static void trimSlash(StringBuilder builder) {
    if (builder.length() > 0 && builder.charAt(builder.length() - 1) == '/') {
      builder.deleteCharAt(builder.length() - 1);
    }
  }
}
