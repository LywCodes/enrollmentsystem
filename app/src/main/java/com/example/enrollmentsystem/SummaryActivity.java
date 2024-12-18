package com.example.enrollmentsystem;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class SummaryActivity extends AppCompatActivity {
    private ListView lvEnrollmentSummary;
    private TextView tvTotalCredits;
    private ArrayList<String> summaryList = new ArrayList<>();
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        lvEnrollmentSummary = findViewById(R.id.lvEnrollmentSummary);
        tvTotalCredits = findViewById(R.id.tvTotalCredits);

        studentId = getIntent().getStringExtra("student_id");

        if (studentId == null) {
            Toast.makeText(this, "No student ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String url = "http://192.168.224.123/wmp-final/enrollment_summary.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                if (!jsonResponse.getString("status").equals("success")) {
                    Toast.makeText(this, "Failed to retrieve summary", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONArray summaryArray = jsonResponse.getJSONArray("summary");
                int totalCredits = jsonResponse.getInt("total_credits");

                summaryList.clear();
                for (int i = 0; i < summaryArray.length(); i++) {
                    JSONObject subject = summaryArray.getJSONObject(i);
                    summaryList.add(subject.getString("name") + " (" + subject.getInt("credits") + " credits)");
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, summaryList);
                lvEnrollmentSummary.setAdapter(adapter);
                tvTotalCredits.setText("Total Credits: " + totalCredits);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing summary data", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(this, "Failed to load summary: " + error.toString(), Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("student_id", studentId);
                return params;
            }
        };

        queue.add(request);
    }
}