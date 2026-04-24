package com.vaccines.vaccines.service;

import com.vaccines.vaccines.model.Profile;
import com.google.gson.Gson;

import java.io.*;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.ArrayList;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonPrimitive;
import java.time.LocalDate;

public class StorageService {
    private static final String FILE_PATH = "profiles.json";


    public void saveProfiles(ArrayList<Profile> profiles) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
                        (date, type, context) -> new JsonPrimitive(date.toString()))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                        (element, type, context) -> LocalDate.parse(element.getAsString()))
                .create();
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

            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String json = sb.toString();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>)
                            (date, type, context) -> new JsonPrimitive(date.toString()))
                    .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>)
                            (element, type, context) -> LocalDate.parse(element.getAsString()))
                    .create();
            Type type = new TypeToken<ArrayList<Profile>>() {
            }.getType();
            return gson.fromJson(json, type);

        } catch (IOException e) {
            System.out.println("Something went wrong when reading...");
            return new ArrayList<>();
        }
    }
}
