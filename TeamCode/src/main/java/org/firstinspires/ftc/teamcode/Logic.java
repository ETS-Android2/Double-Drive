package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;

/**
 * This is an AST for combining actions that use boolean values. It may feel clunky, but it prevents
 * boilerplate declarations combining multiple checking functions. While this class is polymorphic
 * {@code ∀ a}, {@code <T extends ToCallable<Boolean>>} is likely what you will slot here.
 * //TODO: examples
 * @param <A> The value at the leaves of the AST
 * @implNote If you are reading the source code, it may be more beneficial to read the Haskell
 * pseudocode this is based off of, as Java tends to result in boilerplate-y code and Haskell is
 * far more suited for this than Java will ever be. If you don't know Haskell, either suffer the
 * boilerplate or learn it, it's worth it!
 */
public abstract class Logic<A> {
    public abstract <R> R accept (Visitor<A, R> visitor);

    private Logic() {}

    public interface Visitor<A, R> {
        R visit(And<A> and);
        R visit(Or <A> or );
        R visit(Not<A> not);
        R visit(Lit<A> lit);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class And<A> extends Logic<A> {
        private final Logic<A> left;
        private final Logic<A> right;

        public And(Logic<A> left, Logic<A> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<A, R> visitor) {
            return visitor.visit(this);
        }

        public Logic<A> getLeft() {
            return left;
        }

        public Logic<A> getRight() {
            return right;
        }
    }
    public static <T> And<T> And(Logic<T> l, Logic<T> r) {
        return new And<>(l,r);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Or <A> extends Logic<A> {
        private final Logic<A> left;
        private final Logic<A> right;

        public Or(Logic<A> left, Logic<A> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<A, R> visitor) {
            return visitor.visit(this);
        }

        public Logic<A> getLeft() {
            return left;
        }

        public Logic<A> getRight() {
            return right;
        }
    }
    public static <T> Or<T> Or(Logic<T> l, Logic<T> r) {
        return new Or<>(l,r);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Not<A> extends Logic<A> {
        private final Logic<A> val;

        public Not(Logic<A> val) {
            this.val = val;
        }

        @Override
        public <R> R accept(Visitor<A, R> visitor) {
            return visitor.visit(this);
        }

        public Logic<A> getVal() {
            return val;
        }
    }
    public static <T> Not<T> Not(Logic<T> v) {
        return new Not<>(v);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    public static class Lit<A> extends Logic<A> {
        private final A lit;

        public Lit(A lit) {
            this.lit = lit;
        }

        @Override
        public <R> R accept(Visitor<A, R> visitor) {
            return visitor.visit(this);
        }

        public A getLit() {
            return lit;
        }
    }
    public static <T> Lit<T> Lit(T a) {
        return new Lit<>(a);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * See Haskell implementation, as it is self-explanatory
     * @param op A Logic AST
     * @return A {@code Callable} computing the AST
     */
    //TODO: make this package-private
    public static Callable<Boolean> toCallableBools(Logic<Boolean> op) {
        return op.accept(
                new Visitor<Boolean, Callable<Boolean>>() {
                    @Override
                    public Callable<Boolean> visit(And<Boolean> and) {
                        Logic<Boolean> l = and.getLeft();
                        Logic<Boolean> r = and.getRight();
                        return () -> (
                                toCallableBools(l).call() && toCallableBools(r).call()
                        );
                    }

                    @Override
                    public Callable<Boolean> visit(Or<Boolean> or) {
                        Logic<Boolean> l = or.getLeft();
                        Logic<Boolean> r = or.getRight();
                        return () -> (
                                toCallableBools(l).call() || toCallableBools(r).call()
                        );
                    }

                    @Override
                    public Callable<Boolean> visit(Not<Boolean> not) {
                        Logic<Boolean> v = not.getVal();
                        return () -> (
                                !toCallableBools(v).call()
                                );
                    }

                    @Override
                    public Callable<Boolean> visit(Lit<Boolean> lit) {
                        boolean a = lit.getLit();
                        return () -> (
                                a
                                );
                    }
                }
        );
    }

    /**
     * See Haskell implementation, as it is self-explanatory. The Visitor pattern necessarily
     * obscures the definition. The Haskell implementation is not bound by this restriction
     * because Haskell has infinitely better support for Algebraic Data Types.
     * @param op A Logic AST
     * @param <T> {@code T ~ a} in the type of {@code ToCallable a => Logic a -> Callable Bool}
     * @return A {@code Callable} computing the AST
     */
    //TODO: make this package-private
    public static <T extends ToCallable<Boolean>> Callable<Boolean> toCallable(@NonNull Logic<T> op) {
        return op.accept(
                new Visitor<T, Callable<Boolean>>() {
                    @Override
                    public Callable<Boolean> visit(And<T> and) {
                        Logic<T> l = and.getLeft();
                        Logic<T> r = and.getRight();

                        return () -> (
                                toCallable(l).call() && toCallable(r).call()
                        );
                    }

                    @Override
                    public Callable<Boolean> visit(Or<T> or) {
                        Logic<T> l = or.getLeft();
                        Logic<T> r = or.getRight();

                        return () -> (
                                toCallable(l).call() || toCallable(r).call()
                        );
                    }

                    @Override
                    public Callable<Boolean> visit(Not<T> not) {
                        Logic<T> v = not.getVal();

                        return () -> (
                                !toCallable(v).call()
                        );
                    }

                    @Override
                    public Callable<Boolean> visit(Lit<T> lit) {
                        try {
                            boolean a = lit.getLit().toCallable().call();
                            return () -> (a);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        throw new IllegalArgumentException("Unable to extract bool value out of" +
                                "conditional literal. Call threw an exception");
                    }
                }
        );
    }


    /*Haskell implementation of Logic
    data Logic a = And (Logic a) (Logic a)
                 | Or  (Logic a) (Logic a)
                 | Not (Logic a)
                 | Lit a

     instance ToMethod a => ToCallable (Logic a) where
            --this is 100% pseudocode, RHS is effectively Java
            toCall :: ToCallable a Bool => Logic a -> Callable Bool
            toCall (And l r) = () -> return ((toCall l).call() && (toCall r).call())
            toCall (Or  l r) = () -> return ((toCall l).call() || (toCall r).call())
            toCall (Not c)   = () -> return (not $ (toCall l).call())
            toCall (Lit a)   = () -> return a
     --note that in the pseudocode ToCallable there would be a Functional Dependency in the typeclass
     --declaration with `a -> r`
     */

}
