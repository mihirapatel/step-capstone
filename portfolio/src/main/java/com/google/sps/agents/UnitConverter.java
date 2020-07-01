package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import java.util.Map;

/** Unit Converter Agent */
public class UnitConverter implements Agent {
  private final String intentName;
  private String unitFrom;
  private String unitTo;
  private Double amount;
  private String fulfillment;
  private String redirect;

  public UnitConverter(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    unitFrom = parameters.get("unit-from").getStringValue();
    unitTo = parameters.get("unit-to").getStringValue();
    amount = parameters.get("amount").getNumberValue();

    fulfillment = "Redirecting for conversion";
    String baseURL = "http://www.google.com/search?q=";
    String endURL = String.join("+", "Convert", String.valueOf(amount), unitFrom, "to", unitTo);
    redirect = baseURL + endURL;
  }

  @Override
  public String getOutput() {
    return "Redirecting for conversion";
  }

  @Override
  public String getDisplay() {
    return null;
  }

  @Override
  public String getRedirect() {
    return redirect;
  }
}
