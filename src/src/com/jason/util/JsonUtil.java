package com.jason.util;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jason.lawgarden.model.Article;
import com.jason.lawgarden.model.News;
import com.jason.lawgarden.model.Subject;

public class JsonUtil {
    private static final String TAG = "JsonUtil";

    private static final String SERVICE_URI = "http://s-58277.gotocdn.com:8080/Service.svc";

    private static String sAccessToken;

    public static void register() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Username", "jason");
        object.put("Password", "123456");

        String appListString = HttpUtil.doPost(SERVICE_URI + "/Register", object);
        JSONObject objectRet = new JSONObject(appListString);
        // objectRet.getBoolean("ExecutionResult");
    }

    public static String login(String username, String password) throws JSONException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2013, 3, 18);
        sDATE_FOR_TEST = String.format("V(Date(%s+0800)", calendar.getTimeInMillis());

        JSONObject object = new JSONObject();
        object.put("Username", "jason");
        object.put("Password", "123456");

        String appListString = HttpUtil.doPost(SERVICE_URI + "/Login", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            return objectRet.getString("AccessToken");
        }
        return null;
    }

    public static void getUserSubjects() {
        // TODO:implement the method
    }

    private static String sDATE_FOR_TEST;

    public static void updateSubjects() throws JSONException {

        JSONObject object = new JSONObject();
        object.put("AccesToken", sAccessToken);
        object.put("LastUpdateTime", sDATE_FOR_TEST);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateSubjects", object);
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

                subjects.add(subject);
            }
            // TODO: add the subjects to db

            // remove the subjects:
            JSONArray removedArray = objectRet.getJSONArray("RemovedSubjectIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // TODO: remove the subjects by ids
        }

    }

    public static void updateNews() throws JSONException {

        JSONObject object = new JSONObject();
        object.put("AccesToken", sAccessToken);
        object.put("LastUpdateTime", sDATE_FOR_TEST);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateSubjects", object);
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

                newsList.add(news);
            }
            // TODO: add the subjects to db

            // remove the subjects:
            JSONArray removedArray = objectRet.getJSONArray("RemovedNewsIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // TODO: remove the news by ids
        }
    }

    public static void updateArticles() throws JSONException {

        JSONObject object = new JSONObject();
        object.put("AccesToken", sAccessToken);
        object.put("LastUpdateTime", sDATE_FOR_TEST);
        String appListString = HttpUtil.doPost(SERVICE_URI + "/UpdateSubjects", object);
        JSONObject objectRet = new JSONObject(appListString);
        if (objectRet.getBoolean("ExecutionResult")) {
            JSONArray array = objectRet.getJSONArray("UpdateArticles");
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
                article.setLastUpdateTime(obj.getString("Title"));

                articles.add(article);
            }
            // TODO: add the articles to db

            // remove the articles:
            JSONArray removedArray = objectRet.getJSONArray("RemovedArticleIds");
            int[] removedIds = new int[removedArray.length()];
            for (int i = 0; i < removedArray.length(); i++) {
                removedIds[i] = removedArray.getInt(i);
            }
            // TODO: remove the articles by ids
        }
    }
}
