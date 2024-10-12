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
  private native Object nativeInit();

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
    /*Class<?>[] sig = new Class<?>[args.length];
    for (int i=0; i<args.length; i++) {
      sig[i] = args[i].getClass();
    }
    String sig_str = Arrays.toString(sig);
    */
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

  public static boolean init(String libunsafe_path) {
    try {
      System.load(libunsafe_path);
      UnsafeWrapper u = new UnsafeWrapper();
      theUnsafe = u.nativeInit();

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
        /*for (int i=0; i<param_types.length; i++) {
          if (param_types[i].isPrimitive()) {
            param_types[i] = boxmap.get(param_types[i]);
          }
        }*/
        String sig_str = Arrays.toString(param_types);
        //System.out.println("adding " + m.getName() + "(" + sig_str + ")");
        mm.put(sig_str, m);
      }
      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    }
  }

  /*
  public int getInt(Object o, long offset) {
    //return theInternalUnsafe.getInt(o, offset);
  }

  public void putInt(Object o, long offset, int x) {
    //theInternalUnsafe.putInt(o, offset, x);
  }

  public Object getObject(Object o, long offset) {
    //return theInternalUnsafe.getObject(o, offset);
  }

  public void putObject(Object o, long offset, Object x) {
    //theInternalUnsafe.putObject(o, offset, x);
  }

  public boolean getBoolean(Object o, long offset) {
    //return theInternalUnsafe.getBoolean(o, offset);
  }

  public void putBoolean(Object o, long offset, boolean x) {
    //theInternalUnsafe.putBoolean(o, offset, x);
  }

  public byte getByte(Object o, long offset) {
    //return theInternalUnsafe.getByte(o, offset);
  }

  public void putByte(Object o, long offset, byte x) {
    //theInternalUnsafe.putByte(o, offset, x);
  }

  public short getShort(Object o, long offset) {
    //return theInternalUnsafe.getShort(o, offset);
  }

  public void putShort(Object o, long offset, short x) {
    //theInternalUnsafe.putShort(o, offset, x);
  }

  public char getChar(Object o, long offset) {
    //return theInternalUnsafe.getChar(o, offset);
  }

  public void putChar(Object o, long offset, char x) {
    //theInternalUnsafe.putChar(o, offset, x);
  }

  public long getLong(Object o, long offset) {
    //return theInternalUnsafe.getLong(o, offset);
  }

  public void putLong(Object o, long offset, long x) {
    //theInternalUnsafe.putLong(o, offset, x);
  }

  public float getFloat(Object o, long offset) {
    //return theInternalUnsafe.getFloat(o, offset);
  }

  public void putFloat(Object o, long offset, float x) {
    //theInternalUnsafe.putFloat(o, offset, x);
  }

  public double getDouble(Object o, long offset) {
    //return theInternalUnsafe.getDouble(o, offset);
  }

  public void putDouble(Object o, long offset, double x) {
    //theInternalUnsafe.putDouble(o, offset, x);
  }

  public byte getByte(long address) {
    //return theInternalUnsafe.getByte(address);
  }

  public void putByte(long address, byte x) {
    //theInternalUnsafe.putByte(address, x);
  }

  public short getShort(long address) {
    //return theInternalUnsafe.getShort(address);
  }

  public void putShort(long address, short x) {
    //theInternalUnsafe.putShort(address, x);
  }

  public char getChar(long address) {
    //return theInternalUnsafe.getChar(address);
  }

  public void putChar(long address, char x) {
    //theInternalUnsafe.putChar(address, x);
  }

  public int getInt(long address) {
    //return theInternalUnsafe.getInt(address);
  }

  public void putInt(long address, int x) {
    //theInternalUnsafe.putInt(address, x);
  }

  public long getLong(long address) {
    //return theInternalUnsafe.getLong(address);
  }

  public void putLong(long address, long x) {
    //theInternalUnsafe.putLong(address, x);
  }

  public float getFloat(long address) {
    //return theInternalUnsafe.getFloat(address);
  }

  public void putFloat(long address, float x) {
    //theInternalUnsafe.putFloat(address, x);
  }

  public double getDouble(long address) {
    //return theInternalUnsafe.getDouble(address);
  }

  public void putDouble(long address, double x) {
    //theInternalUnsafe.putDouble(address, x);
  }

  public long getAddress(long address) {
    //return theInternalUnsafe.getAddress(address);
  }

  public void putAddress(long address, long x) {
    //theInternalUnsafe.putAddress(address, x);
  }

  public long allocateMemory(long bytes) {
    //return theInternalUnsafe.allocateMemory(bytes);
  }

  public long reallocateMemory(long address, long bytes) {
    //return theInternalUnsafe.reallocateMemory(address, bytes);
  }

  public void setMemory(Object o, long offset, long bytes, byte value) {
    //theInternalUnsafe.setMemory(o, offset, bytes, value);
  }

  public void setMemory(long address, long bytes, byte value) {
    //theInternalUnsafe.setMemory(address, bytes, value);
  }

  public void copyMemory(Object srcBase, long srcOffset,
               Object destBase, long destOffset,
               long bytes) {
    //theInternalUnsafe.copyMemory(srcBase, srcOffset, destBase, destOffset, bytes);
  }

  public void copyMemory(long srcAddress, long destAddress, long bytes) {
    //theInternalUnsafe.copyMemory(srcAddress, destAddress, bytes);
  }

  public void freeMemory(long address) {
    //theInternalUnsafe.freeMemory(address);
  }

  //public static final int INVALID_FIELD_OFFSET = jdk.internal.misc.Unsafe.INVALID_FIELD_OFFSET;

  public long objectFieldOffset(Field f) {
    //return theInternalUnsafe.objectFieldOffset(f);
  }

  public long staticFieldOffset(Field f) {
    //return theInternalUnsafe.staticFieldOffset(f);
  }

  public Object staticFieldBase(Field f) {
    //return theInternalUnsafe.staticFieldBase(f);
  }

  public boolean shouldBeInitialized(Class<?> c) {
    //return theInternalUnsafe.shouldBeInitialized(c);
  }

  public void ensureClassInitialized(Class<?> c) {
    //theInternalUnsafe.ensureClassInitialized(c);
  }

  public int arrayBaseOffset(Class<?> arrayClass) {
    //return theInternalUnsafe.arrayBaseOffset(arrayClass);
  }

  //public static final int ARRAY_BOOLEAN_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_BOOLEAN_BASE_OFFSET;
  //public static final int ARRAY_BYTE_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;
  //public static final int ARRAY_SHORT_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_SHORT_BASE_OFFSET;
  //public static final int ARRAY_CHAR_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_CHAR_BASE_OFFSET;
  //public static final int ARRAY_INT_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_INT_BASE_OFFSET;
  //public static final int ARRAY_LONG_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_LONG_BASE_OFFSET;
  //public static final int ARRAY_FLOAT_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_FLOAT_BASE_OFFSET;
  //public static final int ARRAY_DOUBLE_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_DOUBLE_BASE_OFFSET;
  //public static final int ARRAY_OBJECT_BASE_OFFSET = jdk.internal.misc.Unsafe.ARRAY_OBJECT_BASE_OFFSET;

  public int arrayIndexScale(Class<?> arrayClass) {
    //return theInternalUnsafe.arrayIndexScale(arrayClass);
  }

  //public static final int ARRAY_BOOLEAN_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_BOOLEAN_INDEX_SCALE;
  //public static final int ARRAY_BYTE_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_BYTE_INDEX_SCALE;
  //public static final int ARRAY_SHORT_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_SHORT_INDEX_SCALE;
  //public static final int ARRAY_CHAR_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_CHAR_INDEX_SCALE;
  //public static final int ARRAY_INT_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_INT_INDEX_SCALE;
  //public static final int ARRAY_LONG_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_LONG_INDEX_SCALE;
  //public static final int ARRAY_FLOAT_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_FLOAT_INDEX_SCALE;
  //public static final int ARRAY_DOUBLE_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_DOUBLE_INDEX_SCALE;
  //public static final int ARRAY_OBJECT_INDEX_SCALE = jdk.internal.misc.Unsafe.ARRAY_OBJECT_INDEX_SCALE;

  public int addressSize() {
    //return theInternalUnsafe.addressSize();
  }

  //public static final int ADDRESS_SIZE = theInternalUnsafe.addressSize();

  public int pageSize() {
    //return theInternalUnsafe.pageSize();
  }


  public Class<?> defineClass(String name, byte[] b, int off, int len,
                ClassLoader loader,
                ProtectionDomain protectionDomain) {
    //return theInternalUnsafe.defineClass(name, b, off, len, loader, protectionDomain);
  }

  public Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
    //return theInternalUnsafe.defineAnonymousClass(hostClass, data, cpPatches);
  }

  public Object allocateInstance(Class<?> cls)
    throws InstantiationException {
    //return theInternalUnsafe.allocateInstance(cls);
  }

  @ForceInline
  public void throwException(Throwable ee) {
    //theInternalUnsafe.throwException(ee);
  }

  public final boolean compareAndSwapObject(Object o, long offset,
                        Object expected,
                        Object x) {
    //return theInternalUnsafe.compareAndSetObject(o, offset, expected, x);
  }

  public final boolean compareAndSwapInt(Object o, long offset,
                       int expected,
                       int x) {
    //return theInternalUnsafe.compareAndSetInt(o, offset, expected, x);
  }

  public final boolean compareAndSwapLong(Object o, long offset,
                      long expected,
                      long x) {
    //return theInternalUnsafe.compareAndSetLong(o, offset, expected, x);
  }

  public Object getObjectVolatile(Object o, long offset) {
    //return theInternalUnsafe.getObjectVolatile(o, offset);
  }

  public void putObjectVolatile(Object o, long offset, Object x) {
    //theInternalUnsafe.putObjectVolatile(o, offset, x);
  }

  public int getIntVolatile(Object o, long offset) {
    //return theInternalUnsafe.getIntVolatile(o, offset);
  }

  public void putIntVolatile(Object o, long offset, int x) {
    //theInternalUnsafe.putIntVolatile(o, offset, x);
  }

  public boolean getBooleanVolatile(Object o, long offset) {
    //return theInternalUnsafe.getBooleanVolatile(o, offset);
  }

  public void putBooleanVolatile(Object o, long offset, boolean x) {
    //theInternalUnsafe.putBooleanVolatile(o, offset, x);
  }

  public byte getByteVolatile(Object o, long offset) {
    //return theInternalUnsafe.getByteVolatile(o, offset);
  }

  public void putByteVolatile(Object o, long offset, byte x) {
    //theInternalUnsafe.putByteVolatile(o, offset, x);
  }

  public short getShortVolatile(Object o, long offset) {
    //return theInternalUnsafe.getShortVolatile(o, offset);
  }

  public void putShortVolatile(Object o, long offset, short x) {
    //theInternalUnsafe.putShortVolatile(o, offset, x);
  }

  public char getCharVolatile(Object o, long offset) {
    //return theInternalUnsafe.getCharVolatile(o, offset);
  }

  public void putCharVolatile(Object o, long offset, char x) {
    //theInternalUnsafe.putCharVolatile(o, offset, x);
  }

  public long getLongVolatile(Object o, long offset) {
    //return theInternalUnsafe.getLongVolatile(o, offset);
  }

  public void putLongVolatile(Object o, long offset, long x) {
    //theInternalUnsafe.putLongVolatile(o, offset, x);
  }

  public float getFloatVolatile(Object o, long offset) {
    //return theInternalUnsafe.getFloatVolatile(o, offset);
  }

  public void putFloatVolatile(Object o, long offset, float x) {
    //theInternalUnsafe.putFloatVolatile(o, offset, x);
  }

  public double getDoubleVolatile(Object o, long offset) {
    //return theInternalUnsafe.getDoubleVolatile(o, offset);
  }

  public void putDoubleVolatile(Object o, long offset, double x) {
    //theInternalUnsafe.putDoubleVolatile(o, offset, x);
  }

  public void putOrderedObject(Object o, long offset, Object x) {
    //theInternalUnsafe.putObjectRelease(o, offset, x);
  }

  public void putOrderedInt(Object o, long offset, int x) {
    //theInternalUnsafe.putIntRelease(o, offset, x);
  }

  public void putOrderedLong(Object o, long offset, long x) {
    //theInternalUnsafe.putLongRelease(o, offset, x);
  }

  public void unpark(Object thread) {
    //theInternalUnsafe.unpark(thread);
  }

  public void park(boolean isAbsolute, long time) {
    //theInternalUnsafe.park(isAbsolute, time);
  }

  public int getLoadAverage(double[] loadavg, int nelems) {
    //return theInternalUnsafe.getLoadAverage(loadavg, nelems);
  }

  public final int getAndAddInt(Object o, long offset, int delta) {
    //return theInternalUnsafe.getAndAddInt(o, offset, delta);
  }

  public final long getAndAddLong(Object o, long offset, long delta) {
    //return theInternalUnsafe.getAndAddLong(o, offset, delta);
  }

  public final int getAndSetInt(Object o, long offset, int newValue) {
    //return theInternalUnsafe.getAndSetInt(o, offset, newValue);
  }

  public final long getAndSetLong(Object o, long offset, long newValue) {
    //return theInternalUnsafe.getAndSetLong(o, offset, newValue);
  }

  public final Object getAndSetObject(Object o, long offset, Object newValue) {
    //return theInternalUnsafe.getAndSetObject(o, offset, newValue);
  }

  public void loadFence() {
    //theInternalUnsafe.loadFence();
  }

  public void storeFence() {
    //theInternalUnsafe.storeFence();
  }

  public void fullFence() {
    //theInternalUnsafe.fullFence();
  }*/

  /*
  public void invokeCleaner(java.nio.ByteBuffer directBuffer) {
    if (!directBuffer.isDirect())
      throw new IllegalArgumentException("buffer is non-direct");

    DirectBuffer db = (DirectBuffer)directBuffer;
    if (db.attachment() != null)
      throw new IllegalArgumentException("duplicate or slice");

    Cleaner cleaner = db.cleaner();
    if (cleaner != null) {
      cleaner.clean();
    }
  }*/
}
