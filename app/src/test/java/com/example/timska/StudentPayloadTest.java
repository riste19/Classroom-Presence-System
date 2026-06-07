package com.example.timska;

import static org.junit.Assert.assertEquals;

import com.example.timska.nfc.StudentPayload;

import org.json.JSONException;
import org.junit.Test;

/** Verifies the NFC payload serializes and parses back losslessly. */
public class StudentPayloadTest {

    @Test
    public void roundTripPreservesFields() throws JSONException {
        StudentPayload original = new StudentPayload("201234", "Ана Стоилкова", "Мобилни апликации");

        StudentPayload parsed = StudentPayload.fromBytes(original.toBytes());

        assertEquals(original.studentId, parsed.studentId);
        assertEquals(original.studentName, parsed.studentName);
        assertEquals(original.course, parsed.course);
    }

    @Test
    public void nullCourseBecomesEmptyString() throws JSONException {
        StudentPayload original = new StudentPayload("201235", "Бојан Петров", null);

        StudentPayload parsed = StudentPayload.fromBytes(original.toBytes());

        assertEquals("", parsed.course);
    }
}
