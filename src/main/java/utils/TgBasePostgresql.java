package utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

/**
 * This class create for data processing with 2 data bases, described in the file tgbase.sql
 */
public class TgBasePostgresql {
    //TODO: переделать запросы на человеческие
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

    /**
     * @return properties for access the base
     */
    public static Properties readProperties() {
        Properties props = new Properties();
        Path myPath = Paths.get("src/main/resources/database.properties");

        try {
            LOGGER.info("read bd properties...");
            BufferedReader bf = Files.newBufferedReader(myPath,
                    StandardCharsets.UTF_8);

            props.load(bf);
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }

        return props;
    }

    /**
     * Check tg user in base users
     * @param tgUser - user of tg bot
     * @return true, if user in base users
     */
    public boolean isUserId(int tgUser) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT lat, lon FROM users " +
                     "WHERE user_id=" + tgUser);
             ResultSet rs = pst.executeQuery()) {
            LOGGER.info("Check id in base users...");

            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }

    /**
     * Add tg user in base users
     * @param tgUser - user of tg bot
     */
    public void setUserID(int tgUser) {
        String query = "INSERT INTO users(user_id) VALUES(?)";
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.info("Add in base users...");
            pst.setInt(1, tgUser);
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * add lon in base
     * @param lon - longitude of tg user
     * @param tgUser - tg user id
     */
    public void updateLonUser(double lon, int tgUser){
        String query = "UPDATE users SET lon=" + lon + " WHERE user_id=" + tgUser;
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.info("update lon in base users...");
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * add lat in base
     * @param lat - latitude of tg user
     * @param tgUser - tg user id
     */
    public void updateLatUser(double lat, int tgUser){
        String query = "UPDATE users SET lat=" + lat + " WHERE user_id=" + tgUser;
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.info("update lat in base users...");
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * @param user_id - id user in tg
     * @return latitude this user
     */
    public double getLatUser(int user_id) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT lat FROM users WHERE user_id=" + user_id);
             ResultSet rs = pst.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }

    /**
     * @param user_id - id user in tg
     * @return longitude this user
     */
    public double getLonUser(int user_id) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT lon FROM users WHERE user_id=" + user_id);
             ResultSet rs = pst.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return 0;
        }
    }
}
