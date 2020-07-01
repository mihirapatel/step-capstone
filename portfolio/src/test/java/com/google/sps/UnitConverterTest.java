package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.sps.data.DialogFlow;
import com.google.sps.data.Output;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UnitConverterTest {

  @Mock DialogFlow dialogFlowMock;

  @InjectMocks TextInputServlet textInputServlet;

  @Test
  public void testUnitConversion() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "How many centimeters are in 45 miles?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"mi\",\"unit-to\": \"cm\", \"amount\": \"45.0\"}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+45.0+mi+to+cm");
  }

  @Test
  public void testUnitConversionWithoutAmount() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Yards to meters",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"yd\",\"unit-to\": \"m\", \"amount\": \"0.0\"}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+yd+to+m");
  }

  @Test
  public void testUnitConversionWithoutAmount2() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Change from Fahrenheit to Kelvin",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"F\",\"unit-to\": \"K\", \"amount\": \"0.0\"}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+F+to+K");
  }

  @Test
  public void testUnitConversionWithoutAmount3() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "How many ounces in a cup?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"cup\",\"unit-to\": \"oz\", \"amount\": \"0.0\"}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+cup+to+oz");
  }

  @Test
  public void testUnitConversionWithoutUnitTo() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "10 miles",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"mi\",\"unit-to\": \"\", \"amount\": \"10.0\"}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+10.0+mi");
  }

  @Test
  public void testUnitConversionWithoutUnitTo2() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Is 90 degrees Fahrenheit incredibly hot?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"F\",\"unit-to\": \"\", \"amount\": \"90.0\"}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+90.0+F");
  }

  @Test
  public void testUnitConversionWithoutUnitFrom() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "Convert into miles",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"unit-from\": \"\",\"unit-to\": \"mi\", \"amount\": \"0.0\"}",
            // Intent that you expect dialogflow to return based on your query
            "units.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+mi");
  }
}
