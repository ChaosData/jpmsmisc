package trust.nccgroup;

import java.lang.reflect.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.module.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

public class UnsafeWrapper {
  private static String type = null;
  private static Object theUnsafe = null;

  private static Map<String,Map<String,Method>> methods = new HashMap<>();

  private UnsafeWrapper() { }

  public static String type() {
    return type;
  }

  private static final Map<Class<?>,Class<?>> boxmap = Stream.of(new Class<?>[][] {
    { Void.TYPE, Void.class },
    { Long.TYPE, Long.class },
    { Integer.TYPE, Integer.class },
    { Short.TYPE, Short.class },
    { Byte.TYPE, Byte.class },
    { Float.TYPE, Float.class },
    { Double.TYPE, Double.class },
    { Boolean.TYPE, Boolean.class },
    { Character.TYPE, Character.class },
  }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

  public static Object invoke(String method_name, Class<?>[] types, Object... args) {
    Map<String,Method> mm = methods.get(method_name);
    if (mm == null) {
      System.err.println("method name not found: " + method_name);
      return null;
    }
    String sig_str = Arrays.toString(types);
    Method m = mm.get(sig_str);
    if (m == null) {
      System.err.println("method sig not found: " + method_name + "(" + sig_str + ")");
      return null;
    }
    try {
      return m.invoke(theUnsafe, args);
    } catch (Throwable t) {
      t.printStackTrace();
      return null;
    }
  }

  public static boolean init(Object _theUnsafe) {
    try {
      theUnsafe = _theUnsafe;

      System.out.println(theUnsafe);
      Class<?> c = theUnsafe.getClass();
      type = c.toString();
      System.out.println(c);

      for (Method m : c.getDeclaredMethods()) {
        Map<String,Method> mm = methods.get(m.getName());
        if (mm == null) {
          mm = new HashMap<>();
          methods.put(m.getName(), mm);
        }
        Class<?>[] param_types = m.getParameterTypes();
        String sig_str = Arrays.toString(param_types);
        mm.put(sig_str, m);
      }
      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    }
  }

}
