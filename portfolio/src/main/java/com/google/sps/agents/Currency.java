package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import java.util.Map;

/** Currency Agent */
public class Currency implements Agent {
  private final String intentName;
  private String currencyFrom;
  private String currencyTo;
  private Double amount;
  private String fulfillment = null;;
  private String display = null;
  private String redirect = null;

  public Currency(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    currencyFrom = parameters.get("currency-from").getStringValue();
    currencyTo = parameters.get("currency-to").getStringValue();
    amount = parameters.get("amount").getNumberValue();

    fulfillment = "Redirecting for conversion";
    String baseURL = "http://www.google.com/search?q=";
    String endURL =
        String.join("+", "Convert", String.valueOf(amount), currencyFrom, "to", currencyTo);
    redirect = baseURL + endURL;
  }

  @Override
  public String getOutput() {
    return fulfillment;
  }

  @Override
  public String getDisplay() {
    return display;
  }

  @Override
  public String getRedirect() {
    return redirect;
  }
}
