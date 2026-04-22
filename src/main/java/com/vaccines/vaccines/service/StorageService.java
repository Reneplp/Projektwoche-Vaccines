package com.vaccines.vaccines.service;

import com.vaccines.vaccines.model.Profile;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.io.FileReader;
import java.io.BufferedReader;

import java.util.ArrayList;

public class StorageService {
    private static final String FILE_PATH = "profiles.json";


    public void saveProfiles(ArrayList<Profile> profiles) {
        Gson gson = new Gson();
        String json = gson.toJson(profiles);

        try {
            FileWriter writer = new FileWriter(FILE_PATH);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            System.out.println("Something went wrong when saving...");
        }
    }

    public ArrayList<Profile> loadProfiles() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String json = sb.toString();

            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Profile>>() {
            }.getType();
            return gson.fromJson(json, type);

        } catch (IOException e) {
            System.out.println("Something went wrong when reading...");
            return new ArrayList<>();
        }
    }
}
