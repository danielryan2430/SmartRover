package edu.ucsb.cs.capstone.letmypeoplecode.smartrover.test;

import android.test.InstrumentationTestCase;

/*
Steps to make this work:
1. Run -> Edit Configurations
2. Add an "Android Tests" config, name it whatever
3. Choose "All in package", set package to
edu.ucsb.cs.capstone.letmypeoplecode.smartrover.test
4. Add a filter in logcat to catch messages with "TestRunner" if you
want to see the test output
5. When you run it, Event Log should say Tests failed
 */


public class ExampleTest extends InstrumentationTestCase {
    public void test() throws Exception {
        final int expected = 5;
        final int reality = 5;
        assertEquals("OH SHIT NOT 5", expected, reality);
    }
}
