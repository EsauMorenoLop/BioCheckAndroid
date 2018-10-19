package net.biocheck.biocheckmovil.service;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.sql.Time;

/**
 * Created by gabriel on 1/03/18.
 */

public class TimeAdapter implements JsonDeserializer<Time>, JsonSerializer<Time> {

    @Override
    public Time deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String[] timeParts = json.getAsString().split(":");
        Time time = new Time(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]), 0 );
        return time;
    }

    @Override
    public JsonElement serialize(Time src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }
}
