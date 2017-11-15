package io.atlasmap.actions;

import org.junit.Test;

import io.atlasmap.v2.Action;
import io.atlasmap.v2.Lowercase;
import io.atlasmap.v2.Uppercase;

public class JavaScratchTest {

    @Test
    public void testIsAssignableVsInstanceOf() {
        Action action = new Uppercase();

        int f = 0;
        int j = 0;
        long instStart = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            if (action instanceof Uppercase) {
                j++;
            }
            if (action instanceof Lowercase) {
                f++;
            }
        }
        long instEnd = System.currentTimeMillis();

        int k = 0;
        long isassignStart = System.currentTimeMillis();
        for (int i = 0; i < 5000000; i++) {
            if (action.getClass().isAssignableFrom(Uppercase.class)) {
                k++;
            }
            if (action.getClass().isAssignableFrom(Lowercase.class)) {
                f++;
            }
        }
        long isassignEnd = System.currentTimeMillis();

        System.out.println("Instance of: " + (instEnd - instStart) + " (ms)");
        System.out.println("IsAssignable: " + (isassignEnd - isassignStart) + " (ms)");
    }

    @Test
    public void testActionClassNames() {
        System.out.println(Lowercase.class.getSimpleName());
    }
}
