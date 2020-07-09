package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import com.google.sps.utils.AgentUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Currency Agent */
public class Currency implements Agent {

  private static Logger log = LoggerFactory.getLogger(Currency.class);

  private final String intentName;
  private String userInput;
  private String currencyFrom;
  private String currencyTo;
  private Double amount;
  private String fulfillment = null;
  private String display = null;
  private String redirect = null;
  private String searchText = "";
  private String searchParameters = "";
  private String baseURL;
  private String endURL;

  public Currency(String intentName, Map<String, Value> parameters) {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters) {
    currencyFrom = parameters.get("currency-from").getStringValue();
    currencyTo = parameters.get("currency-to").getStringValue();
    amount = parameters.get("amount").getNumberValue();
    userInput = AgentUtils.getUserInput().toLowerCase();
    baseURL = "http://www.google.com/search?q=";

    // Searching for exchange rate
    if (userInput.contains("exchange")) {
      fulfillment = "Redirecting for exchange rate";
      searchText += "Exchange rate";

      if (amount > 0.0) {
        searchParameters += " for " + String.valueOf(amount);
      }
      if (!currencyFrom.equals("")) {
        searchParameters += " " + currencyFrom;
      }
      if (!currencyTo.equals("")) {
        if (!currencyFrom.equals("")) {
          searchParameters += " to";
        }
        searchParameters += " " + currencyTo;
      }

      searchText += searchParameters;
      String[] individualWords = searchText.split(" ");
      endURL = String.join("+", individualWords);

      // Searching for conversion
    } else {
      fulfillment = "Redirecting for conversion";
      searchText += "Convert";

      if (amount > 0.0) {
        searchParameters += " " + String.valueOf(amount);
      }
      if (!currencyFrom.equals("")) {
        searchParameters += " " + currencyFrom;
      }
      if (!currencyTo.equals("")) {
        if (!currencyFrom.equals("")) {
          searchParameters += " to";
        }
        searchParameters += " " + currencyTo;
      }
      if (searchParameters.equals("")) {
        searchParameters += " currency";
      }

      searchText += searchParameters;
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
