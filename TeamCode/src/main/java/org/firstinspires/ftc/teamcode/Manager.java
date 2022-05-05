package org.firstinspires.ftc.teamcode;


import org.firstinspires.ftc.teamcode.exceptions.AnnotationNotPresentException;
import org.firstinspires.ftc.teamcode.exceptions.TooFewArgumentsException;
import org.firstinspires.ftc.teamcode.exceptions.UnownedArgumentException;
import org.firstinspires.ftc.teamcode.exceptions.UnownedMethodException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import de.cronn.reflection.util.immutable.ImmutableProxy;


//TODO: Enable the ability to add parameters via a String, with the @Supplied using a String to
//      delineate the normal supplied annotation (is this beneficial?)

//TODO: execManyIf()
//TODO: Scheduled run with logic integration
//TODO: all-to-one variant of execWith
//TODO: String variant of execIf
//TODO: optional passing of telemetry for better error messages

//Idea for more efficient scheduled run: Add automatic actions to a TreeMap associated by strings (for naming)
//so that when a task that has the annotation (which holds the string with the name) runs, that auto task
//is ran. Is this necessary?

/**
 * Manager is the heart of your program. It holds the methods, parameters, and function used to
 * execute your code. Using Manager.Builder, it is trivial to construct a new Manager. Please keep
 * in mind that value-returning functions will not have values stored when called. In order to use
 * the value from a value-returning function, use {@code execWith()}. For conditional execution,
 * use {@code execIf()}.
 * @param <K> The Key representing the functions, which must be directly translatable to Methods.
 *            Ex: RobotFuncs.RAISE_LIFT, which is an Enum that translates to a Method used to raise a lift.
 */
public final class Manager<K extends ToMethod> {
    private final Comparator<Class<?>>      classComparator = Comparator.comparing(Class::getCanonicalName);
    private final Comparator<K>             enumComparator  = Comparator.comparing(Objects::hashCode);
    private final TreeMap<K, Method>        methods         = new TreeMap<>(enumComparator); //Do not directly access this. Use `functions` instead
    private final TreeMap<K, Action>        functions       = new TreeMap<>(enumComparator);
    private final TreeMap<Class<?>, Object> parameters      = new TreeMap<>(classComparator);
    private final TreeMap<String, Logic<? extends ToCallable<Boolean>>> conditionals = new TreeMap<>(); //TODO finish
    private final TreeMap<String, Object> strParameters = new TreeMap<>();
    private int numThreads;
//    private ScheduledThreadPoolExecutor pool;
    private ForkJoinPool pool;
    private ScheduledThreadPoolExecutor delayPool = new ScheduledThreadPoolExecutor(4);

    //the queue represents pending actions. actions pend when they are not allowed to work with itself,
    //so it is put in this queue. to flush the queue, use await() or flushQueue() TODO: flushQueue()

    public Manager(Builder<K> builder) {
        this.numThreads = builder.numThreads;
        this.methods.putAll(builder.functions);
        this.parameters.putAll(builder.parameters);
        this.conditionals.putAll(builder.conditionals);
        this.strParameters.putAll(builder.strParameters);

        Set<K> funcsTemp = methods.keySet();
        Iterator iter = funcsTemp.iterator();
        //TODO: check that this while loop works
        while(iter.hasNext()) {
            K next = (K) iter.next();
            functions.put(next, new Action(next));
        }

        pool = new ForkJoinPool(builder.numThreads);
    }

    private Manager() {
//        pool = new ScheduledThreadPoolExecutor(8);
        pool = new ForkJoinPool(12);
    }



