package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import com.google.sps.utils.AgentUtils;
import java.util.Map;

/** Currency Agent */
public class Currency implements Agent {
  private final String intentName;
  private String userInput;
  private String currencyFrom;
  private String currencyTo;
  private Double amount;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private String searchText = "";
  private String baseURL;
  private String endURL;

  public Currency(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    System.out.println(parameters);
    currencyFrom = parameters.get("currency-from").getStringValue();
    currencyTo = parameters.get("currency-to").getStringValue();
    amount = parameters.get("amount").getNumberValue();
    userInput = AgentUtils.getUserInput();
    baseURL = "http://www.google.com/search?q=";

    // Searching for exchange rate
    if (userInput.contains("exchange")) {
      fulfillment = "Redirecting for exchange rate";
      searchText += "Exchange rate";
      if (amount > 0.0) {
        searchText += " for " + String.valueOf(amount);
      }
      if (!currencyFrom.equals("")) {
        searchText += " " + currencyFrom;
      }
      if (!currencyTo.equals("")) {
        if (!currencyFrom.equals("")) {
          searchText += " to";
        }
        searchText += " " + currencyTo;
      }
      String[] individualWords = searchText.split(" ");
      endURL = String.join("+", individualWords);

      // Searching for conversion
    } else {
      fulfillment = "Redirecting for conversion";
      searchText += "Convert";
      if (amount > 0.0) {
        searchText += " " + String.valueOf(amount);
      }
      if (!currencyFrom.equals("")) {
        searchText += " " + currencyFrom;
      }
      if (!currencyTo.equals("")) {
        if (!currencyFrom.equals("")) {
          searchText += " to";
        }
        searchText += " " + currencyTo;
      }
      String[] individualWords = searchText.split(" ");
      endURL = String.join("+", individualWords);
    }

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
