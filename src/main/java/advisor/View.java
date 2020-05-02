package advisor;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class View {

    public static void print(JsonObject json) {
        var keys = new ArrayList<>(json.keySet());
        var key = keys.get(keys.size() - 1);

        switch (key) {
            case "albums":
                printNewReleases(json);
                break;
            case "categories":
                printCategories(json);
                break;
            case "playlists":
                printPlaylists(json);
                break;
            case "error":
                printError(json);
                break;
        }
    }

    private static void printNewReleases(JsonObject json) {
        var albums = json.get("albums").getAsJsonObject();

        albums.get("items").getAsJsonArray().forEach(item -> {
            var album = item.getAsJsonObject();
            var name = album.get("name").getAsString();
            var url = album.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            var artists = new ArrayList<>();
            album.get("artists").getAsJsonArray().forEach(artist ->
                    artists.add(artist.getAsJsonObject().get("name").getAsString()));
            System.out.println(name);
            System.out.println(artists);
            System.out.println(url + "\n");
        });

        printPage(albums);
    }

    private static void printCategories(JsonObject json) {
        var categories = json.get("categories").getAsJsonObject();

        categories.get("items").getAsJsonArray().forEach(item -> {
            var category = item.getAsJsonObject();
            var name = category.get("name").getAsString();
            System.out.println(name);
        });

        printPage(categories);
    }

    private static void printPlaylists(JsonObject json) {
        var playlists = json.get("playlists").getAsJsonObject();

        playlists.get("items").getAsJsonArray().forEach(item -> {
            var playlist = item.getAsJsonObject();
            var name = playlist.get("name").getAsString();
            var url = playlist.get("external_urls").getAsJsonObject().get("spotify").getAsString();
            System.out.println(name);
            System.out.println(url + "\n");
        });

        printPage(playlists);
    }

    private static void printError(JsonObject json) {
        System.out.println(json.get("error").getAsJsonObject().get("message").getAsString());
    }

    private static void printPage(JsonObject json) {
        int page = (json.get("offset").getAsInt() / json.get("limit").getAsInt()) + 1;
        int totalPages = (int) Math.ceil(json.get("total").getAsDouble() / json.get("limit").getAsDouble());
        System.out.println("---PAGE " + page + " OF " + totalPages + "---");
    }
}
