package trust.nccgroup.unsafetest;

import java.lang.reflect.*;
import java.io.*;

public class Test {
  public static void doThing() {
    System.out.println("==<trust.nccgroup.unsafetest.Test::doThing()>==");
    try {
      Class<?> unsafe_class = Class.forName("sun.misc.Unsafe");
      System.out.println(unsafe_class);
      Field theUnsafe = unsafe_class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      System.out.println(theUnsafe);
      Object unsafe = theUnsafe.get(null);
      System.out.println(unsafe);
      Method m = unsafe_class.getDeclaredMethod("putByte", Object.class, long.class, byte.class);
      System.out.println(m);
      m.invoke(unsafe, "abcd", 0, (byte)0);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.out.println("====");
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
        writeKeys_field.set(fr, new long[]{0});

        Method setPrimFieldValues = fr.getClass().getDeclaredMethod("setPrimFieldValues", Object.class, byte[].class);
        setPrimFieldValues.setAccessible(true);
        String f = "foooooo";
        byte zeros[] = new byte[4];
        setPrimFieldValues.invoke(fr, f, zeros);
        System.out.println(f);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    System.out.println("==</trust.nccgroup.unsafetest.Test::doThing()>==");
  }
}
