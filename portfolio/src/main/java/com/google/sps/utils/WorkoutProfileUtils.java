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
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.lang3.SerializationUtils;

public class WorkoutProfileUtils {

  /**
   * Stores all generated workout plans if user is logged in
   *
   * @param datastore Datastore instance to store WorkoutPlan in database
   * @param workoutPlan WorkoutPlan object created by current user to store in database
   */
  public static void storeWorkoutPlan(DatastoreService datastore, WorkoutPlan workoutPlan) {

    // Transform WorkoutPlan to Blob to be able to store with Datastore
    byte[] workoutPlanData = SerializationUtils.serialize(workoutPlan);
    Blob workoutPlanBlob = new Blob(workoutPlanData);

    // Create WorkoutPlan Entity and store in Datastore
    Entity workoutPlanEntity = new Entity("WorkoutPlan");
    workoutPlanEntity.setProperty("userId", workoutPlan.getUserId());
    workoutPlanEntity.setProperty("workoutPlan", workoutPlanBlob);
    workoutPlanEntity.setProperty("workoutPlanId", workoutPlan.getWorkoutPlanId());
    datastore.put(workoutPlanEntity);
  }

  /**
   * Retrieves stored WorkoutPlan that user wants to save
   *
   * @param userID The current logged-in user's ID number
   * @param workoutPlanId The id for the workout they want to save
   * @param datastore Datastore instance to retrieve WorkoutPlan from database
   * @return WorkoutPlan that user wants to save
   */
  public static WorkoutPlan getStoredWorkoutPlan(
      String userId, int workoutPlanId, DatastoreService datastore) {

    // Create Filter to retrieve WorkoutPlan entities based on userId and workoutPlanId
    Filter storedWorkoutPlanFilter = createStoredWorkoutPlanFilter(userId, workoutPlanId);
    Query query = new Query("WorkoutPlan").setFilter(storedWorkoutPlanFilter);
    PreparedQuery results = datastore.prepare(query);

    Entity workoutPlanEntity = results.asSingleEntity();

    // Transform WorkoutPlan Blob back into WorkoutPlan and return it
    Blob workoutPlanBlob = (Blob) workoutPlanEntity.getProperty("workoutPlan");
    WorkoutPlan workoutPlan = SerializationUtils.deserialize(workoutPlanBlob.getBytes());

    return workoutPlan;
  }

  /**
   * Saves WorkoutPlan when requested by user
   *
   * @param datastore Datastore instance to save WorkoutPlan in database
   * @param workoutPlan WorkoutPlan object current user wants to save in database
   */
  public static void saveWorkoutPlan(DatastoreService datastore, WorkoutPlan workoutPlan) {

    // Transform WorkoutPlan to Blob to be able to store with Datastore
    byte[] workoutPlanData = SerializationUtils.serialize(workoutPlan);
    Blob workoutPlanBlob = new Blob(workoutPlanData);

    // Create SavedWorkoutPlan Entity and store in Datastore
    Entity savedWorkoutPlanEntity = new Entity("SavedWorkoutPlan");
    savedWorkoutPlanEntity.setProperty("userId", workoutPlan.getUserId());
    savedWorkoutPlanEntity.setProperty("workoutPlan", workoutPlanBlob);
    savedWorkoutPlanEntity.setProperty("workoutPlanId", workoutPlan.getWorkoutPlanId());
    datastore.put(savedWorkoutPlanEntity);
  }

  /**
   * Retrieves saved WorkoutPlans to display on user's workout dashboard
   *
   * @param userID The current logged-in user's ID number
   * @param datastore Datastore instance to retrieve WorkoutPlans from database
   * @return ArrayList of WorkoutPlan objects that has user saved
   */
  public static ArrayList<WorkoutPlan> getSavedWorkoutPlans(
      String userId, DatastoreService datastore) {

    // Create Filter to retrieve WorkoutPlan entities based on userId
    Filter savedWorkoutPlanFilter = new FilterPredicate("userId", FilterOperator.EQUAL, userId);
    Query query = new Query("SavedWorkoutPlan").setFilter(savedWorkoutPlanFilter);
    PreparedQuery results = datastore.prepare(query);

    ArrayList<WorkoutPlan> savedWorkoutPlans = new ArrayList<>();

    // For each saved WorkoutPLan, transform WorkoutPlan Blob back into WorkoutPlan and add to
    // ArrayList
    for (Entity savedWorkoutPlanEntity : results.asIterable()) {
      Blob workoutPlanBlob = (Blob) savedWorkoutPlanEntity.getProperty("workoutPlan");
      WorkoutPlan workoutPlan = SerializationUtils.deserialize(workoutPlanBlob.getBytes());
      savedWorkoutPlans.add(workoutPlan);
    }

    return savedWorkoutPlans;
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
