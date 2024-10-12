// /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents//Home/include/jni.h
// clang -std=c11 -I "$(/usr/libexec/java_home)/include" -I "$(/usr/libexec/java_home)/include/darwin" -shared -o test.dylib test.c
// gcc -std=c11 -fPIC -I /opt/java/openjdk/include -I /opt/java/openjdk/include/linux -shared -o test.so test.c
#include <stdio.h>
#include <string.h>

#include <jni.h>
#include <jvmti.h>

__attribute__((constructor))
void myctor() {
  //puts("myctor() called");
}

jvmtiEnv* jvmti = NULL;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
  //puts("my JNI_OnLoad() called");
  jvmti = NULL;
  (*vm)->GetEnv(vm, (void **)&jvmti, JVMTI_VERSION_1_0);
  //printf("jvmti: %p\n", jvmti);

  return JNI_VERSION_1_8;
}

//JNIEXPORT jobject JNICALL Java_trust_nccgroup_moduletest_Main_doNative(JNIEnv *env, jobject _this) {
JNIEXPORT jobject JNICALL Java_trust_nccgroup_UnsafeWrapper_nativeInit(JNIEnv *env, jobject _this) {
  //puts("nativeInit() called");

  jclass unsafe = NULL;
  /*
  jclass unsafe = (*env)->FindClass(env, "sun/misc/Unsafe");
  if ((*env)->ExceptionCheck(env)) {
    jthrowable t = (*env)->ExceptionOccurred(env);
    jclass cls = (*env)->GetObjectClass(env, t);
    jmethodID pst = (*env)->GetMethodID(env, cls, "printStackTrace", "()V");
    (*env)->CallVoidMethod(env, t, pst);
    (*env)->ExceptionClear(env);
  }
  //jclass unsafe_cls = env->GetObjectClass(env, unsafe);
  //jmethodID pst = env->GetMethodID(unsafe_cls, "toString", "()");
  */

  jint class_count;
  jclass* classes;
  jvmtiError err = (*jvmti)->GetLoadedClasses(jvmti, &class_count, &classes);

  //printf("class_count: %d\n", class_count);
  jclass class_class = (*env)->FindClass(env, "java/lang/Class");
  jmethodID class_getName = (*env)->GetMethodID(env, class_class, "getName", "()Ljava/lang/String;");
  jmethodID class_getModule = (*env)->GetMethodID(env, class_class, "getModule", "()Ljava/lang/Module;");
  jobject class_module = (*env)->CallObjectMethod(env, class_class, class_getModule);

  jclass unsafewrapper_class = (*env)->GetObjectClass(env, _this);
  jobject unsafewrapper_module = (*env)->CallObjectMethod(env, unsafewrapper_class, class_getModule);

  jclass klass = NULL;
  int jdk_internal = 0;

  for (size_t i=0; i<class_count; i++) {
    jclass klass = classes[i];
    jstring name = (*env)->CallObjectMethod(env, klass, class_getName);
    const char *name_cstr = (*env)->GetStringUTFChars(env, name, 0);
    //printf("class: %s\n", name_cstr);
    if (strstr(name_cstr, ".misc.Unsafe") != NULL) {
      if (strcmp("sun.misc.Unsafe", name_cstr) == 0) {
        //puts("sun.misc.Unsafe");
        unsafe = klass;
        break;
      } else if (strcmp("jdk.internal.misc.Unsafe", name_cstr) == 0) {
        //puts("jdk.internal.misc.Unsafe");
        unsafe = klass;
        jdk_internal = 1;
        break;
      }
    }
  }

  jobject theUnsafe = NULL;
  if (unsafe != NULL) {
    //printf("unsafe: %p\n", unsafe);
    //jfieldID unsafe_theUnsafe = (*env)->GetStaticFieldID(env, unsafe, "theUnsafe", jdk_internal ? "jdk/internal/misc/Unsafe" : "sun/misc/Unsafe");
    //jfieldID unsafe_theUnsafe = (*env)->GetStaticFieldID(env, unsafe, jdk_internal == 1 ? "theUnsafe" : "theInternalUnsafe", "jdk/internal/misc/Unsafe");
    //printf("unsafe_theUnsafe: %p\n", unsafe_theUnsafe);

    //jclass reflected_field_class = (*env)->FindClass(env, "java/lang/reflect/Field");
    //jmethodID reflected_field_toString = (*env)->GetMethodID(env, reflected_field_class, "toString", "()Ljava/lang/String;");

    jint field_count;
    jfieldID* fields;
    jvmtiError err = (*jvmti)->GetClassFields(jvmti, unsafe, &field_count, &fields);
    //printf("unsafe field_count: %d\n", field_count);
    jint accessFlags;
    for (size_t i=0; i<field_count; i++) {
      char* name_ptr;
      /*(*jvmti)->GetFieldModifiers(jvmti, unsafe, fields[i], &accessFlags);
      jobject reflected_theUnsafe = (*env)->ToReflectedField(env, unsafe, fields[i], accessFlags & 0x0008);
      jstring s = (*env)->CallObjectMethod(env, reflected_theUnsafe, reflected_field_toString);
      const char *s_cstr = (*env)->GetStringUTFChars(env, s, 0);
      puts(s_cstr);*/
      (*jvmti)->GetFieldName(jvmti, unsafe, fields[i], &name_ptr, NULL, NULL);
      if (strcmp(name_ptr, jdk_internal ? "theUnsafe" : "theInternalUnsafe") == 0) {
        theUnsafe = (*env)->GetStaticObjectField(env, unsafe, fields[i]);
      }
      (*jvmti)->Deallocate(jvmti, (void*)name_ptr);
      if (theUnsafe != NULL) {
        break;
      }
    }
    (*jvmti)->Deallocate(jvmti, (void*)fields);

    if (theUnsafe != NULL) {
      jint method_count;
      jmethodID* methods;
      jvmtiError err = (*jvmti)->GetClassMethods(jvmti, unsafe, &field_count, &methods);
      jmethodID objectFieldOffset = NULL;
      jmethodID getLong = NULL;
      jmethodID putLong = NULL;

      int count = 0;
      for (size_t i=0; i<method_count; i++) {
        char* name_ptr;
        char* sig_ptr;
        (*jvmti)->GetMethodName(jvmti, methods[i], &name_ptr, &sig_ptr, NULL);
        if (strcmp("objectFieldOffset", name_ptr) == 0 && strcmp("(Ljava/lang/reflect/Field;)J", sig_ptr) == 0) {
          //printf("objectFieldOffset sig: %s\n", sig_ptr);
          objectFieldOffset = methods[i];
          count++;
        } else if (strcmp("getLong", name_ptr) == 0 && strcmp("(Ljava/lang/Object;J)J", sig_ptr) == 0) {
          //printf("getLong sig: %s\n", sig_ptr);
          getLong = methods[i];
          count++;
        } else if (strcmp("putLong", name_ptr) == 0 && strcmp("(Ljava/lang/Object;JJ)V", sig_ptr) == 0) {
          //printf("putLong sig: %s\n", sig_ptr);
          putLong = methods[i];
          count++;
        }
        (*jvmti)->Deallocate(jvmti, (void*)name_ptr);
        (*jvmti)->Deallocate(jvmti, (void*)sig_ptr);
        if (count == 3) {
          break;
        }
      }
      if (count == 3) {
        jfieldID class_module_fieldID = (*env)->GetFieldID(env, class_class, "module", "Ljava/lang/Module;");
        jobject class_module_field = (*env)->ToReflectedField(env, class_class, class_module_fieldID, 0);
        //printf("class_module_field: %p\n", class_module_field);
        //printf("objectFieldOffset: %p\n", objectFieldOffset);
        jlong moduleFieldOffset = (*env)->CallLongMethod(env, theUnsafe, objectFieldOffset, class_module_field);
        //printf("moduleFieldOffset: %p\n", (void*)moduleFieldOffset);

        //long javalangModule = (long)(Long)theUnsafe_method_getLong.invoke(theUnsafe, Integer.class, moduleFieldOffset);
        jlong javabase_module = (*env)->CallLongMethod(env, theUnsafe, getLong, class_class, moduleFieldOffset);
        //printf("javabase_module: %p\n", (void*)javabase_module);
        (*env)->CallVoidMethod(env, theUnsafe, putLong, unsafewrapper_class, moduleFieldOffset, javabase_module);
      } else {
        puts("??????????");
      }

      (*jvmti)->Deallocate(jvmti, (void*)methods);
    }

  }

  (*env)->DeleteLocalRef(env, class_class);
  (*jvmti)->Deallocate(jvmti, (void*)classes);

  return theUnsafe;
}
