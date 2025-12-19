package com;

import com.crdt.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class Client {

    private static Gson gson ;
    private static String BASE_URL;

    public static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static Type commentMapType = new TypeToken<Map<Integer, Comment[]>>() {}.getType();
    private static Type id_vote_type = new TypeToken<Map<Integer, Integer>>() {}.getType();

    public static void init() {
        RuntimeTypeAdapterFactory<User> userAdapter =
                RuntimeTypeAdapterFactory.of(User.class, "type")
                        .registerSubtype(User.class, "user")
                        .registerSubtype(Moderator.class, "moderator")
                        .registerSubtype(Admin.class, "admin");
        RuntimeTypeAdapterFactory<Reportable> reportableAdapter =
                RuntimeTypeAdapterFactory.of(Reportable.class, "type")
                        .registerSubtype(User.class, "user")
                        .registerSubtype(Post.class, "post")
                        .registerSubtype(Comment.class, "comment");
        RuntimeTypeAdapterFactory<Voteable> voteableAdapter =
                RuntimeTypeAdapterFactory.of(Voteable.class, "type")
                        .registerSubtype(Post.class, "post")
                        .registerSubtype(Comment.class, "comment");
        gson = new GsonBuilder().registerTypeAdapterFactory(userAdapter).registerTypeAdapterFactory(reportableAdapter).registerTypeAdapterFactory(voteableAdapter).create();
        BASE_URL = System.getenv("BASE_URL");
    }

    public static void cleanup() {
        THREAD_POOL.shutdown();
        try {
            if (!THREAD_POOL.awaitTermination(5, TimeUnit.SECONDS)) {
                THREAD_POOL.shutdownNow();
            }
        } catch (InterruptedException e) {
            THREAD_POOL.shutdownNow();
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //BOOKMARK: Helpers

    public static boolean isServerReachable() {
        try {
            URL url = new URL(BASE_URL + "/ping");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    return "yes".equals(in.readLine());
                }
            }
        } catch (IOException e) {
            System.out.println("Server unreachable: " + e.getMessage());
        }
        return false;
    }

    public static Map<?,?> GetResponse(String uploadResponse) {
        Map<?, ?> json = gson.fromJson(uploadResponse, Map.class);
        return json;
    }

    public static String UploadFile(File file) throws Exception {
        String boundary = "----Boundary" + System.currentTimeMillis();
        URL url = new URL(BASE_URL + "/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
            out.writeBytes("Content-Type: " + Files.probeContentType(file.toPath()) + "\r\n\r\n");
            Files.copy(file.toPath(), out);
            out.writeBytes("\r\n--" + boundary + "--\r\n");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();
        return response.toString();
    }




    //BOOKMARK: User

    public static User GetUser(int id) throws Exception {
        URL url = new URL(BASE_URL + String.format("/user?id=%s", java.net.URLEncoder.encode(String.valueOf(id), "UTF-8")));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return gson.fromJson(sb.toString(), User.class);
    }

    public static User login(String usermail, String password) throws Exception {
        return User.login(usermail, password, BASE_URL, gson);
    }

    public static void keepAlive(User user) {
        if(user == null)
            return;
        try {
            user.keepAlive(BASE_URL, gson);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean register(User user) throws Exception {
        if(user == null)
            return false;
        return user.register(BASE_URL, gson);
    }

    public static int CheckVote(User user, Voteable voteable) throws Exception {
        return user.CheckVote(voteable, BASE_URL, gson);
    }

    public static boolean Vote(User user, Voteable voteable, int value) {
        THREAD_POOL.submit(() -> {
            try {
                user.Vote(voteable, value, BASE_URL, gson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    public static boolean JoinSubcreddit(User user, Subcreddit sub) {
        THREAD_POOL.submit(() -> {
            try {
                user.joinSubcreddit(sub, BASE_URL, gson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    public static boolean LeaveSubcreddit(User user, Subcreddit sub) {
        if(user.equals(sub.GetCreator()))
            return false;
        THREAD_POOL.submit(() -> {
            try {
                user.leaveSubcreddit(sub, BASE_URL, gson);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }

    public static boolean IsSubMember(User user, Subcreddit sub) throws Exception {
        return user.isMember(sub, BASE_URL, gson);
    }

    public static boolean VerifyModeration(User user, Subcreddit sub) throws Exception {
        return sub.VerifyModeration(user, BASE_URL, gson);
    }

    public static ArrayList<User> GetFriends(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetFriends(BASE_URL, gson)));
    }

    public static ArrayList<User> GetSentFriendRequests(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetSentFriendRequests(BASE_URL, gson)));
    }

    public static ArrayList<User> GetReceivedFriendRequests(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetReceivedFriendRequests(BASE_URL, gson)));
    }

    public static boolean SendFriendRequest(User sender, User receiver) throws Exception {
        return sender.sendFriendRequest(receiver, BASE_URL, gson);
    }

    public static boolean AcceptFriendRequest(User sender, User receiver) throws Exception {
        return receiver.acceptFriend(sender, BASE_URL, gson);
    }

    public static boolean Unfriend(User me, User friend) throws Exception {
        return me.unfriend(friend, BASE_URL, gson);
    }

    public static ArrayList<Subcreddit> GetUserSubcreddits(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetSubcreddits(BASE_URL, gson)));
    }

    public static Map<Post, Integer> GetPostFeed(User user, String prompt, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/feed");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JsonObject jsonObj = gson.fromJson(sb.toString(), JsonObject.class);
        Post[] posts = gson.fromJson(jsonObj.get("posts"), Post[].class);
        Integer[] myVotes = gson.fromJson(jsonObj.get("votes"), Integer[].class);

        Map<Post, Integer> postMap = new LinkedHashMap<>();
        int sz = posts.length;
        for(int i = 0; i < sz; i++)
            postMap.put(posts[i], myVotes[i]);

        return postMap;
    }

    public static Map<Post, Integer> GetPostFeedFilterSub(User user, Subcreddit sub, String prompt, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.add("sub", gson.toJsonTree(sub));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/feed/filter-sub");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JsonObject jsonObj = gson.fromJson(sb.toString(), JsonObject.class);
        Post[] posts = gson.fromJson(jsonObj.get("posts"), Post[].class);
        Integer[] myVotes = gson.fromJson(jsonObj.get("votes"), Integer[].class);

        Map<Post, Integer> postMap = new LinkedHashMap<>();
        int sz = posts.length;
        for(int i = 0; i < sz; i++)
            postMap.put(posts[i], myVotes[i]);

        return postMap;
    }

    public static Map<Post, Integer> GetPostFeedFilterAuthor(User user, User author, String prompt, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.add("author", gson.toJsonTree(author, User.class));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/post/feed/filter-author");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JsonObject jsonObj = gson.fromJson(sb.toString(), JsonObject.class);
        Post[] posts = gson.fromJson(jsonObj.get("posts"), Post[].class);
        Integer[] myVotes = gson.fromJson(jsonObj.get("votes"), Integer[].class);

        Map<Post, Integer> postMap = new LinkedHashMap<>();
        int sz = posts.length;
        for(int i = 0; i < sz; i++)
            postMap.put(posts[i], myVotes[i]);

        return postMap;
    }

    public static ArrayList<Post> GetPostFeedFilterVote(User user, String prompt, int voteValue, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url;
        if(voteValue > 0)
            url = new URL(BASE_URL + "/post/feed/filter-upvote");
        else
            url = new URL(BASE_URL + "/post/feed/filter-downvote");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return new ArrayList<>(Arrays.asList(gson.fromJson(sb.toString(), Post[].class)));
    }

    public static boolean editUser(User user) throws Exception {
        return user.update(BASE_URL, gson);
    }



    //BOOKMARK: Post

    public static int CreatePost(Post post) throws Exception {
        return post.create(BASE_URL, gson);
    }
    public static boolean DeletePost(Post post) throws Exception {
        return post.delete(BASE_URL, gson);
    }
    public static boolean EditPost(Post post) throws Exception {
        return post.update(BASE_URL, gson);
    }

    public static Map<Post, Integer> GetPost(int id, User user) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.addProperty("id", id);
        String jsonBody = gson.toJson(json);
        URL url = new URL(BASE_URL + "/post");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JsonObject jsonObj = gson.fromJson(sb.toString(), JsonObject.class);
        Post post = gson.fromJson(jsonObj.get("post"), Post.class);
        int myVote = gson.fromJson(jsonObj.get("vote"), int.class);

        Map<Post, Integer> postMap = new HashMap<>();
        postMap.put(post, myVote);
        return postMap;
    }

    public static String[] GetAllCategories() throws Exception {
        URL url = new URL(BASE_URL + "/category/all");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return gson.fromJson(sb.toString(), String[].class);
    }





    //BOOKMARK: Report

    public static boolean submitReport(Report report) throws Exception {
        return report.SubmitReport(BASE_URL, gson);
    }

    public static boolean ReportExists(Report report) throws Exception {
        return report.Exists(BASE_URL, gson);
    }

    public static boolean ResolveReport(Report report) throws Exception {
        return report.Resolve(BASE_URL, gson);
    }

    public static boolean DismissReport(Report report) throws Exception {
        return report.Dismiss(BASE_URL, gson);
    }

    public static ArrayList<Report> GetUserReportFeed(Admin admin, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("admin", gson.toJsonTree(admin, Admin.class));
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/report/feed/users");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return new ArrayList<>(Arrays.asList(gson.fromJson(sb.toString(), Report[].class)));
    }

    public static ArrayList<Report> GetPostReportFeed(User user, Subcreddit sub, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.add("sub", gson.toJsonTree(sub, User.class));
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/report/feed/posts");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return new ArrayList<>(Arrays.asList(gson.fromJson(sb.toString(), Report[].class)));
    }

    public static ArrayList<Report> GetCommentReportFeed(User user, Subcreddit sub, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.add("sub", gson.toJsonTree(sub, User.class));
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/report/feed/comments");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return new ArrayList<>(Arrays.asList(gson.fromJson(sb.toString(), Report[].class)));
    }


    //BOOKMARK: Subcreddit

    public static ArrayList<Subcreddit> GetSubFeed(User user, String prompt, int lastID) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.addProperty("prompt", prompt);
        json.addProperty("lastID", lastID);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/subcreddit/feed");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JsonObject jsonObj = gson.fromJson(sb.toString(), JsonObject.class);
        ArrayList<Subcreddit> subs = new ArrayList<>(Arrays.asList(gson.fromJson(jsonObj.get("subs"), Subcreddit[].class)));

        return subs;
    }

    public static Subcreddit[] GetAllSubcreddits() throws Exception {
        URL url = new URL(BASE_URL + "/subcreddit/all");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return gson.fromJson(sb.toString(), Subcreddit[].class);
    }

    public static Subcreddit GetSubcreddit(int id) throws Exception {
        URL url = new URL(BASE_URL + String.format("/subcreddit?id=%s", java.net.URLEncoder.encode(String.valueOf(id), "UTF-8")));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        return gson.fromJson(sb.toString(), Subcreddit.class);
    }

    public static ArrayList<User> GetSubMembers(Subcreddit sub, int lastID) throws Exception {
        return new ArrayList<>(Arrays.asList(sub.GetMemberFeed(lastID, BASE_URL, gson)));
    }

    public static int CreateSubcreddit(Subcreddit sub) throws Exception {
        return sub.create(BASE_URL, gson);
    }

    public static boolean EditSubcreddit(Subcreddit sub) throws Exception {
        return sub.update(BASE_URL, gson);
    }

    public static boolean DeleteSubcreddit(Subcreddit sub) throws Exception {
        return sub.delete(BASE_URL, gson);
    }




    //BOOKMARK: Private Message

    public static boolean SendPM(Message msg) throws Exception {
        return msg.send(BASE_URL, gson);
    }

    public static boolean DeletePM(Message msg) throws Exception {
        return msg.delete(BASE_URL, gson);
    }

    public static boolean ReadMessage(User user, User friend) throws Exception {
        return user.ReadMessages(friend, BASE_URL, gson);
    }

    public static ArrayList<Message> GetUnreadPM(User user) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetUnreadPrivateMessages(BASE_URL, gson)));
    }

    public static ArrayList<Message> GetPMFeed(User user, User friend, int lastID) throws Exception {
        return new ArrayList<>(Arrays.asList(user.GetPrivateMessageFeed(friend, lastID, BASE_URL, gson)));
    }




    //BOOKMARK: Comment

    public static CommentFeed GetPostCommentFeed(User user, Post post, int lastID) throws Exception {
        return post.GetCommentFeed(user, lastID, BASE_URL, gson);
    }

    public static CommentFeed GetCommentFeed(User user, int commentid) throws Exception {
        JsonObject json = new JsonObject();
        json.add("user", gson.toJsonTree(user, User.class));
        json.addProperty("commentid", commentid);

        String jsonBody = gson.toJson(json);

        URL url = new URL(BASE_URL + "/comment/feed");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JsonObject jsonObj = gson.fromJson(sb.toString(), JsonObject.class);
        Comment comment = gson.fromJson(jsonObj.get("parent"), Comment.class);
        Comment[] parents = {comment};
        Map<Integer, Comment[]> lv1 = gson.fromJson(jsonObj.get("lv1"), commentMapType);
        Map<Integer, Comment[]> lv2 = gson.fromJson(jsonObj.get("lv2"), commentMapType);
        Map<Integer, Integer> votes = gson.fromJson(jsonObj.get("lv2"), id_vote_type);

        return new CommentFeed(parents, lv1, lv2, votes);
    }

    public static int CreateComment(Comment comment) throws Exception {
        return comment.create(BASE_URL, gson);
    }
    public static boolean DeleteComment(Comment comment) throws Exception {
        return comment.delete(BASE_URL, gson);
    }
    public static boolean EditComment(Comment comment) throws Exception {
        return comment.update(BASE_URL, gson);
    }




    public static ArrayList<Integer> GetAnalytics(User user) throws Exception {
        if(user instanceof Admin) {
            String jsonBody = gson.toJson(user, User.class);
            URL url = new URL(BASE_URL + "/analytics");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            return new ArrayList<>(Arrays.asList(gson.fromJson(sb.toString(), Integer[].class)));
        }
        else {
            return null;
        }
    }
}