    /**
     * {@code execWith()} enables functions to use their return values in manual functions. It also
     * enables a form of {@code andThen()} by forcing the execution of the value-using function to be executed
     * after the value is acquired. If the value-returning function is {@code Blocking}, then execution
     * in the {@code Manager} chain will wait until the value is passed to the value-using function.
     * If the value-returning function is {@code concurrent}, execution of the value-using function will wait
     * until the value can be used, while execution in the {@code Manager} chain will continue.
     * @param vrFunc Value-returning function
     * @param vuFunc Value-using function
     * @return The current Manager
     */
    public Manager<K> execWith(K vrFunc, K vuFunc) {
        Action vrAct = functions.get(vrFunc);
        Action vuAct = functions.get(vuFunc);

//        assert vrAct != null;
//        assert vuAct != null;

        if(Objects.isNull(vrAct)) {
            throw new UnownedMethodException("Failed to find function with key " + vrFunc +
                    "\n\tDid you ensure to update your Manager with the supplied functions?" +
                    "\n\tDid you ensure to update the use sites of your functions?" +
                    "\n\tDid you ensure to update the ToMethod function?");
        }
        if(Objects.isNull(vuAct)) {
            throw new UnownedMethodException("Failed to find function with key " + vrFunc +
                    "\n\tDid you ensure to update your Manager with the supplied functions?" +
                    "\n\tDid you ensure to update the use sites of your functions?" +
                    "\n\tDid you ensure to update the ToMethod function?");
        }
        actionRun(vrAct, vuAct);

        return this;
    }

    //TODO: make javadoc
    public Manager<K> execWith(K vrFunc, Object[] vrFuncArgs, K vuFunc, Object[] vuFuncArgs) {
        Action vrAct = functions.get(vrFunc);
        Action vuAct = functions.get(vuFunc);
//        assert vrAct != null;
//        assert vuAct != null;
        if(Objects.isNull(vrAct)) {
            throw new UnownedMethodException("Failed to find function with key " + vrFunc +
                    "\n\tDid you ensure to update your Manager with the supplied functions?" +
                    "\n\tDid you ensure to update the use sites of your functions?" +
                    "\n\tDid you ensure to update the ToMethod function?");
        }
        if(Objects.isNull(vuAct)) {
            throw new UnownedMethodException("Failed to find function with key " + vrFunc +
                    "\n\tDid you ensure to update your Manager with the supplied functions?" +
                    "\n\tDid you ensure to update the use sites of your functions?" +
                    "\n\tDid you ensure to update the ToMethod function?");
        }

        actionRun(vrAct, vrFuncArgs, vuAct, vuFuncArgs);

        return this;
    }

    /**
     * {@code actionRun()} serves as a means to make the body of {@code execWith} and its ilk shorter.
     * @param vrAct The action to run, passing the return value to vuAct
     * @param vuAct The action to run using a parameter from vrAct
     */
    private void actionRun(Action vrAct, Action vuAct)  {
        actionRun(vrAct, new Object[0], vuAct, new Object[0]);
    }

