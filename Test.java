import java.lang.reflect.*;
import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import java.lang.module.*;
import java.nio.file.Path;
import java.util.Set;

public class Test {

  public static void main(String[] argv) throws Throwable {
    String jarPath = argv[0];
    String moduleDir = argv[1];
    String moduleName = argv[2];
    String className = argv[3];

    Module m = Test.class.getModule();
    System.out.println("main module: " + m);
    System.out.println("java.lang module: " + Integer.class.getModule());

    URLClassLoader u = null;
    try {
      //URLClassLoader u = new URLClassLoader(new URL[]{new URL("file:///Users/jtd/code/java/moduletest/jar/thing/build/libs/obj.jar")});
      //URLClassLoader u = new URLClassLoader(new URL[]{new File("jar/thing/build/libs/obj.jar").toURI().toURL()});
      //URLClassLoader u = URLClassLoader.newInstance(new URL[]{new File("jar/thing/build/libs/obj.jar").toURI().toURL()}, Gson.class.getClassLoader());
      u = new URLClassLoader(new URL[]{new File(jarPath).toURI().toURL()});
      System.out.println(u);
      //Class<?> c = u.loadClass("trust.nccgroup.moduletest.Obj");
      Class<?> c = u.loadClass(className);
      System.out.println(c);
      System.out.println(c.getModule());
    } catch (Throwable t) {
      t.printStackTrace();
    }

    try {
      ModuleFinder finder = ModuleFinder.of(Path.of(moduleDir));
      ModuleLayer parent = ModuleLayer.boot();
      Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of(moduleName));
      ClassLoader scl = ClassLoader.getSystemClassLoader();
      ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);
      //ModuleLayer layer = parent.defineModulesWithOneLoader(cf, u);
      Class<?> c = layer.findLoader(moduleName).loadClass(className);
      System.out.println(c);
      System.out.println(c.getModule());
    } catch (Throwable t) {
      t.printStackTrace();
    }

  }

}
