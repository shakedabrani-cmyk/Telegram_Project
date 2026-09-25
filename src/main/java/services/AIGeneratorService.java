package services;

import models.Question;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AIGeneratorService {

    private static final String TOKEN = "4ynnJSbIuaORGNUZ9IYEgro69H5tfoY6ORuroFYpZ1qOdOZ7bqaxFVd8q31IGXed";
    private static final String API_URL = "https://shaitest-production-3066.up.railway.app/api-request";

    private static final String JSON_FORMAT_REQUIREMENT =
            " אתה חייב להחזיר אך ורק טקסט בפורמט JSON תקני, בלי שום מילות הסבר לפני או אחרי. " +
                    "מבנה ה-JSON חייב להיות מערך של אובייקטים, כך: " +
                    "[ { \"question\": \"טקסט השאלה\", \"options\": [\"אופציה 1\", \"אופציה 2\"] } ]";

    private static final String JSON_KEY_VALUE = "value";
    private static final String JSON_KEY_QUESTION = "question";
    private static final String JSON_KEY_OPTIONS = "options";

    private static final String ERROR_HTTP_FAILED = "שגיאה בתקשורת מול השרת. קוד שגיאה: ";
    private static final String ERROR_EMPTY_RESPONSE = "התקבלה תגובה ריקה מהשרת.";
    private static final String ERROR_MISSING_VALUE = "שגיאה מהשרת: הערך 'value' לא נמצא.";
    private static final String ERROR_INVALID_JSON_FORMAT = "לא נמצא פורמט JSON תקין בתשובת השרת.";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    public List<Question> generateSurveyQuestions(String prompt) throws Exception {

        String finalPrompt = prompt + JSON_FORMAT_REQUIREMENT;

        HttpUrl url = HttpUrl.parse(API_URL).newBuilder()
                .addQueryParameter("token", TOKEN)
                .addQueryParameter("text", finalPrompt)
                .build();

        Request request = new Request.Builder().url(url).build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new Exception(ERROR_HTTP_FAILED + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new Exception(ERROR_EMPTY_RESPONSE);
            }

            String responseBody = body.string();
            JSONObject json = new JSONObject(responseBody);

            if (json.has(JSON_KEY_VALUE)) {
                String jsonString = json.getString(JSON_KEY_VALUE);
                return parseJsonToQuestions(jsonString);
            } else {
                throw new Exception(ERROR_MISSING_VALUE);
            }
        }
    }

    private List<Question> parseJsonToQuestions(String jsonString) throws Exception {

        String cleanJson = jsonString.trim();

        int startIndex = cleanJson.indexOf('[');
        int endIndex = cleanJson.lastIndexOf(']');

        if (startIndex != -1 && endIndex != -1) {
            cleanJson = cleanJson.substring(startIndex, endIndex + 1);
        } else {
            throw new Exception(ERROR_INVALID_JSON_FORMAT);
        }

        List<Question> questionsList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(cleanJson);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject qObject = jsonArray.getJSONObject(i);
            String text = qObject.getString(JSON_KEY_QUESTION);

            JSONArray optionsArray = qObject.getJSONArray(JSON_KEY_OPTIONS);
            List<String> options = new ArrayList<>();
            for (int j = 0; j < optionsArray.length(); j++) {
                options.add(optionsArray.getString(j));
            }

            questionsList.add(new Question(text, options));
        }

        return questionsList;
    }
}