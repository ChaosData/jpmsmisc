package trust.nccgroup.linkertest;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import java.nio.file.*;
import java.nio.*;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;

/*
// $ javac --add-modules jdk.incubator.foreign LinkerTest.java
// $ java --add-modules jdk.incubator.foreign --enable-native-access=ALL-UNNAMED LinkerTest
*/

// $ ./gradlew build
// $ java --add-modules jdk.incubator.foreign --enable-native-access=trust.nccgroup.linkertest -p build/libs/linkertest.jar -m trust.nccgroup.linkertest/trust.nccgroup.linkertest.LinkerTest

public class LinkerTest {

  public LinkerTest() {
  }

  public static long scanForClass(Class<?> cls) throws Throwable {
    System.out.println("scanning for " + cls);
    MethodHandle memcpywrapper = CLinker.getInstance().downcallHandle(
      CLinker.systemLookup().lookup("memcpy").orElseThrow(Exception::new),
      MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_LONG, CLinker.C_LONG)
    );

    ResourceScope scope = ResourceScope.newConfinedScope();
    MemorySegment ms = MemorySegment.allocateNative(8, 1024, scope);
    ByteBuffer bb = ms.asByteBuffer();

    int hashcode = System.identityHashCode(cls);
    int hashcode2 = cls.hashCode();
    if (hashcode != hashcode2) {
      System.out.println("mismatched hashcodes for " + cls);
    }

    int hashcoder = Integer.reverseBytes(hashcode);

    String maps = new String(Files.readAllBytes(Paths.get("/proc/self/maps")));
    //System.out.println(maps);
    String[] lines = maps.split("\n");
    ArrayList<Long[]> entries = new ArrayList<>();
    for (String line : lines) {
      //if (/*(line.endsWith(" 0 ") || line.endsWith("[heap]")) &&*/ line.indexOf(" rw-p ") != -1) {
      if (/*(line.endsWith(" 0 ") || line.endsWith("[heap]")) &&*/ (line.indexOf(" rw-p ") != -1 || line.indexOf(" r--p ") != -1)) {
        //System.out.println(line);
        String[] parts = line.split(" ");
        String[] range = parts[0].split("-");
        long a = Long.parseLong(range[0], 16);
        long b = Long.parseLong(range[1], 16);
        entries.add(new Long[]{a, b});
      }
    }

