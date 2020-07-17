package com.google.sps.servlets;

import static org.junit.Assert.*;

import java.util.*;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for creating valid Location objects */
@RunWith(JUnit4.class)
public final class SimpleMatrixTest {

  @Test
  public void testMul() throws Exception {
    float[] m = new float[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    SimpleMatrix matrix = new SimpleMatrix(2, 5, true, m);

    float[] v = new float[] {1, 0, 1, 0, 1};
    SimpleMatrix vec = new SimpleMatrix(1, 5, true, v);

    SimpleMatrix result = matrix.mult(vec.transpose());
    assertEquals(2, result.getNumElements());
    assertEquals(9.0, result.get(0, 0), 0.001);
    assertEquals(24.0, result.get(1, 0), 0.001);
  }

  @Test
  public void testNormalize() throws Exception {
    float[] v = new float[] {1, 0, 1, 0, 1};
    SimpleMatrix vec = new SimpleMatrix(1, 5, true, v);
    double magnitude = vec.normF();
    assertEquals(Math.sqrt(3), magnitude, 0.001);
    SimpleMatrix normalized = vec.divide(magnitude);
    for (int i = 0; i < v.length; i++) {
      double expected = i % 2 == 0 ? (1 / Math.sqrt(3)) : 0;
      assertEquals(expected, normalized.get(0, i), 0.001);
    }
  }
}
