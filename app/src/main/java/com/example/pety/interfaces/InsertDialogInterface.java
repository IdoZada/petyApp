package com.example.pety.interfaces;

import android.net.Uri;

public interface InsertDialogInterface {
    void applyTexts(String familyName, String familyImagePath, Uri imageUri);
    void applyAttPet(String petName,String petType,String birthday,String petImagePath,Uri imageUri);
    void applyTime(String time,String op);
    void applyTimeDate(String time, String op);
}
