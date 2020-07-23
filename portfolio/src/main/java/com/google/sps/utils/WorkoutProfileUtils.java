package com.google.sps.utils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.common.collect.Iterables;
import com.google.sps.data.WorkoutPlan;
import java.util.ArrayList;
import org.apache.commons.lang3.SerializationUtils;

public class WorkoutProfileUtils {

  /**
   * Stores all generated workout plans if user is logged in
   *
   * @param userID The current logged-in user's ID number
   * @param datastore Datastore instance to store WorkoutPlan in database
   * @param workoutPlan WorkoutPlan object created by user to store in database
   */
  public static void storeWorkoutPlan(
      String userId, DatastoreService datastore, WorkoutPlan workoutPlan) {
    long timestamp = System.currentTimeMillis();
    Entity workoutPlanEntity = new Entity("WorkoutPlan");

    byte[] workoutPlanData = SerializationUtils.serialize(workoutPlan);
    Blob workoutPlanBlob = new Blob(workoutPlanData);

    workoutPlanEntity.setProperty("userId", userId);
    workoutPlanEntity.setProperty("workoutPlan", workoutPlanBlob);
    workoutPlanEntity.setProperty("workoutPlanId", workoutPlan.getWorkoutPlanId());
    workoutPlanEntity.setProperty("timestamp", timestamp);
    datastore.put(workoutPlanEntity);
  }

  /**
   * Returns number of WorkoutPlans created and saved in Datastore by current user (specified by
   * userId)
   *
   * @param userId The current logged-in user's ID number
   * @param datastore Datastore instance to retrieve WorkoutPlan information from database
   * @return number of workout plans created by userId + 1 to create unique id for new workout plan
   */
  public static int getWorkoutPlanId(String userId, DatastoreService datastore) {
    Filter userFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query = new Query("WorkoutPlan").setFilter(userFilter);
    PreparedQuery results = datastore.prepare(query);
    return Iterables.size(results.asIterable()) + 1;
  }

  /**
   * Retrieves WorkoutPlans saved by current user
   *
   * @param userID The current logged-in user's ID number
   * @param datastore Datastore instance to retrieve WorkoutPlan from database
   * @return ArrayList of WorkoutPlan objects that user saved
   */
  public static ArrayList<WorkoutPlan> getWorkoutPlansList(
      String userId, DatastoreService datastore) {

    Filter userFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query =
        new Query("WorkoutPlan")
            .setFilter(userFilter)
            .addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    ArrayList<WorkoutPlan> workoutPlans = new ArrayList<>();
    for (Entity entity : results.asIterable()) {

      long timestamp = (long) entity.getProperty("timestamp");
      Blob workoutPlanBlob = (Blob) entity.getProperty("workoutPlan");
      WorkoutPlan workoutPlan = SerializationUtils.deserialize(workoutPlanBlob.getBytes());
      workoutPlans.add(workoutPlan);
    }

    return workoutPlans;
  }
}
