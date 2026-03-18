package com.greenteam;

import org.apache.flink.core.execution.JobExecutionStatusEvent;
import org.apache.flink.core.execution.JobStatusChangedEvent;
import org.apache.flink.core.execution.JobStatusChangedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JobStatusListener implements JobStatusChangedListener {
    private static final Logger LOG = LoggerFactory.getLogger(JobStatusListener.class);
    @Override
    public void onEvent(JobStatusChangedEvent event) {
        if (isJobCreatedEvent(event)) {
            LOG.info(
                    "job_created jobId={} jobName={} executionMode={} lineageGraph={} eventClass={}",
                    event.jobId(),
                    event.jobName(),
                    invokeNoArgMethod(event, "executionMode"),
                    invokeNoArgMethod(event, "lineageGraph"),
                    event.getClass().getName()
            );
            return;
        }

        if (event instanceof JobExecutionStatusEvent statusEvent) {
            if (statusEvent.exception() == null) {
                LOG.info(
                        "job_status_changed jobId={} jobName={} oldStatus={} newStatus={}",
                        statusEvent.jobId(),
                        statusEvent.jobName(),
                        statusEvent.oldStatus(),
                        statusEvent.newStatus()
                );
            } else {
                LOG.warn(
                        "job_status_changed jobId={} jobName={} oldStatus={} newStatus={} exception={}",
                        statusEvent.jobId(),
                        statusEvent.jobName(),
                        statusEvent.oldStatus(),
                        statusEvent.newStatus(),
                        statusEvent.exception().toString(),
                        statusEvent.exception()
                );
            }
            return;
        }

        LOG.info(
                "job_status_event jobId={} jobName={} eventClass={}",
                event.jobId(),
                event.jobName(),
                event.getClass().getName()
        );
    }
    private boolean isJobCreatedEvent(JobStatusChangedEvent event) {
        return hasNoArgMethod(event, "executionMode") && hasNoArgMethod(event, "lineageGraph");
    }

    private boolean hasNoArgMethod(Object target, String methodName) {
        try {
            target.getClass().getMethod(methodName);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Object invokeNoArgMethod(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return "unavailable";
        }
    }
}