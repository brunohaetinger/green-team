package com.greenteam;

import org.apache.flink.core.execution.JobStatusChangedListener;
import org.apache.flink.core.execution.JobStatusChangedListenerFactory;

public class GreenTeamJobStatusChangedListenerFactory implements JobStatusChangedListenerFactory {
    @Override
    public JobStatusChangedListener createListener(Context context) {
        return new JobStatusListener();
    }
}
