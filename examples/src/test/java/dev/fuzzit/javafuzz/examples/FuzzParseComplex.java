package com.gitlab.javafuzz.examples;

import com.gitlab.javafuzz.core.AbstractFuzzTarget;


/**
 * Unit test for simple App.
 */
public class FuzzParseComplex extends AbstractFuzzTarget
{
    public void fuzz(byte[] data)
    {
        App.parseComplex(data);
    }
}
