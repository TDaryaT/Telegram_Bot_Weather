package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

public class TgBasePostgresql {
    private static final Logger LOGGER = LogManager.getLogger(TgBasePostgresql.class);
    private final String url;
    private final String user;
    private final String passwd;

    public TgBasePostgresql() {
        LOGGER.info("Connecting bd...");
        Properties props = readProperties();

        url = props.getProperty("db.url");
        user = props.getProperty("db.user");
        passwd = System.getenv("bd_password");
    }

    public static Properties readProperties() {

        Properties props = new Properties();
        Path myPath = Paths.get("src/main/resources/database.properties");

        try {
            BufferedReader bf = Files.newBufferedReader(myPath,
                    StandardCharsets.UTF_8);

            props.load(bf);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }

        return props;
    }

    public boolean isUserBd(User tgUser) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT user_id FROM users");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                if (rs.getInt(1) == tgUser.getId()) {
                    return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage() + " from " + tgUser.getUserName());
            return false;
        }
    }

    public void addUserID(User tgUser) {
        String query = "INSERT INTO users(user_id) VALUES(?)";
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, tgUser.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage() + " from " + tgUser.getUserName());

        }
    }

    public void addUserCity(String city) {
        String query = "INSERT INTO users(city) VALUES(?)";
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, city);
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());

        }
    }

    public boolean isUserCity(String city) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT city FROM users");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                if (rs.getString(1).equals(city)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }
    /**
     * @param weather - массив с информацией о погоде вида:
     *                * result[0] = Shuzenji (city)
     *                * result[1] = clear sky
     *                * result[2] = 281.52 (Kelvin)
     *                * result[3] = 278.99 (feels_like)
     *                * result[4] = 0.47 (seed wind)
     */
    public void addWeather(String[] weather) {
        String query = "INSERT INTO " +
                "weather(city, condition, temp, temp_like, wind_speed, date) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, weather[0]);
            pst.setString(2, weather[1]);
            pst.setDouble(3, Double.parseDouble(weather[2]));
            pst.setDouble(4, Double.parseDouble(weather[3]));
            pst.setDouble(5, Double.parseDouble(weather[4]));

            Date date = new Date(System.currentTimeMillis());
            pst.setDate(6, date);

            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public boolean isWeather(String[] weather) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT * FROM weather");
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                if ((rs.getString(1).equals(weather[0])) &&
                        (rs.getString(2).equals(weather[1])) &&
                        (Math.abs(rs.getDouble(3) - Double.parseDouble(weather[2])) < 1.) &&
                        (Math.abs(rs.getDouble(4) - Double.parseDouble(weather[3])) < 1.) &&
                        (Math.abs(rs.getDouble(5) - Double.parseDouble(weather[4])) < 1.)){
                    return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }
}
