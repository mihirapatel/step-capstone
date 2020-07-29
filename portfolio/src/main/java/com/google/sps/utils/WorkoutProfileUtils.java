package com.google.sps.utils;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.collect.Iterables;
import com.google.sps.data.WorkoutPlan;
import java.util.Arrays;
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
   * Retrieves stored WorkoutPlan that user wants to save
   *
   * @param userID The current logged-in user's ID number
   * @param workoutPlanId The id for the workout they want to save
   * @param datastore Datastore instance to retrieve WorkoutPlan from database
   * @return ArrayList of WorkoutPlan objects that user saved
   */
  public static WorkoutPlan getStoredWorkoutPlan(
      String userId, int workoutPlanId, DatastoreService datastore) {

    Filter storedWorkoutPlanFilter = createStoredWorkoutPlanFilter(userId, workoutPlanId);
    Query query = new Query("WorkoutPlan").setFilter(storedWorkoutPlanFilter);
    PreparedQuery results = datastore.prepare(query);

    Entity workoutPlanEntity = results.asSingleEntity();

    long timestamp = (long) workoutPlanEntity.getProperty("timestamp");
    Blob workoutPlanBlob = (Blob) workoutPlanEntity.getProperty("workoutPlan");
    WorkoutPlan workoutPlan = SerializationUtils.deserialize(workoutPlanBlob.getBytes());

    return workoutPlan;
  }

  /**
   * Saved WorkoutPlan when requested by user
   *
   * @param userID The current logged-in user's ID number
   * @param datastore Datastore instance to save WorkoutPlan in database
   * @param workoutPlan WorkoutPlan object to save in database
   */
  public static void saveWorkoutPlan(
      String userId, DatastoreService datastore, WorkoutPlan workoutPlan) {
    long timestamp = System.currentTimeMillis();
    Entity savedWorkoutPlanEntity = new Entity("SavedWorkoutPlan");

    byte[] workoutPlanData = SerializationUtils.serialize(workoutPlan);
    Blob workoutPlanBlob = new Blob(workoutPlanData);

    savedWorkoutPlanEntity.setProperty("userId", userId);
    savedWorkoutPlanEntity.setProperty("workoutPlan", workoutPlanBlob);
    savedWorkoutPlanEntity.setProperty("workoutPlanId", workoutPlan.getWorkoutPlanId());
    savedWorkoutPlanEntity.setProperty("timestamp", timestamp);
    datastore.put(savedWorkoutPlanEntity);
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
   * Retuns a composite filter for Query to retrieve WorkoutPlan Entity corresponding to userId and
   * workoutPlanId
   *
   * @param userId unique id of session to delete entities from
   * @param workoutPlanId unique id (within session) to delete entities from
   * @return CompositeFilter with proper filters
   */
  public static Filter createStoredWorkoutPlanFilter(String userId, int workoutPlanId) {
    return new CompositeFilter(
        CompositeFilterOperator.AND,
        Arrays.asList(
            new FilterPredicate("userId", FilterOperator.EQUAL, userId),
            new FilterPredicate("workoutPlanId", FilterOperator.EQUAL, workoutPlanId)));
  }
}
