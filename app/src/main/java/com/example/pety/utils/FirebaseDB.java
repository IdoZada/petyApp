package com.example.pety.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.pety.interfaces.SendDataCallback;
import com.example.pety.objects.Beauty;
import com.example.pety.objects.Family;
import com.example.pety.enums.FamilyFlag;
import com.example.pety.objects.Feed;
import com.example.pety.objects.Health;
import com.example.pety.objects.Pet;
import com.example.pety.objects.User;

import com.example.pety.objects.Walk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FirebaseDB {
    private static final String TAG = "RealTimeDatabase";
    public static final String USERS = "users";
    public static final String FIRST_NAME = "f_name";
    public static final String LAST_NAME = "l_name";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String IMAGE_URL = "image_url";
    public static final String FAMILIES = "families";
    public static final String PETS = "pets";
    public static final String WALKS = "walks";
    public static final String FEEDS = "feeds";
    public static final String BEAUTY = "beauty";
    public static final String HEALTH = "health";
    public static final String TIME = "time";
    public static final String TIME_DATE = "time_date";

    private FirebaseAuth authDatabase;
    private FirebaseDatabase database;
    private static FirebaseDB firebaseDB;
    private FirebaseStorage storage;
    Family family;
    User user;

    private FirebaseDB() {
        database = FirebaseDatabase.getInstance();
        authDatabase = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static FirebaseDB getInstance() {
        if (firebaseDB == null) {
            firebaseDB = new FirebaseDB();
        }
        return firebaseDB;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public FirebaseAuth getFirebaseAuth() {
        return authDatabase;
    }

    public FirebaseStorage getFirebaseStorage() {
        return storage;
    }

    public void updateFirstNameAndLastNameAndImage(User user, String imageName, Uri contentUri) {
        String uid = getFirebaseAuth().getCurrentUser().getUid();
        DatabaseReference userRef = database.getReference(USERS);
        userRef.child(uid).child(FIRST_NAME).setValue(user.getF_name());
        userRef.child(uid).child(LAST_NAME).setValue(user.getL_name());
        userRef.child(uid).child(PHONE_NUMBER).setValue(getFirebaseAuth().getCurrentUser().getPhoneNumber());

        final StorageReference image = getFirebaseStorage().getReference().child("pictures/" + imageName);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(uri -> {
                    userRef.child(uid).child(IMAGE_URL).setValue(uri.toString());
                    user.setImage_url(uri.toString());
                });
                Log.d("TAG", "onSuccess: Upload user picture successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: Upload user picture failed");
            }
        });
    }

    public void getImageUser(View view, Context context){
        DatabaseReference myRef = getDatabase().getReference().child(USERS + "/" + authDatabase.getCurrentUser().getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(IMAGE_URL)){
                    String imageUrl = snapshot.child(IMAGE_URL).getValue().toString();
                    Glide.with(context).load(imageUrl).into((ImageView) view);
                    user.setImage_url(imageUrl);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void retrieveUserDataFromDB(SendDataCallback sendDataCallback) {
        user = new User();
        String uid = getFirebaseAuth().getCurrentUser().getUid();
        DatabaseReference myRef = getDatabase().getReference(USERS).child(uid);
        myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d("TAG", "onComplete - retrieveUserDataFromDB: Error getting user data");
                } else {
                    Map<String, String> families_map;
                    String firstName = task.getResult().child(FIRST_NAME).getValue().toString();
                    String lastName = task.getResult().child(LAST_NAME).getValue().toString();
                    String phoneNumber = task.getResult().child(PHONE_NUMBER).getValue().toString();
                    if (task.getResult().hasChild(FAMILIES)) {
                        families_map = (Map<String, String>) task.getResult().child(FAMILIES).getValue();
                        user.setFamilies_map(families_map);
                    }
                    if (task.getResult().hasChild("homeFamily_id")) {
                        String homeFamily_id = task.getResult().child("homeFamily_id").getValue().toString();
                        user.setHomeFamily_id(homeFamily_id);
                    }
                    user.setF_name(firstName);
                    user.setL_name(lastName);
                    user.setPhone_number(phoneNumber);
                    sendDataCallback.sendUser(user);
                    Log.d("TAG", "onComplete - retrieveUserDataFromDB: task success ->" + user.toString());
                }
            }
        });
    }

    public void updateDefaultFamily(String familyHome_id){
        String uid = getFirebaseAuth().getCurrentUser().getUid();
        getDatabase().getReference().child(USERS).child(uid).child("homeFamily_id").setValue(familyHome_id);

    }

    public void retrieveAllFamilies(Map<String, String> families_map, ArrayList<Family> families, SendDataCallback sendDataCallback) {
        DatabaseReference myFamilies = getDatabase().getReference().child(FAMILIES);
        Log.d(TAG, "retrieveAllFamilies: " + families_map.size());
        int numberOfFamilies = families_map.size();
        for (String key : families_map.keySet()) {
            myFamilies.child(key).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "onComplete - retrieveAllFamilies: Error getting families data");
                    } else {
                        Map<String, Pet> pets;
                        family = new Family();
                        String family_key = task.getResult().child("family_key").getValue().toString();
                        String familyName = task.getResult().child("f_name").getValue().toString();
                        String familyImagePath = task.getResult().child("imageURL").getValue().toString();
                        if (task.getResult().hasChild("pets")) {
                            pets = (Map<String, Pet>) task.getResult().child("pets").getValue();
                            Map<String, Pet> pets_map = Converter.convertPets(pets);
                            family.setPets(pets_map);
                        }
                        family.setFamily_key(family_key);
                        family.setF_name(familyName);
                        family.setImageUrl(familyImagePath);
                        families.add(family);
                        if(families.size() == numberOfFamilies){
                            sendDataCallback.sendFamilies(families);
                        }
                        if(family.getFamily_key().equals(user.getHomeFamily_id())){
                            sendDataCallback.sendFamily(family, FamilyFlag.SEND_TO_PET_HOME_FRAGMENT);
                        }
                        Log.d(TAG, "onComplete - retrieveAllFamilies: families success " + families);
                    }
                }
            });
        }

    }

    /**
     * This function store in the database new family
     *
     * @param family
     */
    public void writeNewFamilyToDB(Family family, Map<String, String> families_map, String imageName, Uri contentUri) {
        String key = getDatabase().getReference().child(FAMILIES).push().getKey();
        final StorageReference image = getFirebaseStorage().getReference().child("pictures/" + imageName);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(uri -> {
                    family.setImageUrl(uri.toString());
                    family.setFamily_key(key);
                    Map<String, Object> familyValues = family.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/" + FAMILIES + "/" + key, familyValues);
                    getDatabase().getReference().updateChildren(childUpdates);

                    String uid = getFirebaseAuth().getCurrentUser().getUid();
                    families_map.put(key, key);
                    //families.add(key);
                    childUpdates.put("/" + USERS + "/" + uid + "/" + FAMILIES + "/", families_map);
                    getDatabase().getReference().updateChildren(childUpdates);
                });
                Log.d("TAG", "onSuccess: Upload family successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: Upload family failed");
            }
        });
    }

    public void writeNewPetToDB(Pet pet, Family family, String imageName, Uri contentUri) {
        String key = getDatabase().getReference().child(FAMILIES).child(family.getFamily_key()).child(PETS).push().getKey();
        family.getPets().put(key, pet);
        final StorageReference image = getFirebaseStorage().getReference().child("pictures/" + imageName);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(uri -> {
                    pet.setImage_url(uri.toString());
                    pet.setPet_id(key);
                    Map<String, Object> petValues = pet.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/" + FAMILIES + "/" + family.getFamily_key() + "/" + PETS + "/" + key, petValues);
                    getDatabase().getReference().updateChildren(childUpdates);
                });
                Log.d("TAG", "onSuccess: Upload family successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: Upload family failed");
            }
        });
    }

    public void writeNewTimeToDB(Pet pet, Family family, Object obj , String option,int maxProgressBar) {
        String key = getDatabase().getReference().child(FAMILIES).child(family.getFamily_key()).child(PETS).child(option).push().getKey();
        Map<String, Object> values = null;
        if(obj instanceof Walk){
            pet.getWalks().put(key, (Walk) obj);
            ((Walk) obj).setId(key);
            values = ((Walk) obj).toMap();
            getDatabase().getReference().child(FAMILIES).child(family.getFamily_key()).child(PETS).child(pet.getPet_id()).child("maxProgressBarWalking").setValue(maxProgressBar);
        }else if(obj instanceof Feed){
            pet.getFeeds().put(key, (Feed) obj);
            ((Feed) obj).setId(key);
            values = ((Feed) obj).toMap();
            getDatabase().getReference().child(FAMILIES).child(family.getFamily_key()).child(PETS).child(pet.getPet_id()).child("maxProgressBarFeeding").setValue(maxProgressBar);
        }else if(obj instanceof Beauty){
            pet.getBeauty().put(key, (Beauty) obj);
            ((Beauty) obj).setId(key);
            values = ((Beauty) obj).toMap();
            getDatabase().getReference().child(FAMILIES).child(family.getFamily_key()).child(PETS).child(pet.getPet_id()).child("maxProgressBarBeauty").setValue(maxProgressBar);
        }else{
            pet.getHealth().put(key, (Health) obj);
            ((Health) obj).setId(key);
            values = ((Health) obj).toMap();
            getDatabase().getReference().child(FAMILIES).child(family.getFamily_key()).child(PETS).child(pet.getPet_id()).child("maxProgressBarHealth").setValue(maxProgressBar);
        }
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + FAMILIES + "/" + family.getFamily_key() + "/" + PETS + "/" + pet.getPet_id() + "/" + option + "/" + key, values);
        getDatabase().getReference().updateChildren(childUpdates);
    }

    public void deleteFamilyFromDB(Family family, String family_key) {
        //Remove family from families
        DatabaseReference myFamilies = getDatabase().getReference().child(FAMILIES).child(family_key);
        myFamilies.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().removeValue();
                Log.d("TAG", "Deleted " + snapshot.child("family_key").toString() + " family from " + snapshot.child("f_name").toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //Remove family key from user
        String uid = getFirebaseAuth().getCurrentUser().getUid();
        DatabaseReference myRef = getDatabase().getReference(USERS).child(uid).child(FAMILIES);
        myRef.child(family_key).removeValue();

        //Remove family image from firebase storage
        StorageReference myPic = getFirebaseStorage().getReferenceFromUrl(family.getImageUrl());
        myPic.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "onSuccess: delete family pic from storage database");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: unable to delete family pic from storage database");
            }
        });

        //Remove all pictures of pets from storage firebase
        Log.d("TAG", "pet images " + family.getPets().values());
        for (Map.Entry<String, Pet> entry : family.getPets().entrySet()) {
            StorageReference myPicPet = getFirebaseStorage().getReferenceFromUrl(entry.getValue().getImage_url());
            myPicPet.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("TAG", "onSuccess: delete pet pic from storage database");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG", "onFailure: unable to delete pet pic from storage database");
                }
            });
        }
    }

    public void updateTimeToDB(String family_key,String pet_key,Object obj){
        String key;
        String timeStamp = null;
        String option = null;
        String nav_child = null;
        if(obj instanceof Walk){
            key = ((Walk) obj).getId();
            timeStamp = ((Walk) obj).getTime();
            option = firebaseDB.WALKS;
            nav_child = TIME;
        }else if(obj instanceof Feed){
            key = ((Feed) obj).getId();
            timeStamp = ((Feed) obj).getTime();
            option = firebaseDB.FEEDS;
            nav_child = TIME;
        }else if(obj instanceof Beauty){
            key = ((Beauty) obj).getId();
            timeStamp = ((Beauty) obj).getTimeDate();
            option = firebaseDB.BEAUTY;
            nav_child = TIME_DATE;
        }else{
            key = ((Health) obj).getId();
            timeStamp = ((Health) obj).getTimeDate();
            option = firebaseDB.HEALTH;
            nav_child = TIME_DATE;
        }
        getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child(option).child(key).child(nav_child).setValue(timeStamp);
    }

    public void deleteTimeFromDB(String family_key,String pet_key,Object obj,int maxProgressBar,int fillProgressBar) {
        String key;
        String option = null;
        if(obj instanceof Walk){
            key = ((Walk) obj).getId();
            option = firebaseDB.WALKS;
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarWalking").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarWalking").setValue(maxProgressBar);
            Log.d("TAG", "deleteTimeFromDB: (WALK) " + key);
        }else if(obj instanceof Feed){
            key = ((Feed) obj).getId();
            option = firebaseDB.FEEDS;
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarFeeding").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarFeeding").setValue(maxProgressBar);
            Log.d("TAG", "deleteTimeFromDB: (FEEDS) " + key);
        }else if(obj instanceof Beauty){
            key = ((Beauty) obj).getId();
            option = firebaseDB.BEAUTY;
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarBeauty").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarBeauty").setValue(maxProgressBar);
        }else{
            key = ((Health) obj).getId();
            option = firebaseDB.HEALTH;
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarHealth").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarHealth").setValue(maxProgressBar);
        }

        //Remove option from specific pet
        DatabaseReference myOption = getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child(option).child(key);
        String finalOption = option;
        myOption.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().removeValue();
                Log.d("TAG", "Deleted " + snapshot.child(finalOption).toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void deletePetFromDB(Pet pet,String family_key) {
        Log.d("TAG", "deletePetFromDB: " + pet.getPet_id());

        //Remove pet from families
        DatabaseReference myPet = getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet.getPet_id());
        myPet.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().removeValue();
                Log.d("TAG", "Deleted " + snapshot.child("pet_key").toString() + " pet from " + snapshot.child("f_name").toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //Remove pet image from firebase storage
        StorageReference myPicPet = getFirebaseStorage().getReferenceFromUrl(pet.getImage_url());
        myPicPet.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("TAG", "onSuccess: delete pet pic from storage database");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure: unable to delete pet pic from storage database");
            }
        });
    }


    public void updateSwitchToDB(String family_key,String pet_key,Object obj,int fillProgressBar , int maxProgressBar) {
        String key;
        boolean isActive = false;
        String option = null;
        if (obj instanceof Walk) {
            key = ((Walk) obj).getId();
            isActive = ((Walk) obj).isActive();
            option = firebaseDB.WALKS;
            Log.d("TAG", "updateTimeToDB: (WALK) 3" + isActive);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarWalking").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarWalking").setValue(maxProgressBar);
        } else if (obj instanceof Feed) {
            key = ((Feed) obj).getId();
            isActive = ((Feed) obj).isActive();
            option = firebaseDB.FEEDS;
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarFeeding").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarFeeding").setValue(maxProgressBar);
            Log.d("TAG", "updateTimeToDB: (FEEDS) 3" + key + " Time: " + isActive);
        } else if (obj instanceof Beauty) {
            key = ((Beauty) obj).getId();
            isActive = ((Beauty) obj).isActive();
            option = firebaseDB.BEAUTY;
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarBeauty").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarBeauty").setValue(maxProgressBar);
        } else {
            key = ((Health) obj).getId();
            isActive = ((Health) obj).isActive();
            option = firebaseDB.HEALTH;
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("fillProgressBarHealth").setValue(fillProgressBar);
            getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child("maxProgressBarHealth").setValue(maxProgressBar);
        }
        Log.d("TAG", "updateTimeToDB: " + isActive);
        //Remove option from specific pet
        getDatabase().getReference().child(FAMILIES).child(family_key).child(PETS).child(pet_key).child(option).child(key).child("isActive").setValue(isActive);
    }

    public User getUser(){
        return user;
    }
}
