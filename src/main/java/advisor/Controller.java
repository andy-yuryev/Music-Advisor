package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Controller {

    private static final String NEW = "https://api.spotify.com/v1/browse/new-releases";
    private static final String FEATURED = "https://api.spotify.com/v1/browse/featured-playlists";
    private static final String CATEGORIES = "https://api.spotify.com/v1/browse/categories";
    private static final String PLAYLISTS = "https://api.spotify.com/v1/browse/categories/%s/playlists";
    private static final String CLIENT_ID = "ef17d74bf4db4211870afc2ec3c2b783";
    private static final String CLIENT_SECRET = "7c4c81b44edd4412a4fb4f860b699adc";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static final String RESPONSE_TYPE = "code";
    private static final HttpClient client = HttpClient.newBuilder().build();
    private static HttpRequest request;
    private static HttpResponse<String> response;
    private static String authorizationCode;
    private static String accessToken;

    public static JsonObject getNewReleases(int limit) throws IOException, InterruptedException {
        response = sendRequest(NEW, limit);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject getFeatured(int limit) throws IOException, InterruptedException {
        response = sendRequest(FEATURED, limit);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject getCategories(int limit) throws IOException, InterruptedException {
        response = sendRequest(CATEGORIES, limit);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject getPlaylists(String categoryName, int limit) throws IOException, InterruptedException {
        String categoryId = "";
        int maxLimit = 50;
        var json = getCategories(maxLimit);
        while (json != null) {
            var categories = json.get("categories").getAsJsonObject().get("items").getAsJsonArray();
            for (var element : categories) {
                var category = element.getAsJsonObject();
                if (categoryName.equals(category.get("name").getAsString())) {
                    categoryId = category.get("id").getAsString();
                    break;
                }
            }
            json = getPage("next");
        }
        response = sendRequest(String.format(PLAYLISTS, categoryId), limit);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject getPage(String direction) throws IOException, InterruptedException {
        if (response == null) {
            return null;
        }
        var json = JsonParser.parseString(response.body()).getAsJsonObject();
        var keys = new ArrayList<>(json.keySet());
        var key = keys.get(keys.size() - 1);
        var elem = json.get(key).getAsJsonObject().get(direction);
        if ("null".equals(elem.toString())) {
            return null;
        }
        String url = elem.getAsString();
        response = sendRequest(url);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static boolean authorize() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println("https://accounts.spotify.com/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=" + RESPONSE_TYPE);
        System.out.println("waiting for code...");
        requestAccessCode();
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        HttpResponse<String> response = requestAccessToken();
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        accessToken = json.get("access_token").getAsString();
        return response.statusCode() == 200;
    }

    private static void requestAccessCode() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/",
                exchange -> {
                    String query = exchange.getRequestURI().getQuery();
                    String responseBody;
                    if (query != null && query.contains("code")) {
                        authorizationCode = query.substring(5);
                        latch.countDown();
                        responseBody = "Got the code. Return back to your program.";
                    } else {
                        responseBody = "Not found authorization code. Try again.";
                    }
                    exchange.sendResponseHeaders(200, responseBody.length());
                    exchange.getResponseBody().write(responseBody.getBytes());
                    exchange.getResponseBody().close();
                }
        );

        server.start();
        latch.await();
        server.stop(10);
    }

    private static HttpResponse<String> requestAccessToken() throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=" + GRANT_TYPE
                                + "&code=" + authorizationCode
                                + "&redirect_uri=" + REDIRECT_URI
                                + "&client_id=" + CLIENT_ID
                                + "&client_secret=" + CLIENT_SECRET))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendRequest(String url) throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(url))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendRequest(String url, int limit) throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(url + "?limit=" + limit))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
