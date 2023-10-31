import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

@WebServlet(name = "AlbumServlet", value = "/albums/*")
public class AlbumServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();
        Gson gson = new Gson();

        if (urlPath == null || urlPath.isEmpty()) {
            res. setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(new ResponseMsg("Missing Parameter")));
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (urlPath.length() != 2) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            ResponseMsg msg = new ResponseMsg("Wrong URL Address");
            res.getWriter().write(gson.toJson(msg));
            return;
        }

        String albumID = urlParts[1];

        try (Connection connection = setupDatabaseConnection()) {
            // Retrieve data from the database
            String sql = "SELECT artist, title, year FROM albums WHERE album_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, albumID);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Construct response JSON
                        String jsonResponse = String.format("{\"artist\": \"%s\", \"title\": \"%s\", \"year\": \"%s\"}",
                                resultSet.getString("artist"),
                                resultSet.getString("title"),
                                resultSet.getString("year"));
                        // Send response
                        res.setStatus(HttpServletResponse.SC_OK);
                        res.setContentType("application/json");
                        res.getWriter().write(jsonResponse);
                    } else {
                        // If albumID not found
                        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        res.setContentType("application/json");
                        res.getWriter().write(gson.toJson(new ResponseMsg("Album not found")));
                    }
                }
                try {
                    connection.close();
                    statement.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Handle exceptions and send error response
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.setContentType("application/json");
            res.getWriter().write(gson.toJson(new ResponseMsg("Bad Connection to Database")));
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();

        if (req.getPathInfo() != null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(new ResponseMsg("Wrong URL Address")));
        }

        // check content type to be multipart/form-data
        if (!req.getContentType().startsWith("multipart/form-data")) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new ResponseMsg("Invalid content type")));
            return;
        }

        Part imagePart = req.getPart("image");
        byte[] imageData = null;
        if (imagePart != null) {
            try (InputStream is = imagePart.getInputStream()) {
                imageData = IOUtils.toByteArray(is);
            }
        }

        String profileString = req.getParameter("profile");
        if (profileString == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new ResponseMsg("Missing Profile")));
            return;
        }
        String[] lines = profileString.split("\n");
        String artist = null;
        String title = null;
        String year = null;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("artist:")) {
                artist = line.split(":")[1].trim();
            } else if (line.startsWith("title:")) {
                title = line.split(":")[1].trim();
            } else if (line.startsWith("year:")) {
                year = line.split(":")[1].trim();
            }
        }
//        System.out.println("ProfileString: " + profileString);
//        AlbumsProfile profile = gson.fromJson(profileString, AlbumsProfile.class);
//
//        String artist = profile.getArtist();
//        String title = profile.getTitle();
//        String year = profile.getYear();

        if (artist == null || title == null || year == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new ResponseMsg("Invalid profile data")));
            return;
        }

//        AlbumProfileDao albumProfileDao = new AlbumProfileDao();
//        albumProfileDao.createAlbumProfile(artist, title, year, imageData);
        try (Connection connection = setupDatabaseConnection()){
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO albums (artist, title, year, image) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, artist);
            stmt.setString(2, title);
            stmt.setString(3, year);
            stmt.setBytes(4, new byte[]{});

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating album failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long albumID = generatedKeys.getLong(1);
                    ImageMetaData metaData = new ImageMetaData(String.valueOf(albumID), String.valueOf(imageData.length));
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().write(gson.toJson(metaData));
                } else {
                    throw new SQLException("Creating album failed, no ID obtained.");
                }
            }
            try {
                connection.close();
                stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write(gson.toJson(new ResponseMsg("Database error")));
        }
    }

    private Connection setupDatabaseConnection() {
        try {
            return DatabasePool.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
