package com.lesgens.veganornot.tasks;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.lesgens.veganornot.listeners.VeganOrNotCheckListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VeganOrNotCheckUtils {
    private static final String TAG = "VeganOrNotCheckUtils";

    public static void isVeganOrNot(final VeganOrNotCheckListener listener, String listOfIngredients) {
        final List<String> nonVegans = new ArrayList<>();
        final List<String> canBeVegan = new ArrayList<>();

        listOfIngredients = listOfIngredients.toLowerCase().trim();
        listOfIngredients = listOfIngredients.replaceAll("[\\s,.\\n\\t]+", ",");
        Log.d(TAG, "isVeganOrNot: ingredients=" + listOfIngredients);

        FirebaseFunctions.getInstance().getHttpsCallable("isVeganOrNot")
                .call(listOfIngredients)
                .continueWith(new Continuation<HttpsCallableResult, HashMap>() {
                    @Override
                    public HashMap then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        HashMap result = (HashMap) task.getResult().getData();
                        return result;
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<HashMap>() {
                    @Override
                    public void onComplete(@NonNull Task<HashMap> task) {
                        try {
                            HashMap result = task.getResult();
                            Log.d(TAG, "onComplete: isSuccess=" + task.isSuccessful() + ", result=" + result);
                            if (listener != null) {
                                if (task.isSuccessful()) {
                                    if (result != null) {
                                        JSONObject jsonObject = new JSONObject(result);

                                        if (jsonObject.has("non-vegan")) {
                                            JSONArray nonVeganArray = jsonObject.getJSONArray("non-vegan");
                                            for (int i = 0; i < nonVeganArray.length(); i++) {
                                                nonVegans.add(nonVeganArray.getString(i));
                                            }
                                        }

                                        if (jsonObject.has("can-be-vegan")) {
                                            JSONArray nonVeganArray = jsonObject.getJSONArray("can-be-vegan");
                                            for (int i = 0; i < nonVeganArray.length(); i++) {
                                                canBeVegan.add(nonVeganArray.getString(i));
                                            }
                                        }


                                        listener.onCheckDone(nonVegans, canBeVegan);
                                    } else {
                                        listener.onCheckFailed();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
