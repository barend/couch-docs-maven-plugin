/*
   Copyright 2012 Xebia Nederland B.V.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.xebia.os.maven.couchdocsplugin.junit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation works with {@link ConditionalTestRunner} to enable a test only if a given environment
 * variable is set.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvironmentCondition {

    /**
     * The name of the condition that should be present for the test to run.
     */
    String name();

    /**
     * The kind of environment condition that should be present for the test to run.
     * @return
     */
    Kind kind();

    static enum Kind { ENVIRONMENT_VARIABLE, SYSTEM_PROPERTY }

    static class Evaluator {
        static boolean eval(EnvironmentCondition condition) {
            boolean run = true;
            switch(condition.kind()) {
            case ENVIRONMENT_VARIABLE:
                run = (System.getenv(condition.name()) != null);
                break;
            case SYSTEM_PROPERTY:
                run = (System.getProperty(condition.name()) != null);
                break;
            }
            return run;
        }
    }
}
