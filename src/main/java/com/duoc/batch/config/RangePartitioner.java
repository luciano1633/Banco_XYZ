package com.duoc.batch.config;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import java.util.HashMap;
import java.util.Map;

public class RangePartitioner implements Partitioner {
    private final int gridSize;
    private final Resource resource;
    private final int totalLines;
    private final int linesToSkip;

    public RangePartitioner(Resource resource, int totalLines, int gridSize, int linesToSkip) {
        this.resource = resource;
        this.totalLines = totalLines;
        this.gridSize = gridSize;
        this.linesToSkip = linesToSkip;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();
        int linesPerPartition = (totalLines - linesToSkip) / gridSize;
        int remainder = (totalLines - linesToSkip) % gridSize;
        int start = linesToSkip;
        for (int i = 0; i < gridSize; i++) {
            int end = start + linesPerPartition - 1;
            if (i < remainder) {
                end++;
            }
            ExecutionContext context = new ExecutionContext();
            context.putInt("partitionId", i);
            context.putInt("startLine", start);
            context.putInt("endLine", end);
            context.putString("fileName", resource.getFilename());
            result.put("partition" + i, context);
            start = end + 1;
        }
        return result;
    }
}
