package com.greenteam;

import org.apache.flink.core.execution.JobStatusChangedListener;
import org.apache.flink.core.execution.JobStatusChangedListenerFactory;

public class CustomStatusChangedListenerFactory implements JobStatusChangedListenerFactory {
    @Override
    public JobStatusChangedListener createListener(Context context) {
        JobStatusChangedListener customListener = new JobStatusListener();
        JobStatusChangedListener openLineageListener = null;
        try {
            // Tenta carregar a factory do OpenLineage via reflection (nome correto do JAR)
            Class<?> factoryClass = Class.forName("io.openlineage.flink.listener.OpenLineageJobStatusChangedListenerFactory");
            JobStatusChangedListenerFactory factory = (JobStatusChangedListenerFactory) factoryClass.getDeclaredConstructor().newInstance();
            openLineageListener = factory.createListener(context);
        } catch (Exception e) {
            // Não encontrou o OpenLineage, segue só com o custom
        }
        if (openLineageListener == null) {
            return customListener;
        }
        // Composite listener: chama ambos
        JobStatusChangedListener finalOpenLineageListener = openLineageListener;
        return event -> {
            customListener.onEvent(event);
            finalOpenLineageListener.onEvent(event);
        };
    }
}
