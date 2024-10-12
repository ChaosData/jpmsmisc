package trust.nccgroup.agenttest;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.util.Date;

import trust.nccgroup.UnsafeWrapper;

@SuppressWarnings("unused")
public class Agent {

  public static void premain(String args, Instrumentation inst) {
    agentmain(args, inst);
  }

  public static void agentmain(String args, Instrumentation inst) {
    try {
      System.out.println(Class.forName("sun.misc.Unsafe"));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    try {
      System.out.println(Class.forName("jdk.internal.misc.Unsafe"));
    } catch (Throwable t) {
      t.printStackTrace();
    }
    Object iunsafe = null;
    try {
      ResettableClassFileTransformer rcft1 = new AgentBuilder.Default()
        .disableClassFormatChanges()
        .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
        //.with(new AgentBuilder.Listener.StreamWriting(System.err))
        .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
        .type(ElementMatchers.named("java.util.Date"))
        .transform(
          (builder, _cl, _td, _mod, _pd) -> {
            return builder.visit(
              Advice.to(CloneWrapper.class)
                .on(
                  ElementMatchers.named("clone")
                                 .and(ElementMatchers.takesArguments(0))
                )
            );
          }
        )
        .installOn(inst);

      ResettableClassFileTransformer rcft2 = new AgentBuilder.Default()
        .disableClassFormatChanges()
        .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
        //.with(new AgentBuilder.Listener.StreamWriting(System.err))
        .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
        .type(ElementMatchers.named("java.util.Date"))
        .transform(
          (builder, _cl, _td, _mod, _pd) -> {
            return builder.visit(
              Advice.to(EqualsWrapper.class)
                .on(
                  ElementMatchers.named("equals")
                                 .and(ElementMatchers.takesArguments(1))
                )
            );
          }
        )
        .installOn(inst);

      System.out.println(UnsafeWrapper.class);
      System.out.println(UnsafeWrapper.class.getModule());

      System.setProperty("NCC_UNSAFE_PLEASE", "1");
      Date key = new Date(42);
      iunsafe = key.clone();
      key.equals(UnsafeWrapper.class);
      System.clearProperty("NCC_UNSAFE_PLEASE");
      rcft1.reset(inst, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
      rcft2.reset(inst, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);

      System.out.println(UnsafeWrapper.class);
      System.out.println(UnsafeWrapper.class.getModule());
    } catch (Throwable t) {
      t.printStackTrace();
    }

    if (iunsafe != null) {
      try {
        UnsafeWrapper.init(iunsafe);

        Field moduleField = Class.class.getDeclaredField("module");
        long moduleFieldOffset = (long)(Long)UnsafeWrapper.invoke("objectFieldOffset", new Class<?>[]{Field.class}, moduleField);
        System.out.println("Class.module offset: " + moduleFieldOffset);

        Field errField = System.class.getDeclaredField("err");
        long errFieldOffset = (long)(Long)UnsafeWrapper.invoke("staticFieldOffset", new Class<?>[]{Field.class}, errField);
        System.out.println("System.err offset: " + errFieldOffset);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  static class CloneWrapper {
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static Object enter(@Advice.This Object self) {
      if (((Date)self).getTime() == 42) {
        if ("1".equals(System.getProperty("NCC_UNSAFE_PLEASE"))) {
          try {
            Class<?> iunsafeClass = Class.forName("jdk.internal.misc.Unsafe");
            Method getUnsafe = iunsafeClass.getMethod("getUnsafe");
            Object iunsafe = getUnsafe.invoke(null);
            return iunsafe;
          } catch (Throwable t) {
            t.printStackTrace();
          }
        }
      }
      return null;
    }

    @Advice.OnMethodExit
    static void exit(@Advice.Enter Object enterret, @Advice.Return(readOnly=false, typing=Assigner.Typing.DYNAMIC) Object ret) {
      if (enterret != null) {
        ret = enterret;
      }
    }
  }

  static class EqualsWrapper {
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    static Object enter(@Advice.This Object self,
                        @Advice.Argument(readOnly=false, typing=Assigner.Typing.DYNAMIC, value = 0) Object obj) {
      if (((Date)self).getTime() == 42 && obj instanceof Class) {
        if ("1".equals(System.getProperty("NCC_UNSAFE_PLEASE"))) {
          try {
            Class<?> iunsafeClass = Class.forName("jdk.internal.misc.Unsafe");
            Method getUnsafe = iunsafeClass.getMethod("getUnsafe");
            Object iunsafe = getUnsafe.invoke(null);
            Method getInt = iunsafeClass.getDeclaredMethod("getInt", Object.class, long.class);
            Method putInt = iunsafeClass.getDeclaredMethod("putInt", Object.class, long.class, int.class);
            Method objectFieldOffset = iunsafeClass.getDeclaredMethod("objectFieldOffset", Class.class, String.class);
            long module_off = (Long)objectFieldOffset.invoke(iunsafe, Class.class, "module");
            Class<?> objclass = (Class<?>)obj;
            Class<?> ownclass = self.getClass();
            int javabase = (Integer)getInt.invoke(iunsafe, ownclass, module_off);
            putInt.invoke(iunsafe, objclass, module_off, javabase);
            return "skip";
          } catch (Throwable t) {
            t.printStackTrace();
          }
        }
      }
      return null;
    }

    @Advice.OnMethodExit
    static void exit(@Advice.Enter Object enterret, @Advice.Return(readOnly=false, typing=Assigner.Typing.DYNAMIC) Object ret) {
      //System.out.println("[agent] enterret: " + enterret);
      if (enterret != null) {
        ret = false;
      }
    }
  }
}
