package com.greenteam;

import org.apache.flink.core.execution.JobStatusChangedListener;
import org.apache.flink.core.execution.JobStatusChangedListenerFactory;

public class CustomStatusChangedListenerFactory implements JobStatusChangedListenerFactory {
    @Override
    public JobStatusChangedListener createListener(Context context) {
        JobStatusChangedListener customListener = new JobStatusListener();
        JobStatusChangedListener openLineageListener = null;
        try {
            // try to load the OpenLineage listener factory via reflection, 
            // this allows us to integrate with OpenLineage if it's available on the classpath without creating a hard dependency on it, 
            // and if it's not available, we can still use our custom listener without any issues.
            Class<?> factoryClass = Class.forName("io.openlineage.flink.listener.OpenLineageJobStatusChangedListenerFactory");
            JobStatusChangedListenerFactory factory = (JobStatusChangedListenerFactory) factoryClass.getDeclaredConstructor().newInstance();
            openLineageListener = factory.createListener(context);
        } catch (Exception e) {
            // If the OpenLineage listener factory is not available, we simply ignore it and use only our custom listener.
            System.out.println("OpenLineage listener factory not found, using only custom listener.");
        }
        if (openLineageListener == null) {
            return customListener;
        }
        // If both listeners are available, we create a composite listener that calls both of them when an event occurs.
        JobStatusChangedListener finalOpenLineageListener = openLineageListener;
        return event -> {
            customListener.onEvent(event);
            finalOpenLineageListener.onEvent(event);
        };
    }
}