    for (Long[] range : entries) {
      for (long i = range[0]; i+3 < range[1]; i+=1) {
        memcpywrapper.invokeExact(ms.address(), i, (long)4);
        int v = bb.position(0).getInt();
        if (v == hashcoder) {
          memcpywrapper.invokeExact(ms.address(), i+7, (long)2);
          if ( ((bb.position(0).get(0) & 0xff) == 0x58)
            && ((bb.position(0).get(1) & 0xff) == 0x17) ) {
            System.out.println("found: " + Long.toHexString(i-1));
            dumpClass(i-1);
            return i-1;
          }
        }
      }
    }
    return 0;
  }

  public static void dumpClass(long addr) throws Throwable {
    MethodHandle memcpywrapper = CLinker.getInstance().downcallHandle(
      CLinker.systemLookup().lookup("memcpy").orElseThrow(Exception::new),
      MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_LONG, CLinker.C_LONG)
    );

    ResourceScope scope = ResourceScope.newConfinedScope();
    MemorySegment ms = MemorySegment.allocateNative(8, 1024, scope);
    ByteBuffer bb = ms.asByteBuffer();

    if (addr != 0) {
      for (long ii = addr-1; ii+3 < addr+0x60+3; ii+=4) {
        memcpywrapper.invokeExact(ms.address(), ii, (long)4);
        String a = Integer.toHexString(bb.position(0).get(0) & 0xff);
        String _b = Integer.toHexString(bb.position(0).get(1) & 0xff);
        String c = Integer.toHexString(bb.position(0).get(2) & 0xff);
        String d = Integer.toHexString(bb.position(0).get(3) & 0xff);
        System.out.println("[" + Long.toHexString(ii) + "]: " + a + " " + _b + " " + c + " " + d);
      }
    } else {
      System.out.println("addr == null");
    }
  }

  public static void writeInt(long addr, int val) throws Throwable {
    MethodHandle memcpywrapperW = CLinker.getInstance().downcallHandle(
      CLinker.systemLookup().lookup("memcpy").orElseThrow(Exception::new),
      MethodType.methodType(void.class, long.class, MemoryAddress.class, long.class),
      FunctionDescriptor.ofVoid(CLinker.C_LONG, CLinker.C_POINTER, CLinker.C_LONG)
    );

    ResourceScope scope = ResourceScope.newConfinedScope();
    MemorySegment ms = MemorySegment.allocateNative(4, 1024, scope);
    ByteBuffer bb = ms.asByteBuffer();
    bb.putInt(val);
    memcpywrapperW.invoke(addr, ms.address(), (long)4);
  }

  public static int readInt(long addr) throws Throwable {
    MethodHandle memcpywrapperR = CLinker.getInstance().downcallHandle(
      CLinker.systemLookup().lookup("memcpy").orElseThrow(Exception::new),
      MethodType.methodType(void.class, MemoryAddress.class, long.class, long.class),
      FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_LONG, CLinker.C_LONG)
    );

    ResourceScope scope = ResourceScope.newConfinedScope();
    MemorySegment ms = MemorySegment.allocateNative(4, 1024, scope);
    ByteBuffer bb = ms.asByteBuffer();
    memcpywrapperR.invoke(ms.address(), addr, (long)4);
    int val = bb.position(0).getInt();
    return val;
  }

  public static void main(String[] argv) throws Throwable {
    /*
    MethodHandle isatty = CLinker.getInstance().downcallHandle(
      CLinker.systemLookup().lookup("isatty").orElseThrow(Exception::new),
      MethodType.methodType(int.class, int.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_INT)
    );

    System.out.println("isatty(0): " + isatty.invoke(0));
    System.out.println("isatty(1): " + isatty.invoke(1));
    System.out.println("isatty(2): " + isatty.invoke(2));

    MethodHandle printftest = CLinker.getInstance().downcallHandle(
      CLinker.systemLookup().lookup("printf").orElseThrow(Exception::new),
      MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class),
      FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER)
    );

    String fmt = "hello %s\n";
    String arg = "world";
    try(ResourceScope scope = ResourceScope.newConfinedScope()) {
      SegmentAllocator allocator = SegmentAllocator.arenaAllocator(scope);
      MemorySegment _fmt = CLinker.toCString(fmt, allocator);
      MemorySegment _arg = CLinker.toCString(arg, allocator);
      int len = (int) printftest.invokeExact(_fmt.address(), _arg.address());
    }
    */

    long linkertestaddr = scanForClass(LinkerTest.class);
    long oscaddr = scanForClass(ObjectStreamClass.class);

    System.out.println("linkertestaddr: " + Long.toHexString(linkertestaddr));
    System.out.println("oscaddr: " + Long.toHexString(oscaddr));

    try {
      Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
      Method m = unsafeClass.getDeclaredMethod("getUnsafe");
      Object unsafe = m.invoke(null);
      System.out.println(unsafe);
    } catch (Throwable t) {
      t.printStackTrace();
    }

    if (linkertestaddr != 0 && oscaddr != 0) {
      int origmodule = readInt(linkertestaddr + 48);
      int javabasejavaiomodule = readInt(oscaddr + 48);
      writeInt(linkertestaddr + 48, javabasejavaiomodule);
      System.out.println(LinkerTest.class.getModule());
      if (!LinkerTest.class.getModule().equals(ObjectStreamClass.class.getModule())) {
        System.out.println("didn't work, trying again...");
        writeInt(linkertestaddr + 48, origmodule);
        java.util.concurrent.TimeUnit.SECONDS.sleep(2);
        LinkerTest.main(argv);
        return;
      }

      try {
        Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
        Method m = unsafeClass.getDeclaredMethod("getUnsafe");
        Object unsafe = m.invoke(null);
        System.out.println(unsafe);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }
}
