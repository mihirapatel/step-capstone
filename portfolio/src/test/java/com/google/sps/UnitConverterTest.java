/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.sps.data.Output;
import java.io.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UnitConverterTest {

  private static Logger log = LoggerFactory.getLogger(UnitConverterTest.class);

  @Test
  public void testUnitConversion() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "How many centimeters are in 45 miles?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"mi\",\"unit-to\": \"cm\", \"amount\": 45.0}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+45.0+mi+to+cm", output.getRedirect());
  }

  @Test
  public void testUnitConversionWithoutAmount() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Yards to meters",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"yd\",\"unit-to\": \"m\", \"amount\": 0.0}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+yd+to+m", output.getRedirect());
  }

  @Test
  public void testUnitConversionWithoutAmount2() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Change from Fahrenheit to Kelvin",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"F\",\"unit-to\": \"K\", \"amount\": 0.0}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+F+to+K", output.getRedirect());
  }

  @Test
  public void testUnitConversionWithoutAmount3() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "How many ounces in a cup?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"cup\",\"unit-to\": \"oz\", \"amount\": 0.0}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+cup+to+oz", output.getRedirect());
  }

  @Test
  public void testUnitConversionWithoutUnitTo() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "10 miles",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"mi\",\"unit-to\": \"\", \"amount\": 10.0}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+10.0+mi", output.getRedirect());
  }

  @Test
  public void testUnitConversionWithoutUnitTo2() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Is 90 degrees Fahrenheit incredibly hot?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"F\",\"unit-to\": \"\", \"amount\": 90.0}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+90.0+F", output.getRedirect());
  }

  @Test
  public void testUnitConversionWithoutUnitFrom() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Convert into miles",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"\",\"unit-to\": \"mi\", \"amount\": 0.0}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+mi", output.getRedirect());
  }
}
