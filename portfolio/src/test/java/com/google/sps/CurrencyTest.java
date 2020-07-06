package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.sps.data.Output;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CurrencyTest {

  private static Logger log = LoggerFactory.getLogger(CurrencyTest.class);

  @Test
  public void testExchangeRate() throws Exception {

    TestHelper tester =
        new TestHelper(
            // User input text
            "What's the exchange rate for 10 Canadian dollars to US dollars?",
            // Parameter JSON string (copy paste from dialogflow)
            "{\"currency-from\": \"CAD\",\"currency-to\": \"USD\", \"amount\": 10.0}",
            // Intent that you expect dialogflow to return based on your query
            "currency.convert");

    Output output = tester.getOutput();

    // Assertions
    assertEquals(output.getFulfillmentText(), "Redirecting for exchange rate");
    assertEquals(
        output.getRedirect(), "http://www.google.com/search?q=Exchange+rate+for+10.0+CAD+to+USD");
  }

  @Test
  public void testExchangeRateWithoutAmount() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Exchange rate for Euros to US dollars",
            "{\"currency-from\": \"EUR\",\"currency-to\": \"USD\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for exchange rate");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Exchange+rate+EUR+to+USD");
  }

  @Test
  public void testExchangeRateWithoutCurrencyTo() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Exchange rate for 10 Mexican Pesos",
            "{\"currency-from\": \"MXN\",\"currency-to\": \"\", \"amount\": 10.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for exchange rate");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Exchange+rate+for+10.0+MXN");
  }

  @Test
  public void testExchangeRateWithoutCurrencyFromAndAmount() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Exchange rate Mexican Pesos",
            "{\"currency-from\": \"\",\"currency-to\": \"MXN\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for exchange rate");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Exchange+rate+MXN");
  }

  @Test
  public void testExchangeRateWithoutAllParameters() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Current exchange rate",
            "{\"currency-from\": \"\",\"currency-to\": \"\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for exchange rate");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Exchange+rate");
  }

  @Test
  public void testConversion() throws Exception {

    TestHelper tester =
        new TestHelper(
            "How much is 25 US dollars in Euros?",
            "{\"currency-from\": \"USD\",\"currency-to\": \"EUR\", \"amount\": 25.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+25.0+USD+to+EUR");
  }

  @Test
  public void testConversionWithoutAmount() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Convert Jamaican dollars into Yen",
            "{\"currency-from\": \"JMD\",\"currency-to\": \"JPY\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+JMD+to+JPY");
  }

  @Test
  public void testConversionWithoutCurrencyTo() throws Exception {

    TestHelper tester =
        new TestHelper(
            "How much will I get for 1000 Indian rupees?",
            "{\"currency-from\": \"INR\",\"currency-to\": \"\", \"amount\": 1000.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+1000.0+INR");
  }

  @Test
  public void testConversionWithoutAllParameters() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Can you convert currencies?",
            "{\"currency-from\": \"\",\"currency-to\": \"\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals(output.getFulfillmentText(), "Redirecting for conversion");
    assertEquals(output.getRedirect(), "http://www.google.com/search?q=Convert+currency");
  }
}
