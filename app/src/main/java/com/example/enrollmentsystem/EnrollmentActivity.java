package com.example.enrollmentsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EnrollmentActivity extends AppCompatActivity {

    private ListView lvSubjects;
    private Button btnEnroll;
    private Button btnSummary;
    private ArrayList<String> subjectList = new ArrayList<>();
    private ArrayList<Integer> subjectIds = new ArrayList<>();
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        lvSubjects = findViewById(R.id.lvSubjects);
        btnEnroll = findViewById(R.id.btnEnroll);
        btnSummary = findViewById(R.id.summary);

        studentId = getIntent().getStringExtra("student_id");

        // Fetch available subjects from the server
        String url = "http://192.168.224.123/wmp-final/get_subject.php"; // Update URL to your actual PHP file
        RequestQueue queue = Volley.newRequestQueue(this);


        btnSummary.setOnClickListener(v -> {
            Intent intent = new Intent(this, SummaryActivity.class);
            intent.putExtra("student_id", studentId);
            startActivity(intent);
        });

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getString("status").equals("success")) {
                    // Parse subjects and add to list
                    JSONArray jsonArray = jsonResponse.getJSONArray("subjects");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject subject = jsonArray.getJSONObject(i);
                        String subjectName = subject.getString("name");
                        int credits = subject.getInt("credits");
                        subjectList.add(subjectName + " (" + credits + " credits)");
                        subjectIds.add(subject.getInt("id"));
                    }


                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, subjectList);
                    lvSubjects.setAdapter(adapter);
                    lvSubjects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                } else {
                    Toast.makeText(this, "No subjects available", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(this, "Failed to load subjects", Toast.LENGTH_SHORT).show());

        queue.add(request);

        btnEnroll.setOnClickListener(v -> {
            SparseBooleanArray checkedItems = lvSubjects.getCheckedItemPositions();
            ArrayList<Integer> selectedSubjectIds = new ArrayList<>();

            for (int i = 0; i < checkedItems.size(); i++) {
                if (checkedItems.valueAt(i)) {
                    selectedSubjectIds.add(subjectIds.get(checkedItems.keyAt(i)));
                }
            }

            if (!selectedSubjectIds.isEmpty()) {
                enrollInSubjects(selectedSubjectIds);
            } else {
                Toast.makeText(this, "Please select at least one subject", Toast.LENGTH_SHORT).show();
            }
        });

    }



    private void enrollInSubjects(ArrayList<Integer> selectedSubjectIds) {
        String enrollUrl = "http://192.168.224.123/wmp-final/enroll_subject.php"; // Update to your actual PHP file
        RequestQueue queue = Volley.newRequestQueue(this);

        for (int subjectId : selectedSubjectIds) {
            StringRequest enrollRequest = new StringRequest(Request.Method.POST, enrollUrl, response -> {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("status").equals("success")) {
                        Toast.makeText(EnrollmentActivity.this, "Enrolled successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("Student ID", studentId); // Log to see if it's correct
                        Toast.makeText(EnrollmentActivity.this, "Failed to enroll, maximum of credits is 24", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(EnrollmentActivity.this, "Error enrolling", Toast.LENGTH_SHORT).show();
                }
            }, error -> Toast.makeText(EnrollmentActivity.this, "Enrollment failed", Toast.LENGTH_SHORT).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("student_id", studentId);
                    params.put("subject_id", String.valueOf(subjectId));
                    return params;
                }
            };

            queue.add(enrollRequest);
        }
    }
}
