package org.firstinspires.ftc.teamcode;

import java.util.concurrent.Callable;

/**
 * This is an AST for combining actions that use boolean values. It may feel clunky, but it prevents
 * boilerplate declarations combining multiple checking functions. While this class is polymorphic
 * {@code ∀ a}, {@code boolean} is likely what you will slot here.
 * //TODO: examples
 * @param <A> The value at the leaves of the AST
 * @implNote If you are reading the source code, it may be more beneficial to read the Haskell
 * pseudocode this is based off of, as Java tends to result in boilerplate-y code and Haskell is
 * far more suited for this than Java ever will be. If you don't know Haskell, either suffer the
 * boilerplate or learn it, it's worth it!
 */
public abstract class Logic<A> {
    public abstract <R> R accept (Visitor<A, R> visitor);
    private Manager<?> manager;

    private Logic() {}

    public Logic(Manager<?> manager) {
        this.manager = manager;
    }

    public interface Visitor<A, R> {
        R visit(And<A> and);
        R visit(Or <A> or );
        R visit(Not<A> not);
        R visit(Lit<A> lit);
    }

    public static class And<A> extends Logic<A> {
        private Logic<A> left;
        private Logic<A> right;

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

    public static class Or<A> extends Logic<A> {
        private Logic<A> left;
        private Logic<A> right;

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

    public static class Not<A> extends Logic<A> {
        private Logic<A> val;

        @Override
        public <R> R accept(Visitor<A, R> visitor) {
            return visitor.visit(this);
        }

        public Logic<A> getVal() {
            return val;
        }
    }

    public static class Lit<A> extends Logic<A> {
        private A lit;

        @Override
        public <R> R accept(Visitor<A, R> visitor) {
            return visitor.visit(this);
        }

        public A getLit() {
            return lit;
        }
    }

    /**
     * See Haskell implementation, as it is self-explanatory
     * @param op A Logic AST
     * @return A {@code Callable} computing the AST
     */
    private static Callable<Boolean> toCallable(Logic<Boolean> op) {
        return op.accept(
                new Visitor<Boolean, Callable<Boolean>>() {
                    @Override
                    public Callable<Boolean> visit(And<Boolean> and) {
                        Logic<Boolean> l = and.getLeft();
                        Logic<Boolean> r = and.getRight();
                        return () -> (
                                toCallable(l).call() && toCallable(r).call()
                        );
                    }

                    @Override
                    public Callable<Boolean> visit(Or<Boolean> or) {
                        Logic<Boolean> l = or.getLeft();
                        Logic<Boolean> r = or.getRight();
                        return () -> (
                                toCallable(l).call() || toCallable(r).call()
                        );
                    }

                    @Override
                    public Callable<Boolean> visit(Not<Boolean> not) {
                        Logic<Boolean> v = not.getVal();
                        return () -> (
                                !toCallable(v).call()
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

    //TODO: compute()

    /* NOTICE HOW MUCH NICER HASKELL IS?
    data Bools a = And Bools Bools
                 | Or Bools Bools
                 | Not Bools
                 | Lit a

    instance ToCallable a => Compute (Bools a) where
        compute :: ToCallable a => Bools a -> Bool
        compute (And l r) = exec (toCall l) && exec (toCall r)
        compute (Or  l r) = exec (toCall l) || exec (toCall r)
        compute (Not c)   = not . exec $ toCall c
        compute (Lit a)   = exec $ toCall a

     instance ToMethod a => ToCallable (Bools a) where
            --this is 100% pseudocode, RHS is effectively Java
            toCall :: ToMethod a => Bools a -> Callable Bool
            toCall (And l r) = () -> return ((toCall l).call() && (toCall r).call())
            toCall (Or  l r) = () -> return ((toCall l).call() || (toCall r).call())
            toCall (Not c)   = () -> return (not $ (toCall l).call())
            toCall (Lit a)   = () -> return a
     */

}
