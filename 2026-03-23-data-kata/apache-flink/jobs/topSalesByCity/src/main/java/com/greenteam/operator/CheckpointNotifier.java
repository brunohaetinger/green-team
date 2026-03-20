package com.greenteam.operator;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.CheckpointListener;
import org.apache.flink.util.Collector;
import com.greenteam.openlineage.OpenLineageIntegration;
import com.greenteam.config.JobConfig;

public class CheckpointNotifier<T> extends RichFlatMapFunction<T, T> implements CheckpointListener {
    private final OpenLineageIntegration lineage;

    public CheckpointNotifier(OpenLineageIntegration lineage) {
        this.lineage = lineage;
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
}
