package org.firstinspires.ftc.teamcode;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;

//Manager is the builder of the entire config. functions, the RobotConfig class, special drive modes,
//etc. will all live here. functions should live here because it would allow Manager to pass arguments
//to the functions. Should a method not be found, it does nothing.
//TODO: make sure to assert that the number of arguments in the argument array == [Callable].getClass().getEnclosingMethod().getParameterCount. e
//pass references via explicit lambda, default arguments from State interface/class?

//K allows the user to refer to the functions by anything they please. likely a String or enum

//EXIT STATUSES: TODO: (use these over assertions?)
// 10: Method to add cannot be found
// 11: Method does not have a Behavior annotation
// 12: @args@ provided does not equal arguments required for the @Method@, as determined
//     by the @@Supplied@ annotations
public class Manager<K extends ToMethod<K>> {
    private Comparator<Class<?>> classComparator = Comparator.comparing(Class::getCanonicalName);
    private final ExecutorService pool;
    private final TreeMap<K, Method> functions = new TreeMap<>();
    private final TreeMap<Class<?>, Object> parameters = new TreeMap<>(classComparator);


    public Manager() {
        pool = newFixedThreadPool(10);
    }

    //automatically supplies arguments
    private Runnable toRunnable(Method method, Object... args) {
        int parameterCount = method.getParameterCount();
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[parameterCount];

//        assert(params.length == args.length);

        Annotation[][] annotations = method.getParameterAnnotations();
        int numAnno = parameterCount;
        for(Annotation[] annoArr : annotations) {
            for(Annotation anno : annoArr) {
                if(anno instanceof Supplied) {numAnno--;}
            }
        }

        for(int i=0; i<parameterCount; i++) {
            params[i] = parameters.get(paramTypes[i]);
            System.out.println(parameters);

            if(Objects.isNull(params[i]) && parameters.containsKey(paramTypes[i])) {
                System.err.println("\n\nNumber of arguments provided in args must be equal to number " +
                        "required for the Method, as determined by the number of Supplied annotations on arguments");
                assert(false);
            }
        }

        //END COMMON CODE
        if(method.getParameterCount() == 0) {
            return () -> {
                try {
                    method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        }
        //Check if anything has an annotation. If so, replace those by the args
        if(numAnno > 0) {
            if(numAnno != args.length) {
                System.err.println("\n\nNumber of arguments provided in args must be equal to number " +
                        "required for the Method, as determined by the number of Supplied annotations on arguments");
                assert(false);
            }
            int argNum = 0;
            for(int i=0; i<parameterCount; i++) {
                 if(Objects.isNull(params[i])) {
                    params[i] = args[argNum];
                    argNum++;
                 }
             }
        }

        //return the final method call
        return () -> {
            try {
//                System.out.println(Arrays.toString(paramTypes));
                method.invoke(null, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        };

    }

    public Manager<K> addFunc(K key) {
        try {
            functions.putIfAbsent(key, key.toMethod(key));
        }
        catch (NoSuchMethodException e) {
            System.err.println("Error: Cannot find method associated with key " + key);
            assert(false);
//            System.exit(10);
        }
        return this;
    }

    public Manager<K> addParameter(Object param) {
        if(parameters.containsKey(param.getClass())) {
            System.err.println("\n\nERROR: Cannot put more than one automatic argument of the " +
                    "same Class into a Manager.");
            assert(false);
        }
        parameters.put(param.getClass(), param);

        return this;
    }

    public void exec(K key, Object... args) {
        Method func = functions.get(key);
        assert func != null;
        Concurrent behavior = func.getDeclaredAnnotation(Concurrent.class);
        if(Objects.isNull(behavior)) {
            System.err.println("\n\nERROR IN EXEC FOR FUNCTION " + key + ": Method defined by "+ key+
                    " must have a Behavior annotation.\n\n");
            assert(false);
//            System.exit(11);
        }
        ConcE concStatus = behavior.behavior();

        switch (concStatus) {
            case CONCURRENT: pool.execute(toRunnable(func, args));
            case BLOCKING:   toRunnable(func, args).run();
        }
    }

//    public Manager<K> addMethodSource(Class clazz) {
//        methodSources.add(clazz);
//        return this;
//    }
//
//    private Method searchSources() {
//        Method meth;
//        for(Class<?> clazz : methodSources) {
//            try {
//                Method tempMeth = clazz.
//            }
//        }
//        return meth;
//    }
//    public Manager<K> addFunc(K key, Runnable runnable, String funcName) {
//        try {
//            functions.putIfAbsent(key, runnable.getClass().getMethod(funcName));
//        } catch (NoSuchMethodException e) {
//            System.out.println("Could not find method with name " + funcName);
//            e.printStackTrace();
//        }
//        return this;
//    }
//    public Manager<K> addFunc(K key, Method method) {
//
//        return this;
//    }
}
