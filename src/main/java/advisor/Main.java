package advisor;

import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        boolean authorized = false;
        int limit = 20;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-page")) {
                limit = Integer.parseInt(args[i + 1]);
            }
        }

        while (true) {
            String input = scanner.nextLine();
            String command = input.contains(" ") ? input.substring(0, input.indexOf(" ")) : input;
            if ("auth".equals(command)) {
                if (!authorized) {
                    authorized = Controller.authorize();
                    System.out.println(authorized ? "Success!" : "Failed");
                } else {
                    System.out.println("Already authorized");
                }
                continue;
            }
            if ("exit".equals(command)) {
                System.out.println("---GOODBYE!---");
                return;
            }
            if (authorized) {
                switch (command) {
                    case "new":
                        var newReleases = Controller.getNewReleases(limit);
                        View.print(newReleases);
                        break;
                    case "featured":
                        var featured = Controller.getFeatured(limit);
                        View.print(featured);
                        break;
                    case "categories":
                        var categories = Controller.getCategories(limit);
                        View.print(categories);
                        break;
                    case "playlists":
                        String categoryName = input.substring(input.indexOf(" ") + 1);
                        var playlists = Controller.getPlaylists(categoryName, limit);
                        View.print(playlists);
                        break;
                    case "next":
                        var nextPage = Controller.getPage("next");
                        if (nextPage == null) {
                            System.out.println("No more pages.");
                        } else {
                            View.print(nextPage);
                        }
                        break;
                    case "previous":
                        var previousPage = Controller.getPage("previous");
                        if (previousPage == null) {
                            System.out.println("No more pages.");
                        } else {
                            View.print(previousPage);
                        }
                        break;
                    default:
                        System.out.println("Unknown command");
                }
            } else {
                System.out.println("Provide access to the application");
            }
        }
    }
}
