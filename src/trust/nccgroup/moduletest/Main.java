package trust.nccgroup.moduletest;

import java.io.*;
// ./gradle jar
// $ java -cp 'libs/*:build/libs/moduletest-0.0.1.jar' trust.nccgroup.moduletest.Main
// $ java -p build/libs:libs -m trust.nccgroup.moduletest/trust.nccgroup.moduletest.Main

import java.lang.reflect.*;
import java.util.List;
import java.util.Arrays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.module.*;
import java.nio.file.Path;
import java.util.Set;
import trust.nccgroup.UnsafeWrapper;

public class Main {

  //public native Class<?> doNative();
  public native Object doNative();

  public static class Test {
    public Test(String a, String b) {
      foo = a;
      bar = b;
    }
    public String foo;
    private String bar;
    //public sun.misc.Unsafe baz;
  }

  public static void test1() {
    try {
      Field field = Integer.class.getDeclaredField("value");
      field.setAccessible(true);
      field.set(1, 42);

      List<Integer> list = Arrays.asList(0, 1, 2);
      System.out.println(list);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void patchInteger(Object theUnsafe) {
    try {
      Class<?> theUnsafe_class = theUnsafe.getClass();
      Method theUnsafe_field_objectFieldOffset = theUnsafe_class.getMethod("objectFieldOffset", Field.class);

      Class<?> class_class = Class.class;
      Field class_field_module = class_class.getDeclaredField("module");

      long moduleFieldOffset = (long)(Long)theUnsafe_field_objectFieldOffset.invoke(theUnsafe, class_field_module);
      System.out.println("Class.module offset: " + moduleFieldOffset);

      Method theUnsafe_method_getLong = theUnsafe_class.getMethod("getLong", Object.class, long.class);
      Method theUnsafe_method_putLong = theUnsafe_class.getMethod("putLong", Object.class, long.class, long.class);

      long myModule = (long)(Long)theUnsafe_method_getLong.invoke(theUnsafe, Main.class, moduleFieldOffset);
      long javalangModule = (long)(Long)theUnsafe_method_getLong.invoke(theUnsafe, Integer.class, moduleFieldOffset);
      System.out.println("Main.class.module: " + myModule);
      System.out.println("Integer.class.module: " + javalangModule);

      //theUnsafe_method_putLong.invoke(theUnsafe, Integer.class, moduleFieldOffset, myModule);
      theUnsafe_method_putLong.invoke(theUnsafe, Main.class, moduleFieldOffset, javalangModule);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void patchInteger2() {
    try {
      //Class<?> theUnsafe_class = theUnsafe.getClass();
      //Method theUnsafe_field_objectFieldOffset = theUnsafe_class.getMethod("objectFieldOffset", Field.class);

      Class<?> class_class = Class.class;
      Field class_field_module = class_class.getDeclaredField("module");

      long moduleFieldOffset = (long)(Long)UnsafeWrapper.invoke("objectFieldOffset", new Class<?>[]{Field.class}, class_field_module);
      System.out.println("Class.module offset: " + moduleFieldOffset);

      //Method theUnsafe_method_getLong = theUnsafe_class.getMethod("getLong", Object.class, long.class);
      //Method theUnsafe_method_putLong = theUnsafe_class.getMethod("putLong", Object.class, long.class, long.class);

      long myModule = (long)(Long)UnsafeWrapper.invoke("getLong", new Class<?>[]{Object.class, long.class}, Main.class, moduleFieldOffset);
      long javalangModule = (long)(Long)UnsafeWrapper.invoke("getLong", new Class<?>[]{Object.class, long.class}, Integer.class, moduleFieldOffset);
      System.out.println("Main.class.module: " + myModule);
      System.out.println("Integer.class.module: " + javalangModule);

      //UnsafeWrapper.invoke("putLong", Integer.class, moduleFieldOffset, myModule);
      UnsafeWrapper.invoke("putLong", new Class<?>[]{Object.class, long.class, long.class}, Main.class, moduleFieldOffset, javalangModule);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static long patchClassModule(Class<?> clobber, Class<?> with) {
    try {
      Class<?> class_class = Class.class;
      Field class_field_module = class_class.getDeclaredField("module");

      long moduleFieldOffset = (long)(Long)UnsafeWrapper.invoke("objectFieldOffset", new Class<?>[]{Field.class}, class_field_module);
      System.out.println("Class.module offset: " + moduleFieldOffset);

      long original_module = (long)(Long)UnsafeWrapper.invoke("getLong", new Class<?>[]{Object.class, long.class}, clobber, moduleFieldOffset);
      long new_module = (long)(Long)UnsafeWrapper.invoke("getLong", new Class<?>[]{Object.class, long.class}, with, moduleFieldOffset);

      UnsafeWrapper.invoke("putLong", new Class<?>[]{Object.class, long.class, long.class}, clobber, moduleFieldOffset, new_module);
      return original_module;
    } catch (Throwable t) {
      t.printStackTrace();
      return 0;
    }
  }

  public static void stompModule(Class<?> clobber, long with) {
    try {
      Class<?> class_class = Class.class;
      Field class_field_module = class_class.getDeclaredField("module");
      long moduleFieldOffset = (long)(Long)UnsafeWrapper.invoke("objectFieldOffset", new Class<?>[]{Field.class}, class_field_module);

      UnsafeWrapper.invoke("putLong", new Class<?>[]{Object.class, long.class, long.class}, clobber, moduleFieldOffset, with);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }


  public static void test2() {
    try {
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      //Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
      Field f = unsafeClass.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      final Object unsafe = f.get(null);
      System.out.println("unsafe: " + unsafe);

      /*
      Method objectFieldOffset = unsafe.getClass().getMethod("objectFieldOffset", Field.class);
      Class<?> c = Class.class;
      Field moduleField = c.getDeclaredField("module");
      long moduleFieldOffset = (long)(Long)objectFieldOffset.invoke(unsafe, moduleField);
      System.out.println("Class.module offset: " + moduleFieldOffset);

      Method getLong = unsafe.getClass().getMethod("getLong", Object.class, long.class);
      Method putLong = unsafe.getClass().getMethod("putLong", Object.class, long.class, long.class);

      long myModule = (long)(Long)getLong.invoke(unsafe, Main.class, moduleFieldOffset);
      long javalangModule = (long)(Long)getLong.invoke(unsafe, Integer.class, moduleFieldOffset);
      System.out.println("Main.class.module: " + myModule);
      System.out.println("Integer.class.module: " + javalangModule);

      putLong.invoke(unsafe, Integer.class, moduleFieldOffset, myModule);
      */
      patchInteger(unsafe);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void gsontest1() {
    try {
      Test t = new Test("aaaa", "bbbb");
      GsonBuilder builder = new GsonBuilder();//.disableJdkUnsafe();
      Gson gson = builder.create();
      String out = gson.toJson(t);
      System.out.println(out);
      Test t2 = gson.fromJson(out, Test.class);
      System.out.println(t2);
      //System.out.println(t2.baz);

      Class<?> unsafeAllocatorClass = Class.forName("com.google.gson.internal.UnsafeAllocator");
      System.out.println(unsafeAllocatorClass);
      System.out.println(Arrays.toString(unsafeAllocatorClass.getDeclaredFields()));

      Method create = unsafeAllocatorClass.getMethod("create");
      System.out.println(create);
      // --add-exports=com.google.gson/com.google.gson.internal=trust.nccgroup.moduletest
      Object thing = create.invoke(null);
      //Class<?> thing = unsafeAllocatorClass.getMethod("create").invoke(null);
      System.out.println(thing);
      Class<?> c = thing.getClass();
      System.out.println(c);
      System.out.println(getlocation(c));

      System.out.println(Arrays.toString(c.getDeclaredFields()));
      Method newInstance = c.getDeclaredMethod("newInstance", Class.class);
      newInstance.setAccessible(true);
      Object o = newInstance.invoke(thing, Test.class);
      System.out.println(o);
    } catch (Throwable th) {
      th.printStackTrace();
    }
  }

  public static void gsontest2() {
    try {
      //URLClassLoader u = new URLClassLoader(new URL[]{new URL("file:///Users/jtd/code/java/moduletest/jar/thing/build/libs/obj.jar")});
      //URLClassLoader u = new URLClassLoader(new URL[]{new File("jar/thing/build/libs/obj.jar").toURI().toURL()});
      //URLClassLoader u = URLClassLoader.newInstance(new URL[]{new File("jar/thing/build/libs/obj.jar").toURI().toURL()}, Gson.class.getClassLoader());
      //URLClassLoader u = URLClassLoader.newInstance(new URL[]{new File("build/libs/moduletest.jar").toURI().toURL()}, Gson.class.getClassLoader());
      //System.out.println(u);
      //Class<?> c = u.loadClass("trust.nccgroup.moduletest.Obj");
      //ModuleFinder finder = ModuleFinder.of(Path.of("jar/thing/build/libs"));
      ModuleFinder finder = ModuleFinder.of(Path.of("jar/thing/mod/jar"));
      ModuleLayer parent = ModuleLayer.boot();
      //ModuleLayer parent = Gson.class.getModule().getLayer();
      Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of("obj"));
      ClassLoader scl = ClassLoader.getSystemClassLoader();
      //ModuleLayer layer = parent.defineModulesWithOneLoader(cf, u);
      //ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);
      ModuleLayer layer = parent.defineModulesWithOneLoader(cf, Gson.class.getClassLoader());
      Class<?> c = layer.findLoader("obj").loadClass("trust.nccgroup.moduletest.Obj");
      System.out.println(c);
      System.out.println(c.getModule());

      //Class<?> c = u.loadClass("trust.nccgroup.moduletest.Main");
      //System.out.println(c);
      //System.out.println(c.getModule());
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.create();
      Object o = gson.fromJson("{\"aaa\":{}}", c);
      System.out.println(o);
      Field aaa = c.getDeclaredField("aaa");
      System.out.println(aaa.get(o));
      //System.out.println(o.aaa);
    } catch (Throwable t) {
      t.printStackTrace();
    }

    //Gson gson = builder.create();
    //Obj o = gson.fromJson("{}", Obj.class);
    //System.out.println(o);
    //System.out.println(o.aaa);
  }

  /*
  public static void dyntest1() {
    try {
      org.cojen.maker.ClassMaker cm = org.cojen.maker.ClassMaker.begin().public_();
      org.cojen.maker.MethodMaker mm = cm.addMethod(null, "run").public_().static_();
      mm.var(System.class).field("out").invoke("println", "hello, world");

      Class<?> clazz = cm.finish();
      clazz.getMethod("run").invoke(null);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
  */

  public static void test3() {
    try {
      System.load("/Users/jtd/code/java/moduletest/jni/test.dylib");
      Main m = new Main();
      //Class<?> c = m.doNative();
      Object theUnsafe = m.doNative();
      System.out.println(theUnsafe);
      Class<?> c = theUnsafe.getClass();
      System.out.println(c);
      patchInteger(theUnsafe);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void test4() {
    try {
      UnsafeWrapper.init("/Users/jtd/code/java/moduletest/jni/test.dylib");
      patchInteger2();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void test5() {
    try {
      //UnsafeWrapper.init("/Users/jtd/code/java/moduletest/jni/test.dylib");
      UnsafeWrapper.init("/moduletest/jni/test.so");
      System.out.println(Main.class.getModule());
      long main_original_module = patchClassModule(Main.class, Integer.class);
      System.out.println(Main.class.getModule());
      test1();
      stompModule(Main.class, main_original_module);
      System.out.println(Main.class.getModule());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void test6() {
    try {
      /* // actually the internal Unsafe, requires another opens to work with
      Field f = Class.forName("java.nio.Bits").getDeclaredField("UNSAFE");
      f.setAccessible(true);
      Object o = f.get(null);
      System.out.println(o);
      Method m = o.getClass().getDeclaredMethod("putByte", Object.class, long.class, byte.class);
      System.out.println(m);
      m.invoke(o, "abcd", 0, (byte)0);
      */

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
  }

  public static void test7() {
    try {
      URLClassLoader u = new URLClassLoader(new URL[]{new File("jar/unsafetest/build/libs/unsafetest.jar").toURI().toURL()});
      Class<?> c = u.loadClass("trust.nccgroup.unsafetest.Test");
      Method m = c.getDeclaredMethod("doThing");
      m.invoke(null);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static void test8() {
    System.out.println("Main.class.getModule():" + Main.class.getModule());
    System.out.println("ObjectStreamClass.class.getModule():" + ObjectStreamClass.class.getModule());
    try {
      System.out.println("trying to get sun.misc.Unsafe:");
      Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      System.out.println("unsafeClass: " + unsafeClass);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    try {
      System.out.println("trying to get jdk.internal.misc.Unsafe:");
      Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
      System.out.println("unsafeClass: " + unsafeClass);
      Method m = unsafeClass.getDeclaredMethod("getUnsafe");
      Object unsafe = m.invoke(null);
      System.out.println("unsafe: " + unsafe);
    } catch (Throwable t) {
      t.printStackTrace();
    }
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

      Method getPrimFieldValues = fr.getClass().getDeclaredMethod("getPrimFieldValues", Object.class, byte[].class);
      getPrimFieldValues.setAccessible(true);

      Field readKeys_field = fr.getClass().getDeclaredField("readKeys");
      readKeys_field.setAccessible(true);
      readKeys_field.set(fr, new long[]{48});

      byte javabase_javaio_module[] = new byte[4];
      System.out.println("Arrays.toString(javabase_javaio_module):" + Arrays.toString(javabase_javaio_module));
      getPrimFieldValues.invoke(fr, ObjectStreamClass.class, javabase_javaio_module);
      System.out.println("Arrays.toString(javabase_javaio_module):" + Arrays.toString(javabase_javaio_module));

      Field writeKeys_field = fr.getClass().getDeclaredField("writeKeys");
      writeKeys_field.setAccessible(true);
      writeKeys_field.set(fr, new long[]{48});

      Method setPrimFieldValues = fr.getClass().getDeclaredMethod("setPrimFieldValues", Object.class, byte[].class);
      setPrimFieldValues.setAccessible(true);

      setPrimFieldValues.invoke(fr, Main.class, javabase_javaio_module);
      System.out.println("Main.class.getModule():" + Main.class.getModule());

      /*
      byte cl[] = new byte[4];
      readKeys_field.set(fr, new long[]{48+4});
      System.out.println("Arrays.toString(cl):" + Arrays.toString(cl));
      getPrimFieldValues.invoke(fr, ObjectStreamClass.class, cl);
      System.out.println("Arrays.toString(cl):" + Arrays.toString(cl));

      writeKeys_field.set(fr, new long[]{48+4});
      setPrimFieldValues.invoke(fr, Main.class, cl);
      */

      try {
        System.out.println("trying to get sun.misc.Unsafe:");
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        System.out.println("unsafeClass: " + unsafeClass);
      } catch (Throwable t) {
        t.printStackTrace();
      }
      try {
        System.out.println("trying to get jdk.internal.misc.Unsafe:");
        Class<?> unsafeClass = Class.forName("jdk.internal.misc.Unsafe");
        System.out.println("unsafeClass: " + unsafeClass);
        Method m = unsafeClass.getDeclaredMethod("getUnsafe");
        Object unsafe = m.invoke(null);
        System.out.println("unsafe: " + unsafe);

        //Field f = unsafeClass.getDeclaredField("theUnsafe");
        //f.setAccessible(true);
        //final Object unsafe = f.get(null);
        //System.out.println("unsafe: " + unsafe);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }


  public static URL getlocation(Class<?> c) {
    return c.getResource('/' + c.getName().replace('.', '/') + ".class");
  }

  public static void main(String[] argv) throws Throwable {
    //Module m = Main.class.getModule();
    System.out.println("Main module: " + Main.class.getModule());
    System.out.println("Main: " + getlocation(Main.class));
    System.out.println("Integer module: " + Integer.class.getModule());
    System.out.println("Integer: " + getlocation(Integer.class));
    System.out.println("UnsafeWrapper module: " + UnsafeWrapper.class.getModule());
    System.out.println("UnsafeWrapper: " + getlocation(UnsafeWrapper.class));
    //System.out.println("com.google.gson.internal.UnsafeAllocator: " + com.google.gson.internal.UnsafeAllocator.class.getModule());
    //System.out.println("com.google.gson.internal.UnsafeAllocator: " + getlocation(com.google.gson.internal.UnsafeAllocator.class));
    System.out.println("-----test5-----");
    test5();

    System.out.println("-----test6-----");
    test6();
    System.out.println("-----test7-----");
    test7();
    System.out.println("-----test8-----");
    test8();
    System.out.println("-----test1-----");
    test1();
    System.out.println("-----test2-----");
    test2();
    System.out.println("-----test1----");
    test1();
    //System.out.println("done");
    System.out.println("-----gtest1-----");
    gsontest1();
    System.out.println("-----gtest2-----");
    //dyntest1();
    gsontest2();
    System.out.println("-----test1-----");
    test1();
    System.out.println("-----testN-----");
    //test3();
    //test4();
    //System.out.println("-----test1-----");
    //test1();
    System.out.println("----------");
    System.out.println("Main module: " + Main.class.getModule());
    System.out.println("Integer module: " + Integer.class.getModule());
    System.out.println("UnsafeWrapper module: " + UnsafeWrapper.class.getModule());

  }
}
