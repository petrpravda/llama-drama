package org.llamadrama.tensor;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.llamadrama.gguf.GGMLType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FloatTensorTest {
    private TestFloatTensor tensor;
    private static final float DELTA = 1e-6f;

    @BeforeEach
    void setUp() {
        tensor = new TestFloatTensor(new float[]{1.0f, 2.0f, 3.0f, 4.0f});
    }

    @Test
    void testSize() {
        assertEquals(4, tensor.size());
    }

    @Test
    void testGetFloat() {
        assertEquals(1.0f, tensor.getFloat(0), DELTA);
        assertEquals(2.0f, tensor.getFloat(1), DELTA);
        assertEquals(3.0f, tensor.getFloat(2), DELTA);
        assertEquals(4.0f, tensor.getFloat(3), DELTA);
    }

    @Test
    void testSetFloat() {
        tensor.setFloat(0, 5.0f);
        assertEquals(5.0f, tensor.getFloat(0), DELTA);
    }

    @Test
    void testNumberOfElements() {
        assertEquals(6, FloatTensor.numberOfElements(2, 3));
        assertEquals(24, FloatTensor.numberOfElements(2, 3, 4));
    }

    @Test
    void testDot() {
        TestFloatTensor other = new TestFloatTensor(new float[]{2.0f, 3.0f, 4.0f, 5.0f});
        float result = tensor.dot(0, other, 0, 4);
        assertEquals(40.0f, result, DELTA); // (1*2 + 2*3 + 3*4 + 4*5)
    }

    @Test
    void testMatmul() {
        TestFloatTensor a = new TestFloatTensor(new float[]{1.0f, 2.0f, 3.0f, 4.0f});
        TestFloatTensor b = new TestFloatTensor(new float[]{5.0f, 6.0f});
        TestFloatTensor out = new TestFloatTensor(new float[]{0.0f, 0.0f});

        a.matmul(b, out, 2, 2);

        assertEquals(17.0f, out.getFloat(0), DELTA); // 1*5 + 2*6
        assertEquals(39.0f, out.getFloat(1), DELTA); // 3*5 + 4*6
    }

    @Test
    void testReduce() {
        float sum = tensor.reduce(0, 4, 0.0f, Float::sum);
        assertEquals(10.0f, sum, DELTA);

        float max = tensor.reduce(0, 4, Float.NEGATIVE_INFINITY, Float::max);
        assertEquals(4.0f, max, DELTA);
    }

    @Test
    void testSum() {
        assertEquals(10.0f, tensor.sum(0, 4), DELTA);
    }

    @Test
    void testMax() {
        assertEquals(4.0f, tensor.max(0, 4), DELTA);
    }

    @Test
    void testArgmax() {
        assertEquals(3, tensor.argmax(0, 4));
    }

    @Test
    void testMapInPlace() {
        tensor.mapInPlace(value -> value * 2);
        assertArrayEquals(
            new float[]{2.0f, 4.0f, 6.0f, 8.0f},
            tensor.getData(),
            DELTA
        );
    }

    @Test
    void testMapWithIndexInPlace() {
        tensor.mapWithIndexInPlace(0, 4, (value, index) -> value + index);
        assertArrayEquals(
            new float[]{1.0f, 3.0f, 5.0f, 7.0f},
            tensor.getData(),
            DELTA
        );
    }

    @Test
    void testAddInPlace() {
        TestFloatTensor other = new TestFloatTensor(new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        tensor.addInPlace(other);
        assertArrayEquals(
            new float[]{2.0f, 3.0f, 4.0f, 5.0f},
            tensor.getData(),
            DELTA
        );
    }

    @Test
    void testMultiplyInPlace() {
        TestFloatTensor other = new TestFloatTensor(new float[]{2.0f, 2.0f, 2.0f, 2.0f});
        tensor.multiplyInPlace(other);
        assertArrayEquals(
            new float[]{2.0f, 4.0f, 6.0f, 8.0f},
            tensor.getData(),
            DELTA
        );
    }

    @Test
    void testDivideInPlace() {
        tensor.divideInPlace(0, 4, 2.0f);
        assertArrayEquals(
            new float[]{0.5f, 1.0f, 1.5f, 2.0f},
            tensor.getData(),
            DELTA
        );
    }

    @Test
    void testFillInPlace() {
        tensor.fillInPlace(0, 4, 7.0f);
        assertArrayEquals(
            new float[]{7.0f, 7.0f, 7.0f, 7.0f},
            tensor.getData(),
            DELTA
        );
    }

    @Test
    void testSoftmaxInPlace() {
        tensor.softmaxInPlace(0, 4);
        float sum = tensor.sum(0, 4);
        assertEquals(1.0f, sum, DELTA);
        assertTrue(tensor.getFloat(3) > tensor.getFloat(0));
    }

    @Test
    void testSaxpyInPlace() {
        TestFloatTensor other = new TestFloatTensor(new float[]{1.0f, 2.0f, 3.0f, 4.0f});
        tensor.saxpyInPlace(0, other, 0, 4, 2.0f);
        assertArrayEquals(
            new float[]{3.0f, 6.0f, 9.0f, 12.0f},
            tensor.getData(),
            DELTA
        );
    }

    @Test
    void testCopyTo() {
        TestFloatTensor dest = new TestFloatTensor(new float[4]);
        tensor.copyTo(0, dest, 0, 4);
        assertArrayEquals(tensor.getData(), dest.getData(), DELTA);
    }

    // Helper test implementation of FloatTensor
    private static class TestFloatTensor extends FloatTensor {
        private final float[] data;

        TestFloatTensor(float[] data) {
            this.data = data;
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public float getFloat(int index) {
            return data[index];
        }

        @Override
        public void setFloat(int index, float value) {
            data[index] = value;
        }

        @Override
        FloatVector getFloatVector(VectorSpecies<Float> species, int offset) {
            throw new UnsupportedOperationException("Vector operations not supported in test implementation");
        }

        @Override
        GGMLType type() {
            return GGMLType.F32;
        }

        float[] getData() {
            return data;
        }
    }
}
