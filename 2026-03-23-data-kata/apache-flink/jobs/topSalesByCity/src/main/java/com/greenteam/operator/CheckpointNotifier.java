package com.greenteam.operator;

import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.CheckpointListener;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import com.greenteam.openlineage.OpenLineageIntegration;
import com.greenteam.config.JobConfig;

public class CheckpointNotifier<T> extends RichFlatMapFunction<T, T> implements CheckpointListener {
    private final String jobExecutionId;
    private transient OpenLineageIntegration lineage;

    public CheckpointNotifier(String jobExecutionId) {
        this.jobExecutionId = jobExecutionId;
    }

    @Override
    public void open(OpenContext parameters) {
        this.lineage = new OpenLineageIntegration(jobExecutionId);
    }

    @Override
    public void flatMap(T value, Collector<T> out) {
        out.collect(value);
    }

    @Override
    public void notifyCheckpointComplete(long checkpointId) {
        lineage.emitKafkaToKafkaEvent(
            JobConfig.INPUT_TOPIC,
            JobConfig.OUTPUT_TOPIC,
            io.openlineage.client.OpenLineage.RunEvent.EventType.RUNNING
        );
    }

    @Override
    public void close() throws Exception {
        if (lineage != null) {
            lineage.close();
        }
    }
}
