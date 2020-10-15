package com.gitlab.javafuzz.core;

public abstract class AbstractFuzzTarget {
    public abstract void fuzz(byte[] data);
}