    private void actionRun(Action vrAct, Object[] vrFuncArgs, Action vuAct, Object[] vuFuncArgs) {
        if(vrAct.doIgnore()) {
            return;
        }
        Method vrMeth = vrAct.toMethod();

        Concurrent vrAnno = vrMeth.getAnnotation(Concurrent.class);
        if(Objects.isNull(vrAnno)) {
            System.err.println("Unable to find Concurrent annotation for method " +
                    vrMeth.getName() + " when trying to run it.");
            throw new AnnotationNotPresentException("Method "+vrMeth.getName()+" must have a Concurrent annotation");
        }
        if (vrAnno.behavior() == ConcE.CONCURRENT) {
            Future<Object> vrRetValF = vrAct.toFuture(vrFuncArgs);

            pool.execute(() -> { //Submits it to a queue
                try {
                    Object vrRetVal = vrRetValF.get();

                    if(vuAct.doIgnore()) {
                        vuAct.release();
                        return;
                    }
                    Method vuMeth = vuAct.toMethod(); //TODO: CHECK THIS

                    //collect all of the arguments together
                    ArrayList<Object> allVuArgsAL = new ArrayList<>(vuFuncArgs.length + 1);
                    allVuArgsAL.addAll(Arrays.asList(vuFuncArgs));
                    allVuArgsAL.add(vrRetVal);
                    Object[] allVuArgs = allVuArgsAL.toArray();

                    toRunnable(vuMeth, allVuArgs).run();
                    vrAct.release();
                    vuAct.release();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else { //Blocks
            try {
                Object vrRetVal = toCallable(vrMeth, vrFuncArgs).call();
                vrAct.release(); //vrAct is done, release

                if(vuAct.doIgnore()) {
                    vuAct.release();
                    return;
                }
                Method vuMeth = vuAct.toMethod(); //TODO: CHECK THIS
                //collect all of the arguments together
                ArrayList<Object> allVuArgsAL = new ArrayList<>(vuFuncArgs.length + 1);
                allVuArgsAL.add(vrRetVal);
                allVuArgsAL.addAll(Arrays.asList(vuFuncArgs));
                Object[] allVuArgs = allVuArgsAL.toArray();

                toRunnable(vuMeth, allVuArgs).run();
                vuAct.release();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    //TODO: make javadoc
    //TODO: make actionManyRun()
    @SafeVarargs
    public final Manager<K> execManyWith(K vrFunc, K... vuFuncs) {
        Action vrAct = functions.get(vrFunc);
        if(Objects.isNull(vrAct))
            throw new UnownedMethodException("Failed to find function with key " + vrFunc +
                    "\n\tDid you ensure to update your Manager with the supplied functions?"+
                    "\n\tDid you ensure to update the use sites of your functions?"+
                    "\n\tDid you ensure to update the ToMethod function?");

        if(vrAct.doIgnore()) {return this;}
        Method vrMeth = vrAct.toMethod();
        Scheduler vrSchedule = vrMeth.getAnnotation(Scheduler.class);

        List<Method> vuMeths = new ArrayList<>();
        List<Scheduler> vuSchedulers = new ArrayList<>();
        for(K vuFunc : vuFuncs) {
            Action vuAct = functions.get(vuFunc);
            if(vuAct.doIgnore()) continue;
            Method vuMeth = vuAct.toMethod();
            vuMeths.add(vuMeth);
            vuSchedulers.add(vuMeth.getAnnotation(Scheduler.class));
        }

        Concurrent vrAnno = vrMeth.getAnnotation(Concurrent.class);
        if(Objects.isNull(vrAnno))
            throw new AnnotationNotPresentException("Method "+vrFunc+" must have a Concurrent annotation");

        if (vrAnno.behavior() == ConcE.CONCURRENT) {
            Future<Object> vrRetValF = vrAct.toFuture();
            //^get the future representing the return value
            pool.execute(() -> {
                try {
                    Object vrRetVal = vrRetValF.get();
                    vrAct.release();
                    manageSchedulerAnno(vrSchedule);
                    for (int i=0; i<vuMeths.size(); i++) {
                        toRunnable(vuMeths.get(i), vrRetVal).run();
                        functions.get(vuFuncs[i]).release();
                        manageSchedulerAnno(vuSchedulers.get(i));
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else {
            try {
                Object vrRetVal = toCallable(vrMeth).call();
                vrAct.release();
                pool.execute(() -> manageSchedulerAnno(vrSchedule));
                for (int i=0; i<vuMeths.size(); i++) {
                    toRunnable(vuMeths.get(i), vrRetVal).run();
                    functions.get(vuFuncs[i]).release();
                    int finalI = i;
                    pool.execute(() -> manageSchedulerAnno(vuSchedulers.get(finalI)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * Unconditionally execute a function
     * @param key The Method represented by the Manager's K type
     * @param args The manual arguments of the function
     * @return An updated Manager
     */
    public Manager<K> exec(K key, Object... args) {
        Action act = functions.get(key);
        assert act != null;

        if(act.doIgnore()) {return this;}
        Method meth = act.toMethod();

        if (Objects.isNull(meth)) {
            System.out.println("Failed to find function with key " + key +
                    "\n\tDid you ensure to update your Manager with the supplied functions?"+
                    "\n\tDid you ensure to update the use sites of your functions?"+
                    "\n\tDid you ensure to update the ToMethod function?");

            throw new UnownedMethodException("Failed to find function with key " + key +
                    "\n\tDid you ensure to update your Manager with the supplied functions?"+
                    "\n\tDid you ensure to update the use sites of your functions?"+
                    "\n\tDid you ensure to update the ToMethod function?");
        }
        Concurrent behavior = meth.getDeclaredAnnotation(Concurrent.class);
        if(Objects.isNull(behavior)) {
            System.err.println("\n\nERROR IN EXEC FOR FUNCTION " + key + ": Method defined by "+ key+
                    " must have a Behavior annotation.\n\n");
            throw new AnnotationNotPresentException("Method defined by "+key+" must have a Behavior annotation");
        }
        Scheduler schedule = meth.getDeclaredAnnotation(Scheduler.class);
        ConcE concStatus = behavior.behavior();

        switch (concStatus) {
            case CONCURRENT: {
                pool.execute(() -> {
                    toRunnable(meth, args).run();
                    act.release();
                    manageSchedulerAnno(schedule);
                });
                return this;
            }
            case BLOCKING: {
                toRunnable(meth, args).run();
                act.release();
                pool.execute( () -> manageSchedulerAnno(schedule));
                return this;
            }
            default: return this;
        }
    }

    public Manager<K> execIf(K boolF, K func) {
        Action act = functions.get(boolF);
        Action funcAct = functions.get(func);
        assert act != null;
        assert funcAct != null;

        actionRunIf(act, new Object[0], funcAct, new Object[0]);

        return this;
    }

    public Manager<K> execIf(K boolF, Object[] boolFArgs, K func, Object[] runArgs) {
        Action act = functions.get(boolF);
        Action funcAct = functions.get(func);
        assert act != null;
        assert funcAct != null;

        actionRunIf(act, boolFArgs, funcAct, runArgs);
        return this;
    }

    public Manager<K> execIf(K boolF, Object[] boolFArgs, K func) {
        Action act = functions.get(boolF);
        Action funcAct = functions.get(func);
        assert act != null;
        assert funcAct != null;

        actionRunIf(act, boolFArgs, funcAct, new Object[0]);
        return this;
    }
    //TODO: replace asserts in all execIfs with errors

    /**
     * A version of {@code execIf} that uses a {@code Logic} AST instead of a single function. Due to
     * this, all parameters for values stored in AST Literals must be known statically. This is a Good
     * Thing. Always ran concurrently.
     * @param logic A Logic AST
     * @param vuFunc Value-using function
     * @param vuFuncArgs the Value-using function's manually passed arguments. {@code true} is added
     *                   to the end.
     * @param <T> An Algebraic Data Type in which all Data Constructors (inner classes) implement
     *           {@code ToCallable<Boolean>}. If you do not know what an Algebraic Data Type is,
     *           see the examples. //TODO: make examples
     * @return The manager
     */
    public <T extends ToCallable<Boolean>> Manager<K> execIf(Logic<T> logic, K vuFunc, Object[] vuFuncArgs) {
        Action vuAct = functions.get(vuFunc);
        assert vuAct != null;

        //since the logic shouldn't take long, just abort if we cant even run the result, saving computation time
        if(vuAct.doIgnore()) {return this;}
        Method vuMeth = vuAct.toMethod();
        Scheduler schedule = vuMeth.getDeclaredAnnotation(Scheduler.class);

        pool.execute(() -> {
           try {
               boolean logicRes = Logic.toCallable(logic).call();
               if(!logicRes) {
                   vuAct.release();
                   return;
               }

               ArrayList<Object> allVuArgsAL = new ArrayList<>(vuFuncArgs.length + 1);
               allVuArgsAL.addAll(Arrays.asList(vuFuncArgs));
               allVuArgsAL.add(true);
               Object[] allVuArgs = allVuArgsAL.toArray();

               toRunnable(vuMeth, allVuArgs).run();
               vuAct.release();
               manageSchedulerAnno(schedule);
           } catch (ExecutionException | InterruptedException e) {
               e.printStackTrace();
           } catch (Exception e) {
               e.printStackTrace();
           }
        });

        return this;
    }

    public <T extends ToCallable<Boolean>> Manager<K> execIf(Logic<T> logic, K vuFunc) {
        execIf(logic, vuFunc, new Object[0]);
        return this;
    }

    /**
     * See documentation for {@code actionRun()}. This function behaves identically, except it will
     * abort if the action returns false. The action must return a boolean.
     */
    private void actionRunIf(Action vrAct, Object[] vrFuncArgs, Action vuAct, Object[] vuFuncArgs) {
        if(vrAct.doIgnore()) {
            return;
        }

        Method vrMeth = vrAct.toMethod();
//        Method vuMeth = vuAct.toMethod(); //TODO: replace this with individual calls after computing vrMeth?

        if(!vrMeth.getReturnType().equals(boolean.class)) {
            System.out.println("Key " + vrAct + "in execIf must return boolean (must not be Boolean)");
            throw new IllegalArgumentException("Key " + vrAct + "in execIf must return boolean (must not be Boolean)");
        }

        Concurrent vrAnno = vrMeth.getAnnotation(Concurrent.class);
        Scheduler vrSchedule = vrMeth.getAnnotation(Scheduler.class);

        if(Objects.isNull(vrAnno)) {
            System.err.println("Unable to find Concurrent annotation for method " +
                    vrMeth.getName());
            throw new AnnotationNotPresentException("Method defined by "+vrMeth.getName()+" must have a Behavior annotation");
        }
        if (vrAnno.behavior() == ConcE.CONCURRENT) {
            Future<Object> vrRetValF = vrAct.toFuture(vrFuncArgs);

            pool.execute(() -> { //Submits it to a queue
                try {
                    Object vrRetVal = vrRetValF.get();

                    if(vuAct.doIgnore()) {
                        vrAct.release();
                        return;
                    }
                    Method vuMeth = vuAct.toMethod(); //TODO: CHECK THIS VS ABOVE TODO
                    Scheduler vuSchedule = vuMeth.getAnnotation(Scheduler.class);

                    if(vrRetVal.equals(false)) {
                        vrAct.release();
                        vuAct.release();
                        return;
                    }
                    //collect all of the arguments together
                    ArrayList<Object> allVuArgsAL = new ArrayList<>(vuFuncArgs.length + 1);
                    allVuArgsAL.addAll(Arrays.asList(vuFuncArgs));
                    allVuArgsAL.add(vrRetVal); //this is always `true`
                    Object[] allVuArgs = allVuArgsAL.toArray();

                    toRunnable(vuMeth, allVuArgs).run();
                    vrAct.release();
                    manageSchedulerAnno(vrSchedule);
                    vuAct.release();
                    manageSchedulerAnno(vuSchedule);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else { //Blocks
            try {
                Object vrRetVal = toCallable(vrMeth, vrFuncArgs).call();

                if(vuAct.doIgnore()) {
                    vrAct.release();
                    return;
                }
                Method vuMeth = vuAct.toMethod(); //TODO: DITTO
                Scheduler vuSchedule = vuMeth.getAnnotation(Scheduler.class);

                if(vrRetVal.equals(false)) {
                    vrAct.release();
                    vuAct.release();
                    return;
                }

                //collect all of the arguments together
                ArrayList<Object> allVuArgsAL = new ArrayList<>(vuFuncArgs.length + 1);
                allVuArgsAL.add(vrRetVal);
                allVuArgsAL.addAll(Arrays.asList(vuFuncArgs));
                Object[] allVuArgs = allVuArgsAL.toArray();

                toRunnable(vuMeth, allVuArgs).run();
                vrAct.release();
                pool.execute(() -> manageSchedulerAnno(vrSchedule));
                vuAct.release();
                pool.execute(() -> manageSchedulerAnno(vuSchedule));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * A Callable variant of {@code toRunnable()}. Follows the same semantics. Not to be confused with
     * interface {@code ToCallable}.
     * @param method The method passed. Must have a Concurrency annotation.
     * @param args Manually passed arguments denoted by a lack of a Supplied annotation.
     * @param <T> The Callable return type.
     * @return The Callable representing the method applied to the arguments.
     */
    private <T> Callable<T> toCallable(Method method, Object... args) {
        Object[] params = getParams(method, args);

        //return the runnable
        if(method.getParameterTypes().length == 0) {//(method.getParameterCount() == 0) {
            return () -> {
                try {
                    return (T) method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                } catch (ClassCastException e) {
                    System.out.println("Could not cast method invocation to value T");
                    return null;
                }

            };
        }

        return () -> {
            try {
//                System.out.println(Arrays.toString(paramTypes));
                return (T) method.invoke(null, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            } catch (ClassCastException e) {
                System.out.println("Could not cast method invocation to value T");
                return null;
            }
        };
    }

    /**
     * Automatically fills a method with its args, returning it in Runnable form. Arguments marked
     * {@code @Supplied} will be found in the Manager's list of parameters, while argument not marked
     * {@code @Supplied} must be supplied in {@code args}.
     * @param method The method passed. Must have a Concurrency annotation.
     * @param args Manually passed arguments denoted by a lack of a Supplied annotation.
     * @return The Runnable representing the method applied to the arguments.
     */
    private Runnable toRunnable(Method method, Object... args) {
        Object[] params = getParams(method, args);

        //return the runnable
        if(method.getParameterTypes().length == 0){//(method.getParameterCount() == 0) { //FIXME: SDK UPDATE WHEN?
            return () -> {
                try {
                    method.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
        }
        //Check if anything has an annotation. If so, replace those by the args

        return () -> {
            try {
                method.invoke(null, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        };

    }

    /**
     * {@code getParams()} gets the parameters of a method supplied with a list of manual values.
     * Used by {@code toRunnable()} and {@code toCallable()}.
     */
    private Object[] getParams(Method method, Object... args) {
        int parameterCount = method.getParameterTypes().length;//method.getParameterCount();
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[]   params = new Object[parameterCount];
        Boolean[]  paramMkdSupplied = new Boolean[parameterCount];
        //^ Keeps track (linearly) of if parameters are marked Supplied. Makes it much quicker than re-double-iterating

        Annotation[][] annotations = method.getParameterAnnotations();
        int numAnno = parameterCount;
        for(int k=0; k<annotations.length; k++) {
            if(Arrays.stream(annotations[k]).noneMatch(arg -> arg instanceof Supplied)) {
                paramMkdSupplied[k] = false;
            }
            else {
                numAnno--;
                paramMkdSupplied[k] = true;
            }
//            for(int i=0; i<annotations[k].length; i++) {
//                if(annotations[k][i] instanceof Supplied) {
//                    numAnno--;
//                    paramMkdSupplied[k] = true;
//                }
//            }
        }
        //Fetch the automatic parameters, erroring if it is unable to find arguments which are Supplied
        for(int i=0; i<parameterCount; i++) {
            params[i] = parameters.get(paramTypes[i]);
            if(Objects.isNull(params[i]) //TreeMap.get will return null when it can't find anything
                    && !parameters.containsKey(paramTypes[i])
                    //^It may be useful to include a null in the Treemap, so ensure it isn't present
                    && paramMkdSupplied[i]) { //check if it is marked Supplied, which would require it to be in the TreeMap
                throw new UnownedArgumentException("Unable to supply arguments which are marked Supplied." +
                        "\n\tIn position " + i + " (from 0) with type " + paramTypes[i].getCanonicalName() +
                        "\n\tIn call for method " + method.getName() +
                        "\n\tWith manual arguments " + Arrays.toString(args) +
                        "\n\tPerhaps you should use a shared Environment or BotConfig parameter?");
            }
        }

        //Check if anything has an annotation. If so, replace those by the args
        if(numAnno > 0) {
            if(numAnno > args.length) { //TODO: ENSURE THIS DOESN'T CAUSE ANY BUGS
                System.err.println("Number of arguments provided in args must be equal to number " +
                        "required for Method "+method.getName()+", as determined by the number of Supplied annotations on arguments");
                throw new TooFewArgumentsException("Number of arguments provided in args must be equal to number " +
                        "required for the Method, as determined by the number of Supplied annotations on arguments");
            }
            int argNum = 0;
            for(int i=0; i<parameterCount; i++) {
                if(Objects.isNull(params[i])) {
                    params[i] = args[argNum];
                    argNum++;
                }
            }
        }
        return params;
    }


    /**
     * Wait for all given tasks to complete before continuing, allowing a Concurrent function
     * to wait without being marked as Blocking. This will wait a maximum of 10 seconds.
     * @return Itself
     */
    public Manager<K> await() {
        try {
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
            delayPool.shutdown();
            delayPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        restartPools();
        return this;
    }

    private void manageSchedulerAnno(Scheduler schedule) {
        if(Objects.isNull(schedule)) {
            return;
        }
        Class<? extends ToMethod> actionC = schedule.action();
        String[] strings = schedule.args();
        long runAfter = schedule.runAfter();
        TimeUnit unit = schedule.unit();

        Object[] args = new Object[strings.length];
        for(int i=0; i<strings.length; i++) {
            args[i] = strParameters.get(strings[i]);
        }


        try {
            Method actionMeth = (Method) actionC.getMethod("toMethod").invoke(actionC.newInstance());
            Runnable actionRun = toRunnable(actionMeth, args);
            delayPool.schedule(actionRun, runAfter, unit);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    private void restartPools() {
        pool = new ForkJoinPool(numThreads);
        delayPool = new ScheduledThreadPoolExecutor(numThreads/2);
    }

    public void displayPoolStatistics() {
        System.out.println("\033[" +
                "Targeted # of active threads   : "   + pool.getParallelism() +
                "\n# Threads stealing/executing   : " + pool.getActiveThreadCount() +
                "\n# Threads not blocking/waiting : " + pool.getRunningThreadCount() +
                "\n# Tasks waiting for execution  : " + pool.getQueuedSubmissionCount() +
                "\n# Tasks in worker thread queues: " + pool.getQueuedTaskCount() +
                "\n# Tasks stolen                 : " + pool.getStealCount() +
                "\n"
        );
    }

    //**********************************************************************************************
    public static class Builder<T extends ToMethod> {

        private final Comparator<Class<?>>      classComparator = Comparator.comparing(Class::getCanonicalName);
        private final Comparator<T>             enumComparator  = Comparator.comparing(Objects::hashCode);
        private final TreeMap<T, Method>        functions       = new TreeMap<>(enumComparator);
        private final TreeMap<Class<?>, Object> parameters      = new TreeMap<>(classComparator);
        private final TreeMap<String, Logic<? extends ToCallable<Boolean>>> conditionals = new TreeMap<>();
        private final TreeMap<String, Object> strParameters = new TreeMap<>();
        private int numThreads = 8;

        private Builder() {}

        public static <T extends ToMethod> Builder<T> builder() {
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
                throw new UnownedMethodException("Failed to find function with key " + key +
                        "\n\tDid you ensure to update your Manager with the supplied functions?"+
                        "\n\tDid you ensure to update the use sites of your functions?"+
                        "\n\tDid you ensure to update the ToMethod function?");
            }
            return this;
        }

        /**
         * Add an automatically passed parameter. This function is unsafe because of potential
         * concurrent usage of the variables. <strong>If you use the Immutable </strong>
         * It is safer to use {@code addParameter()} instead, but the
         * object must utilize the reflection-utils package provided with DoubleDrive to use the safe version.
         * Use this function if you know what you are doing and are sure your code follows
         * a {@code ReaderT IO@-like} design pattern. The best use case for this method is adding things like
         * gamepads.
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
                throw new IllegalArgumentException("Cannot put more than one automatic argument of" +
                        "the same Type in a Manager");
            }
            parameters.put(param.getClass(), param);

            return this;
        }

        /**
         * This is an alias for {@code addParameterUnsafe()}. It exists to make your code look nicer.
         * Use it only if you are really adding a parameter that is immutable or has an {@code @Value.Immutable}
         * annotation. It is not possible to confirm if that annotation exists at runtime, so follow
         * best practices.
         * @param param The auto-parameter
         * @return an updated Builder.
         */
        public Builder<T> addImmutableParameter(Object param) {
            addParameterUnsafe(param);
            return this;
        }

        public Builder<T> addImmutableParameter(Object param, Class<?> clazz) {
            if(parameters.containsKey(clazz)) {
                System.err.println("\n\nERROR: Cannot put more than one automatic argument of the " +
                        "same Class into a Manager.");
                throw new IllegalArgumentException("Cannot put more than one automatic argument of" +
                        "the same Type in a Manager");
            }
            parameters.put(clazz, param);

            return this;
        }

        public Builder<T> addStrParameter(String name, Object param) {
            strParameters.putIfAbsent(name, param);
            return this;
        }

        /**
         * Add an automatically passed parameter. Please read the implementation notes.
         * @param param The auto-parameter
         * @return An updated Builder
         * @implNote Due to storage in a TreeMap, parameters must be unique. Immutability
         * <strong>is</strong> enforced. In order to add a parameter using this function, it should
         * be an ImmutableProxy from the reflection-utils library. This is not fool-proof, however.
         * For example, {@code param.getFoo().unsafeChange(bar)} could be done and will not be caught, but may not
         * be thread-safe. <strong>All access to shared state must be synchronized,
         * not just modifications.</strong>
         */
        public Builder<T> addMutableParameter(Object param) {
            if(parameters.containsKey(param.getClass())) {
                System.err.println("\n\nERROR: Cannot put more than one automatic argument of the " +
                        "same Class into a Manager.");
                throw new IllegalArgumentException("Cannot put more than one automatic argument of" +
                        "the same Type in a Manager");
            }

            Object immutableParam = ImmutableProxy.create(param);
            parameters.put(param.getClass(), immutableParam);

            return this;
        }

        /**
         * Add custom logic to your Manager. Logic is polymorphic in its type, but requires the type
         * to implement a ToCallable that returns a Boolean. Logic alleviates the need for boilerplate
         * conditional actions/keys, making your code cleaner and the logic more concise. Logic
         * enables simpler bottom-up design of conditional expressions built upon basic primitives
         * and combined to create larger, complex structures.
         * @param name The name to hereby refer to this conditional as
         * @param logic The Logic to add
         * @return An updated Builder
         */
        public Builder<T> addConditional(String name, Logic<? extends ToCallable<Boolean>> logic) {
            conditionals.put(name, logic);
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
    //**********************************************************************************************
    /**
     * Actions serve to hold {@code K}s in order to manager their ability to be executed. It holds a Semaphore
     * and the action along with some utility methods.
     */
    private class Action implements ToMethod {
        private final Semaphore available; //= new Semaphore(1, true);
        private K action;

        private boolean requirePermit;
        private boolean ignoreAsyncCall;

        private Action() {
            available = new Semaphore(1, true);
        }

        public Action(K action) {
            available = new Semaphore(1, true);
            this.action = action;
            Method actionM = methods.get(action);
            assert actionM != null;
            if(Objects.isNull(actionM)) {
                throw new UnownedMethodException("Failed to find function with key " + action +
                        "\n\tDid you ensure to update your Manager with the supplied functions?"+
                        "\n\tDid you ensure to update the use sites of your functions?"+
                        "\n\tDid you ensure to update the ToMethod function?");
            }


            Concurrent actionAnno = actionM.getAnnotation(Concurrent.class);
            assert actionAnno != null;
            if(Objects.isNull(actionAnno))
                throw new AnnotationNotPresentException("Method defined by "+action+" must have a Behavior annotation");

            requirePermit = !actionAnno.allowAsync();
            ignoreAsyncCall = actionAnno.ignoreOnAsync();
        }

        @Override
        public Method toMethod(){
            //Check to see if we need to wait for access for this method
            if(requirePermit) {
                try {
                    available.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Method actionMeth = null;
            try {
                actionMeth = action.toMethod();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            assert actionMeth != null;
            return actionMeth;
        }

        /**
         * Allow this method to be fetched again
         */
        public void release() {
            if(requirePermit) {
                available.release();
            }
        }

        public Future<Object> toFuture() {
            return toFuture(null);
        }

        public Future<Object> toFuture(Object ...args) {
            Method actionM = methods.get(action);
            return pool.submit( () -> toCallable(actionM, args).call());
        }

        public boolean doIgnore() {
            return available.availablePermits() == 0 && ignoreAsyncCall;
        }

    }
}
