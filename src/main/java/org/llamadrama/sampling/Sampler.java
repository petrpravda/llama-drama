package org.llamadrama.sampling;

import org.llamadrama.tensor.FloatTensor;

@FunctionalInterface
public interface Sampler {
    int sampleToken(FloatTensor logits);

    Sampler ARGMAX = FloatTensor::argmax;
}
