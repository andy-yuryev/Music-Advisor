# Music Advisor

A simple CLI application that uses Spotify API to provide music recommendations.

## Running the application

- Building app
```
./gradlew build
```

- Running app
```
java -jar build/libs/*.jar
```

An output is separated by pages. The number of entries on a page is 20 by default, 
but can be amended using the command line argument -page n, where n is a number of entries.

## Commands

To perform any actions other than authorization require the user has to be authorized with Spotify.

- `auth` Provides a link to the Spotify authorization page and waits until user confirms or rejects the authorization.
- `new` Shows list of new albums with artists and links on Spotify.
- `featured` Shows list of Spotify featured playlists with their links fetched from API.
- `categories` Shows list of all available categories on Spotify (just their names).
- `playlists C_NAME` where C_NAME - name of category. List contains playlists of this category and their links on Spotify.
- `next` Next page.
- `previous` Previous page.
