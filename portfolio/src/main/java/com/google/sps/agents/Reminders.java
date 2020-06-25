package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.sps.data.Output;
import com.google.sps.agents.Agent;
import com.google.sps.utils.TimeUtils;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Reminders Agent
 */
public class Reminders implements Agent {
    private String intentName = null;
    private String recurrence = null;
    private Date date = null;
    private String name = "";
    private String fulfillment = "";
    private String display = null;
    
    public Reminders(String intentName, Map<String, Value> parameters) {
        this.intentName = intentName;
        try {
            setParameters(parameters);
        } catch (Exception e) {
            fulfillment = null;
        }
    }

    @Override 
    public void setParameters(Map<String, Value> parameters) {
        if (intentName.equals("snooze")) {
			Struct durationStruct = parameters.get("date-time").getStructValue();
			Map<String, Value> durationMap = durationStruct.getFieldsMap();
			Date start = TimeUtils.stringToDate(durationMap.get("startDateTime").getStringValue());
			Date end = TimeUtils.stringToDate(durationMap.get("endDateTime").getStringValue());
			int diffSec = (int)((end.getTime() - start.getTime()) / 1000);
			String timeDisplay = TimeUtils.timeToString(diffSec);
			if (timeDisplay == null) {
				fulfillment = "Sorry, unable to set a timer for less than 1 second or more than 1 day. Please try adding a reminder instead.";
			} else {
				fulfillment = "Starting a timer for " + timeDisplay + " now.";
				display = TimeUtils.makeClockDisplay(diffSec);
			}
		 } else {
			fulfillment = "Sorry, the reminders feature is not supported yet.";
			//TODO: if we ever get access to storing user info or calendars...
		 }
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
		return null;
    }
}
