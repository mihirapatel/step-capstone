package com.google.sps.servlets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.gson.Gson;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import com.google.sps.data.DialogFlow;
import com.google.sps.data.Output;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RemindersTest {

  @Mock DialogFlow dialogFlowMock;

  @InjectMocks TextInputServlet textInputServlet;

  @Before
  public void setupTests() {
    dialogFlowMock = mock(DialogFlow.class);
  }

  @Test
  public void testReminderSnooze() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("request-input")).thenReturn("Set a timer for 10 sec");
    String jsonString =
        "{\"date-time\": {\"endDateTime\": \"2020-06-29T16:53:50-07:00\",\"startDateTime\": \"2020-06-29T16:53:40-07:00\"}}";
    Map<String, Value> map = stringToMap(jsonString);
    when(dialogFlowMock.getParameters()).thenReturn(map);
    when(dialogFlowMock.getQueryText()).thenReturn("Set a timer for 10 sec");
    when(dialogFlowMock.getIntentName()).thenReturn("reminders.snooze");
    when(dialogFlowMock.getIntentConfidence()).thenReturn((float) 1.0);
    when(dialogFlowMock.getFulfillmentText()).thenReturn("");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    (new TestableTextInputServlet()).doPost(request, response);

    verify(request, atLeast(1)).getParameter("request-input");
    writer.flush();

    System.out.println("What is this?");
    System.out.println(stringWriter.toString());

    Output output = new Gson().fromJson(stringWriter.toString(), Output.class);
    System.out.println(output);
    System.out.println(output.getFulfillmentText());

    assertTrue(output.getFulfillmentText().equals("Starting a timer for 10 seconds now."));
  }

  private class TestableTextInputServlet extends TextInputServlet {
    @Override
    public DialogFlow createDialogFlow(
        String text, String languageCode, SessionsClient sessionsClient) {
      return dialogFlowMock;
    }
  }

  private Map<String, Value> stringToMap(String json) {
    try {
      JSONObject jsonObject = new JSONObject(json);
      Builder structBuilder = Struct.newBuilder();
      JsonFormat.parser().merge(jsonObject.toString(), structBuilder);
      Struct struct = structBuilder.build();
      return struct.getFieldsMap();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
