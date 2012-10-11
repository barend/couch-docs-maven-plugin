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

import java.lang.annotation.Annotation;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * JUnit {@code TestRunner} that can process the {@link EnvironmentCondition} annotation.
 *
 * @author Barend Garvelink <bgarvelink@xebia.com> (https://github.com/barend)
 */
public class ConditionalTestRunner extends BlockJUnit4ClassRunner {

    public ConditionalTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        boolean run = evaluate(getTestClass().getAnnotations())
                   && evaluate(method.getAnnotations());
        if (run) {
            super.runChild(method, notifier);
        } else {
            notifier.fireTestIgnored(describeChild(method));
        }
    }

    private static boolean evaluate(final Annotation[] annotations) {
        boolean run = true;
        for (int i = 0, max = annotations.length; run && i < max; i++) {
            final Annotation annotation = annotations[i];
            if (annotation instanceof EnvironmentCondition) {
                run = EnvironmentCondition.Evaluator.eval((EnvironmentCondition) annotation);
            }
        }
        return run;
    }
}