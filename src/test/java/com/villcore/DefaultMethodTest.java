package com.villcore;

import org.junit.Test;

public class DefaultMethodTest {
    interface IA {
        default void print() {
            System.out.println("IA");
        }
    }

    interface IB {
        default void print() {
            System.out.println("IB");
        }
    }

    //class Impl implements IA, IB{}
    @Test
    public void defaultMethodTest() {

    }
}
