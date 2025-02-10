package org.llamadrama.gguf;

import org.llamadrama.tensor.FloatTensor;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class GGUFloatTensor {
    public static Map<String, GGMLTensorEntry> loadTensors(FileChannel fileChannel, long tensorDataOffset, Map<String, GGUF.GGUFTensorInfo> tensorInfos) throws IOException {
        Arena arena = Arena.ofAuto();
        MemorySegment tensorData = fileChannel.map(FileChannel.MapMode.READ_ONLY, tensorDataOffset, fileChannel.size() - tensorDataOffset, arena);
        Map<String, GGMLTensorEntry> tensorEntries = HashMap.newHashMap(tensorInfos.size());
        for (Map.Entry<String, GGUF.GGUFTensorInfo> entry : tensorInfos.entrySet()) {
            GGUF.GGUFTensorInfo ti = entry.getValue();
            int numberOfElements = FloatTensor.numberOfElements(ti.dimensions());
            int sizeInBytes = Math.toIntExact(ti.ggmlType().byteSizeFor(numberOfElements));
            MemorySegment memorySegment = tensorData.asSlice(ti.offset(), sizeInBytes);
            tensorEntries.put(ti.name(), new GGMLTensorEntry(tensorData, ti.name(), ti.ggmlType(), ti.dimensions(), memorySegment));
        }
        return tensorEntries;
    }
}
