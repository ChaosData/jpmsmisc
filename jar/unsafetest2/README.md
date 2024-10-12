# poking around at Java 9+'s module system

```
$ PATH=/path/to/jdk-17.0.1+12/Contents/Home/bin:$PATH ./gradlew jar

BUILD SUCCESSFUL in 619ms
2 actionable tasks: 2 executed

$ PATH=/path/to/jdk-17.0.1+12/Contents/Home/bin:$PATH java -p build/libs -m trust.nccgroup.moduletest/trust.nccgroup.moduletest.Main
main module: module trust.nccgroup.moduletest
java.lang module: module java.base
java.lang.reflect.InaccessibleObjectException: Unable to make field private final int java.lang.Integer.value accessible: module java.base does not "opens java.lang" to module trust.nccgroup.moduletest
	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:354)
	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:297)
	at java.base/java.lang.reflect.Field.checkCanSetAccessible(Field.java:178)
	at java.base/java.lang.reflect.Field.setAccessible(Field.java:172)
	at trust.nccgroup.moduletest/trust.nccgroup.moduletest.Main.test1(Main.java:12)
	at trust.nccgroup.moduletest/trust.nccgroup.moduletest.Main.main(Main.java:66)
unsafe: sun.misc.Unsafe@28a418fc
Class.module offset: 48
Main.class.module: -4325654125372351871
Integer.class.module: 3287835804
[0, 42, 2]
done

$ PATH=/path/to/jdk-17.0.1+12/Contents/Home/bin:$PATH java -jar build/libs/moduletest.jar
main module: unnamed module @647c3190
java.lang module: module java.base
java.lang.reflect.InaccessibleObjectException: Unable to make field private final int java.lang.Integer.value accessible: module java.base does not "opens java.lang" to unnamed module @647c3190
	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:354)
	at java.base/java.lang.reflect.AccessibleObject.checkCanSetAccessible(AccessibleObject.java:297)
	at java.base/java.lang.reflect.Field.checkCanSetAccessible(Field.java:178)
	at java.base/java.lang.reflect.Field.setAccessible(Field.java:172)
	at trust.nccgroup.moduletest.Main.test1(Main.java:12)
	at trust.nccgroup.moduletest.Main.main(Main.java:66)
unsafe: sun.misc.Unsafe@5c647e05
Class.module offset: 48
Main.class.module: -2623915780821376
Integer.class.module: 4294355952
[0, 42, 2]
done
```
