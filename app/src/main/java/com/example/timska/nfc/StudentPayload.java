package com.example.timska.nfc;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/** The data a student broadcasts over NFC, serialized as compact JSON. */
public class StudentPayload {

    public final String studentId;
    public final String studentName;
    public final String course;

    public StudentPayload(String studentId, String studentName, String course) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.course = course;
    }

    public byte[] toBytes() {
        JSONObject json = new JSONObject();
        try {
            json.put("sid", studentId);
            json.put("name", studentName);
            json.put("course", course == null ? "" : course);
        } catch (JSONException ignored) {
            // keys are constant, cannot fail
        }
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static StudentPayload fromBytes(byte[] bytes) throws JSONException {
        JSONObject json = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
        return new StudentPayload(
                json.optString("sid"),
                json.optString("name"),
                json.optString("course"));
    }
}
