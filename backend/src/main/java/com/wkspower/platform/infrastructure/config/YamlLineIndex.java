package com.wkspower.platform.infrastructure.config;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dotted-path → 1-based line number map, built by a secondary token-streaming pass over the YAML
 * source. Jackson's YAML tree API does not carry source positions on the deserialised objects, so
 * we walk the token stream once to build the index and hand it to the validator alongside the
 * deserialised {@link RawCaseTypeConfig}.
 *
 * <p>Paths use JSON-Pointer-flavour notation minus the leading slash — e.g. {@code id}, {@code
 * fields[2].type}, {@code roles[0].permissions[1]}. Array indices are 0-based; line numbers are
 * 1-based (matches editor gutters).
 */
public final class YamlLineIndex {

  private static final YAMLFactory FACTORY = new YAMLFactory();

  private final Map<String, Integer> byPath;

  private YamlLineIndex(Map<String, Integer> byPath) {
    this.byPath = Map.copyOf(byPath);
  }

  /** Empty index — used when the YAML is unparseable and line capture is not available. */
  public static YamlLineIndex empty() {
    return new YamlLineIndex(Map.of());
  }

  public static YamlLineIndex of(Path file) throws IOException {
    try (InputStream in = Files.newInputStream(file)) {
      return of(in);
    }
  }

  public static YamlLineIndex of(InputStream in) throws IOException {
    Map<String, Integer> map = new HashMap<>();
    try (YAMLParser parser = FACTORY.createParser(in)) {
      Deque<Frame> stack = new ArrayDeque<>();
      // currentPath is assigned when a FIELD_NAME is consumed (object child) or when entering an
      // array element (computed from parent index). It is the path of the *next* value or
      // container. After the value or container is recorded, completion logic advances the
      // parent array index.
      String currentPath = null;

      JsonToken tok;
      while ((tok = parser.nextToken()) != null) {
        switch (tok) {
          case START_OBJECT -> {
            String prefix = childPrefix(stack, currentPath);
            if (prefix != null) {
              map.putIfAbsent(prefix, parser.currentTokenLocation().getLineNr());
            }
            stack.push(new Frame(prefix, false));
            currentPath = null;
          }
          case START_ARRAY -> {
            String prefix = childPrefix(stack, currentPath);
            if (prefix != null) {
              map.putIfAbsent(prefix, parser.currentTokenLocation().getLineNr());
            }
            stack.push(new Frame(prefix, true));
            currentPath = null;
          }
          case END_OBJECT, END_ARRAY -> {
            Frame closed = stack.isEmpty() ? null : stack.pop();
            // If closing inside an array, advance the parent's index.
            if (!stack.isEmpty() && stack.peek().isArray) {
              stack.peek().arrayIndex++;
            }
            // Reset any dangling field pointer left by the closed frame.
            currentPath = null;
            if (closed == null) {
              // defensive — unbalanced stream, shouldn't happen for well-formed YAML
            }
          }
          case FIELD_NAME -> {
            Frame top = stack.isEmpty() ? null : stack.peek();
            String parent = top == null ? null : top.prefix;
            String name = parser.currentName();
            currentPath = parent == null ? name : parent + "." + name;
          }
          case VALUE_STRING,
              VALUE_NUMBER_INT,
              VALUE_NUMBER_FLOAT,
              VALUE_TRUE,
              VALUE_FALSE,
              VALUE_NULL -> {
            Frame top = stack.isEmpty() ? null : stack.peek();
            String path = currentPath;
            if (path == null && top != null && top.isArray) {
              path =
                  top.prefix == null
                      ? "[" + top.arrayIndex + "]"
                      : top.prefix + "[" + top.arrayIndex + "]";
            }
            if (path != null) {
              map.putIfAbsent(path, parser.currentTokenLocation().getLineNr());
            }
            if (top != null && top.isArray) {
              top.arrayIndex++;
            }
            currentPath = null;
          }
          default -> {
            // no-op
          }
        }
      }
    }
    return new YamlLineIndex(map);
  }

  /** Returns the 1-based line of the value at {@code path} if known. */
  public Optional<Integer> lineOf(String path) {
    return Optional.ofNullable(byPath.get(path));
  }

  /**
   * Best-effort lookup — falls back to the line of the nearest ancestor path when the exact path is
   * absent (e.g. collection-level errors cite the collection line, not a missing child).
   */
  public Optional<Integer> lineOfOrNearest(String path) {
    Integer exact = byPath.get(path);
    if (exact != null) {
      return Optional.of(exact);
    }
    String p = path;
    while (true) {
      int dot = p.lastIndexOf('.');
      int bracket = p.lastIndexOf('[');
      int cut = Math.max(dot, bracket);
      if (cut <= 0) {
        return Optional.empty();
      }
      p = p.substring(0, cut);
      Integer hit = byPath.get(p);
      if (hit != null) {
        return Optional.of(hit);
      }
    }
  }

  /**
   * Computes the path of a child being entered. If the parent is an array, the child path is {@code
   * parent[i]} (ignoring {@code currentPath} since arrays have no field names). Otherwise {@code
   * currentPath} wins.
   */
  private static String childPrefix(Deque<Frame> stack, String currentPath) {
    Frame top = stack.peek();
    if (top != null && top.isArray) {
      String base = top.prefix == null ? "" : top.prefix;
      return base + "[" + top.arrayIndex + "]";
    }
    return currentPath;
  }

  private static final class Frame {
    final String prefix;
    final boolean isArray;
    int arrayIndex;

    Frame(String prefix, boolean isArray) {
      this.prefix = prefix;
      this.isArray = isArray;
    }
  }
}
