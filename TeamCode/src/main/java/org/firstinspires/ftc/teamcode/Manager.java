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


    /**
     *
     * @param method The method passed. Must have a Concurrency annotation.
     * @param args Manually passed arguments denoted by a lack of a Supplied annotation
     * @return The Runnable representing the method applied to the arguments
     */
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
        //Fetch the automatic parameters, erroring if it is unable to find arguments which are Supplied
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

        //return the runnable
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

    /**
     * Call a function given to the Builder
     * @param key The function represented by the Manager's K type
     * @param args The manual arguments
     * @return Itself
     */
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

    /**
     * Wait for all given tasks to complete before continuing, allowing a Concurrent function
     * to wait without being marked as Blocking
     * @return Itself
     */
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

        /**
         * Set the max number of threads to be used concurrently
         * @param threads The max number of threads
         * @return An updated builder
         */
        public Builder<T> setThreads(int threads) {
            numThreads = threads;
            return this;
        }

        /**
         * Add a function to the builder to be used by the Manager
         * @param key The function to add
         * @return An updated Builder
         */
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

        /**
         * Add an automatically passed parameter. This function is unsafe because of potential
         * concurrent usage of the variables. It is safer to use addParameter() instead, but the
         * object must utilize the Immutables package provided with DoubleDrive. Use this function
         * if you know what you are doing and are sure your code follows a @ReaderT IO@-like design
         * pattern.
         * @param param The auto-parameter
         * @return An updated Builder
         * @implNote Due to storage in a TreeMap, parameters must be unique. They also ought to be
         * immutable because of potential concurrent access. Note that immutability is not enforced,
         * so it is necessary to ensure best practices. <strong>All access to shared state must be synchronized,
         * not just modifications.</strong>
         */
        public Builder<T> addParameterUnsafe(Object param) {
            if(parameters.containsKey(param.getClass())) {
                System.err.println("\n\nERROR: Cannot put more than one automatic argument of the " +
                        "same Class into a Manager.");
                assert(false);
            }
            parameters.put(param.getClass(), param);

            return this;
        }

        /**
         *
         * @param param The auto-parameter
         * @return An updated Builder
         * @implNote Due to storage in a TreeMap, parameters must be unique. Immutability
         * <strong>is</strong> enforced. In order to add a parameter using this function, it must
         * have an Immutability annotation from the Immutables library. This is not fool-proof, however.
         * For example, @param.foo().unsafeChange(bar)@ could be done and will not be caught, but may not
         * be thread-safe. <strong>All access to shared state must be synchronized,
         * not just modifications.</strong>
         */
        public Builder<T> addParameter(Object param) {
            if(parameters.containsKey(param.getClass())) {
                System.err.println("\n\nERROR: Cannot put more than one automatic argument of the " +
                        "same Class into a Manager.");
                assert(false);
            }
            //TODO: make this check for Immutability annotation
            return this;
        }

        /**
         * Finalize the Builder, preventing further changes
         * @return The Manager represented by the Builder
         */
        public Manager<T> build() {
            return new Manager<>(this);
        }
    }
}
