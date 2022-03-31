package org.firstinspires.ftc.teamcode;

import static java.util.concurrent.Executors.newFixedThreadPool;

import com.qualcomm.robotcore.util.ThreadPool;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

//Manager is the builder of the entire config. functions, the RobotConfig class, special drive modes,
//etc. will all live here. functions should live here because it would allow Manager to pass arguments
//to the functions. Should a method not be found, it does nothing.

//K allows the user to refer to the functions by anything they please. Enums allow implementations
//to be easily swapped out for one another, or even mixed and matched

//EXIT STATUSES: TODO: (use these over assertions?)
// 10: Method to add cannot be found
// 11: Method does not have a Behavior annotation
// 12: @args@ provided does not equal arguments required for the @Method@, as determined
//     by the @@Supplied@ annotations
public final class Manager<K extends ToMethod> {
    private final Comparator<Class<?>>      classComparator = Comparator.comparing(Class::getCanonicalName);
    private final Comparator<K>             enumComparator  = Comparator.comparing(Objects::hashCode);
    private final TreeMap<K, Method>        functions       = new TreeMap<>(enumComparator);
    private final TreeMap<Class<?>, Object> parameters      = new TreeMap<>(classComparator);
    private int numThreads;
    private ScheduledThreadPoolExecutor pool;


    public Manager(Builder<K> builder) {
        this.numThreads = builder.numThreads;
        this.functions.putAll(builder.functions);
        this.parameters.putAll(builder.parameters);
        pool = new ScheduledThreadPoolExecutor(builder.numThreads);//newFixedThreadPool(10);
    }

    private void restartPool() {
        pool = new ScheduledThreadPoolExecutor(numThreads);
    }

    private Manager() {
        pool = new ScheduledThreadPoolExecutor(10);
    }


    //automatically supplies arguments
    private Runnable toRunnable(Method method, Object... args) {
        int parameterCount = method.getParameterCount();
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[]   params = new Object[parameterCount];
        Boolean[]  paramMkdSupplied = new Boolean[parameterCount];
        //^ Keeps track (linearly) of if parameters are marked Supplied. Makes it much quicker than re-double-iterating

//        assert(params.length == args.length);
        Annotation[][] annotations = method.getParameterAnnotations();
        int numAnno = parameterCount;
        for(int k=0; k<annotations.length; k++) {
            if(annotations[k].length == 0) {
                paramMkdSupplied[k] = false;
            }
            for(int i=0; i<annotations[k].length; i++) {
                if(annotations[k][i] instanceof Supplied) {
                    numAnno--;
                    paramMkdSupplied[k] = true;
                }
            }
        }

        for(int i=0; i<parameterCount; i++) {
            params[i] = parameters.get(paramTypes[i]);
            if(Objects.isNull(params[i]) //TreeMap.get will return null when it can't find anything
                    && !parameters.containsKey(paramTypes[i])
                    //^It may be useful to include a null in the Treemap, so ensure it isn't present
                    && paramMkdSupplied[i]) { //check if it is marked Supplied, which would require it to be in the TreeMap
                System.err.println("\n\nFATAL: Unable to supply arguments which are marked Supplied." +
                        "\n\tIn position " + i + " (from 0) with type " + paramTypes[i].getCanonicalName() +
                        "\n\tIn call for method " + method.getName() +
                        "\n\tWith manual arguments " + Arrays.toString(args) +
                        "\n\tPerhaps you should use a shared State parameter?");
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

    public Manager<K> exec(K key, Object... args) {
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
            case CONCURRENT: {
                pool.execute(toRunnable(func, args));
                return this;
            }
            case BLOCKING: {
                toRunnable(func, args).run();
                return this;
            }
            default: return this;
        }
    }

    public Manager<K> await() {
        try {
            pool.shutdown();
            pool.awaitTermination(7, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        restartPool();
        return this;
    }

//    public Manager<K> addFunc(K key) {
//        try {
//            functions.putIfAbsent(key, key.toMethod());
//        }
//        catch (NoSuchMethodException e) {
//            System.err.println("ERROR: Cannot find method associated with key " + key +
//                    "\n\tPerhaps you need to update your ToMethod instance?");
//            assert(false);
////            System.exit(10);
//        }
//        return this;
//    }
//
//    public Manager<K> addParameter(Object param) {
//        if(parameters.containsKey(param.getClass())) {
//            System.err.println("\n\nERROR: Cannot put more than one automatic argument of the " +
//                    "same Class into a Manager.");
//            assert(false);
//        }
//        parameters.put(param.getClass(), param);
//
//        return this;
//    }

    //**********************************************************************************************
    public static class Builder<T extends ToMethod> {

        private final Comparator<Class<?>>      classComparator = Comparator.comparing(Class::getCanonicalName);
        private final Comparator<T>             enumComparator  = Comparator.comparing(Objects::hashCode);
        private final TreeMap<T, Method>        functions       = new TreeMap<>(enumComparator);
        private final TreeMap<Class<?>, Object> parameters      = new TreeMap<>(classComparator);
        private int numThreads = 10;

        private Builder() {}

        public static <T extends ToMethod> Builder<T> newBuilder() {
            return new Builder<>();
        }

        public Builder<T> setThreads(int threads) {
            numThreads = threads;
            return this;
        }

        public Builder<T> addFunc(T key) {
            try {
                functions.putIfAbsent(key, key.toMethod());
            }
            catch (NoSuchMethodException e) {
                System.err.println("ERROR: Cannot find method associated with key " + key +
                        "\n\tPerhaps you need to update your ToMethod instance?");
                assert(false);
//            System.exit(10);
            }
            return this;
        }

        public Builder<T> addParameter(Object param) {
            if(parameters.containsKey(param.getClass())) {
                System.err.println("\n\nERROR: Cannot put more than one automatic argument of the " +
                        "same Class into a Manager.");
                assert(false);
            }
            parameters.put(param.getClass(), param);

            return this;
        }

        public Manager<T> build() {
            return new Manager<>(this);
        }
    }
}
