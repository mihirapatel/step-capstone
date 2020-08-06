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
    assertEquals("Redirecting for exchange rate", output.getFulfillmentText());
    assertEquals(
        "http://www.google.com/search?q=Exchange+rate+for+10.0+CAD+to+USD", output.getRedirect());
  }

  @Test
  public void testExchangeRateWithoutAmount() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Exchange rate for Euros to US dollars",
            "{\"currency-from\": \"EUR\",\"currency-to\": \"USD\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals("Redirecting for exchange rate", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Exchange+rate+EUR+to+USD", output.getRedirect());
  }

  @Test
  public void testExchangeRateWithoutCurrencyTo() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Exchange rate for 10 Mexican Pesos",
            "{\"currency-from\": \"MXN\",\"currency-to\": \"\", \"amount\": 10.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals("Redirecting for exchange rate", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Exchange+rate+for+10.0+MXN", output.getRedirect());
  }

  @Test
  public void testExchangeRateWithoutCurrencyFromAndAmount() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Exchange rate Mexican Pesos",
            "{\"currency-from\": \"\",\"currency-to\": \"MXN\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals("Redirecting for exchange rate", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Exchange+rate+MXN", output.getRedirect());
  }

  @Test
  public void testExchangeRateWithoutAllParameters() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Current exchange rate",
            "{\"currency-from\": \"\",\"currency-to\": \"\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals("Redirecting for exchange rate", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Exchange+rate", output.getRedirect());
  }

  @Test
  public void testConversion() throws Exception {

    TestHelper tester =
        new TestHelper(
            "How much is 25 US dollars in Euros?",
            "{\"currency-from\": \"USD\",\"currency-to\": \"EUR\", \"amount\": 25.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals("Redirecting for conversion", output.getFulfillmentText());
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

    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+JMD+to+JPY", output.getRedirect());
  }

  @Test
  public void testConversionWithoutCurrencyTo() throws Exception {

    TestHelper tester =
        new TestHelper(
            "How much will I get for 1000 Indian rupees?",
            "{\"currency-from\": \"INR\",\"currency-to\": \"\", \"amount\": 1000.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+1000.0+INR", output.getRedirect());
  }

  @Test
  public void testConversionWithoutAllParameters() throws Exception {

    TestHelper tester =
        new TestHelper(
            "Can you convert currencies?",
            "{\"currency-from\": \"\",\"currency-to\": \"\", \"amount\": 0.0}",
            "currency.convert");

    Output output = tester.getOutput();

    assertEquals("Redirecting for conversion", output.getFulfillmentText());
    assertEquals("http://www.google.com/search?q=Convert+currency", output.getRedirect());
  }
}
