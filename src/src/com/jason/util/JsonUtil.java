package com.jason.util;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jason.lawgarden.db.DataBaseHelper;
import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.News;
import com.jason.lawgarden.model.Subject;
import com.jason.lawgarden.model.User;
import com.jason.lawgarden.model.UserSubjects;

public class JsonUtil {
    private static final String TAG = "JsonUtil";

    private static final String SERVICE_URI = "http://s-58277.gotocdn.com:8080/Service.svc";

    public static String sAccessToken = "rTacjaF7CUS6qbA2B74Q4Q";

    public static User sUser;

    public static boolean register(String userName, String password, String[] message)
            throws JSONException {
        Log.d(TAG, "register");
        JSONObject object = new JSONObject();
        object.put("Username", userName);
        object.put("Password", password);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/Register", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (TextUtils.isEmpty(appListString)) {
            return false;
        }
        if (objectRet.getBoolean("ExecutionResult")) {
            Log.d(TAG, "register:sucess");
            // sAccessToken = objectRet.getString("AccessToken");
            message[0] = "×¢²á³É¹¦";
            return true;
        } else {
            message[0] = objectRet.getString("Message");
        }
        return false;
    }

    public static JSONObject login(String username, String password) throws JSONException {
        Log.d(TAG, "login");
        Calendar calendar = Calendar.getInstance();
        calendar.set(1997, 2, 1);
        sDATE_FOR_TEST = String.format("/Date(%s+0800)/", calendar.getTimeInMillis());

        JSONObject object = new JSONObject();
        object.put("Username", username);
        object.put("Password", password);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/Login", object);
        if (TextUtils.isEmpty(appListString)) {
            return null;
        }
        JSONObject objectRet = new JSONObject(appListString);
        // if (objectRet.getBoolean("ExecutionResult")) {
        // return objectRet.getString("AccessToken");
        // }
        return objectRet;
    }

    public static void updateUserSubjects(Context context) throws JSONException {
        Log.d(TAG, "updateUserSubjects");
        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/GetUserSubjects", object);
        if (TextUtils.isEmpty(appListString)) {
            Log.d(TAG, "get updateUserSubjects api error");
            return;
        }

        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("Subjects");
            ArrayList<UserSubjects> subjects = new ArrayList<UserSubjects>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                UserSubjects subject = new UserSubjects();
                subject.setDescription(obj.getString("Description"));
                // subject.setId(obj.getInt("Id"));
                subject.setIsPrivate(obj.getBoolean("IsPrivate") ? 1 : 0);
                subject.setLastUpdateTime(obj.getString("LastUpdateTime"));
                subject.setName(obj.getString("Name"));
                subject.setOrderId(obj.getInt("OrderId"));
                subject.setParentId(obj.getInt("Id"));

                subjects.add(subject);
            }
            // add the subjects to db
            DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
            dbHelper.insertUserSubjects(subjects);
        }
    }

    private static String sDATE_FOR_TEST = "/Date(1362575535693+0800)/";

    public static void updateSubjects(Context context) throws JSONException {
        Log.d(TAG, "updateSubjects");
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
        String lastUpdateSubjectTime = dbHelper.getLastUpdateSubjectTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateSubjects", object);
        if (TextUtils.isEmpty(appListString)) {
            Log.d(TAG, "get updateSubjects api error");
            return;
        }
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("Subjects");
            ArrayList<Subject> subjects = new ArrayList<Subject>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Subject subject = new Subject();
                subject.setDescription(obj.getString("Description"));
                subject.setId(obj.getInt("Id"));
                subject.setPrivate(obj.getBoolean("IsPrivate"));
                subject.setLastUpdateTime(obj.getString("LastUpdateTime"));
                subject.setName(obj.getString("Name"));
                subject.setOrderId(obj.getInt("OrderId"));
                subject.setParentId(obj.getInt("ParentId"));
                subject.setNew(true);

                subjects.add(subject);
            }
            // add the subjects to db
            dbHelper.insertSubjects(subjects);

            // remove the subjects:
            JSONArray removedArray = objectRet.getJSONArray("RemovedSubjectIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // remove the subjects by ids
            dbHelper.removeSubjectsByIds(removedIds);
        }
    }

    public static void updateNews(Context context) throws JSONException {
        Log.d(TAG, "updateNews");
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
        String lastUpdateSubjectTime = dbHelper.getLastUpdateNewsTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateNews", object);

        if (TextUtils.isEmpty(appListString)) {
            Log.d(TAG, "get appListString api error");
            return;
        }

        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("News");
            ArrayList<News> newsList = new ArrayList<News>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                News news = new News();

                news.setId(obj.getInt("Id"));
                news.setTitle(obj.getString("Title"));
                news.setContent(obj.getString("Contents"));
                news.setSource(obj.getString("Source"));
                news.setValidTime(obj.getString("ValidTime"));
                news.setLastUpdateTime(obj.getString("LastUpdateTime"));
                news.setUri(obj.getString("DefaultImageUrl"));

                newsList.add(news);
            }
            // add the news to db
            dbHelper.insertNews(newsList);

            // remove the subjects:
            JSONArray removedArray = objectRet.getJSONArray("RemovedNewsIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // remove the news by ids
            dbHelper.removeNewsByIds(removedIds);
        }
    }

    public static int updateArticles(Context context, int PageIndex, String lastUpdateSubjectTime)
            throws JSONException {
        Log.d(TAG, "updateArticles");
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
        // String lastUpdateSubjectTime = dbHelper.getLastUpdateArticleTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);
        object.put("PageIndex", PageIndex);
        object.put("PageSize", 100);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateArticles", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("Articles");
            ArrayList<Article> articles = new ArrayList<Article>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Article article = new Article();

                article.setContents(obj.getString("Contents"));
                article.setId(obj.getInt("Id"));
                article.setTitle(obj.getString("Keywords"));
                article.setLastUpdateTime(obj.getString("LastUpdateTime"));
                article.setLevel(obj.getInt("Level"));
                article.setSubjects(obj.getString("Subjects"));
                article.setTitle(obj.getString("Title"));
                article.setNew(true);

                articles.add(article);
            }
            // add the articles to db
            dbHelper.insertArticles(articles);

            // remove the articles:
            JSONArray removedArray = objectRet.getJSONArray("RemovedArticleIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // remove the articles by ids
            dbHelper.removeArticlesByIds(removedIds);
        }

        return objectRet.getInt("TotalPages");
    }

    public static boolean CheckSubjectUpdates(Context context) throws JSONException {
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
        String lastUpdateSubjectTime = dbHelper.getLastUpdateSubjectTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/CheckSubjectUpdates", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            return objectRet.getBoolean("UpdatesExist");
        }

        return false;
    }

    public static boolean CheckNewsUpdates(Context context) throws JSONException {
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
        String lastUpdateSubjectTime = dbHelper.getLastUpdateNewsTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/CheckNewsUpdates", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            return objectRet.getBoolean("UpdatesExist");
        }

        return false;
    }

    public static boolean CheckArticleUpdates(Context context) throws JSONException {
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
        String lastUpdateSubjectTime = dbHelper.getLastUpdateArticleTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTime",
                !TextUtils.isEmpty(lastUpdateSubjectTime) ? lastUpdateSubjectTime : sDATE_FOR_TEST);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/CheckArticleUpdates", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            return objectRet.getBoolean("UpdatesExist");
        }

        return false;
    }

    public static boolean CheckAllUpdates(Context context) throws JSONException {
        DataBaseHelper dbHelper = DataBaseHelper.getSingleInstance(context);
        String LastUpdateTimeOfNews = dbHelper.getLastUpdateNewsTime();
        String LastUpdateTimeOfSubjects = dbHelper.getLastUpdateSubjectTime();
        String LastUpdateTimeOfArticles = dbHelper.getLastUpdateArticleTime();

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);
        object.put("LastUpdateTimeOfNews", LastUpdateTimeOfNews);
        object.put("LastUpdateTimeOfSubjects", LastUpdateTimeOfSubjects);
        object.put("LastUpdateTimeOfArticles", LastUpdateTimeOfArticles);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/CheckAllUpdates", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            return objectRet.getBoolean("NewsUpdatesExist")
                    || objectRet.getBoolean("SubjectUpdatesExist")
                    || objectRet.getBoolean("ArticleUpdatesExist");
        }

        return false;
    }

    public static JSONObject ValidateToken(Context context) throws JSONException {

        JSONObject object = new JSONObject();
        object.put("AccessToken", sAccessToken);

        String appListString = HttpUtil.doPost(SERVICE_URI + "/ValidateToken", object);
        JSONObject objectRet = new JSONObject(appListString);

        return objectRet;
    }

}
