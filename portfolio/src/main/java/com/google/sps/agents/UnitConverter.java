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
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private String searchText = "";
  private String baseURL;
  private String endURL;

  public UnitConverter(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  public void setParameters(Map<String, Value> parameters) {
    unitFrom = parameters.get("unit-from").getStringValue();
    unitTo = parameters.get("unit-to").getStringValue();
    amount = parameters.get("amount").getNumberValue();
    fulfillment = "Redirecting for conversion";
    baseURL = "http://www.google.com/search?q=";

    searchText += "Convert";
    if (amount > 0.0) {
      searchText += " " + String.valueOf(amount);
    }
    if (!unitFrom.equals("")) {
      searchText += " " + unitFrom;
    }
    if (!unitTo.equals("")) {
      if (!unitFrom.equals("")) {
        searchText += " to";
      }
      searchText += " " + unitTo;
    }
    String[] individualWords = searchText.split(" ");
    endURL = String.join("+", individualWords);
    redirect = baseURL + endURL;

    System.out.println("unitFrom: " + unitFrom);
    System.out.println("unitTo: " + unitTo);
    System.out.println("amount: " + amount);
    System.out.println("fulfillment: " + fulfillment);
    System.out.println("redirect: " + redirect);
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
