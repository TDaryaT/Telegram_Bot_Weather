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
    public boolean isUserId(User tgUser) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT * FROM users " +
                     "WHERE user_id=" + tgUser.getId());
             ResultSet rs = pst.executeQuery()) {
            LOGGER.info("Check id " + tgUser.getUserName() + " in base users...");
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage() + " from " + tgUser.getUserName());
            return false;
        }
    }

    public boolean isUserCity(User tgUser, String city) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT * FROM users " +
                     "WHERE user_id=" + tgUser.getId() + " AND " +
                     "city = '" + city + '\'');
             ResultSet rs = pst.executeQuery()) {
            LOGGER.info("Check city " + tgUser.getUserName() + " in base users...");
            return rs.next();
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage() + " from " + tgUser.getUserName());
            return false;
        }
    }

    /**
     * Add tg user in base users
     * @param tgUser - user of tg bot
     */
    public void setUserID(User tgUser) {
        String query = "INSERT INTO users(user_id) VALUES(?)";
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.info("Add " + tgUser.getUserName() + " in base users...");
            pst.setInt(1, tgUser.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage() + " from " + tgUser.getUserName());
        }
    }

    /**
     * add new city in base users
     * @param tgUser - user tg
     * @param city   - new city
     */
    public void updateUserCity(User tgUser, String city) {
        String query = "UPDATE users SET city='" + city + "' WHERE user_id=" + tgUser.getId();
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.info("update city in base users...");
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
    public void updateLonUser(double lon, User tgUser){
        String query = "UPDATE users SET lon=" + lon + " WHERE user_id=" + tgUser.getId();
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
    public void updateLatUser(double lat, User tgUser){
        String query = "UPDATE users SET lat=" + lat + " WHERE user_id=" + tgUser.getId();
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {

            LOGGER.info("update lat in base users...");
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * get city in base users
     * @param user_id - user id in tg
     * @return - String city
     */
    public String getCityUser(int user_id) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT city FROM users WHERE user_id=" + user_id);
             ResultSet rs = pst.executeQuery()) {
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return null;
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

    /**
     * Проверка наличия данных о погоде в городе
     * @param city - искомый город
     * @return true, если есть
     */
    public boolean isCityWeather(String city) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT city FROM weather WHERE city='" + city + '\'');
             ResultSet rs = pst.executeQuery()) {

            LOGGER.info("Check city in weather base...");
            return rs.next();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    /**
     * Есть ли погодная строка с малыми различиями
     * @param weather - параметры новой погоды
     * @return true, если уже есть такая погода и он не сильно отличается новой
     */
    public boolean isWeather(String[] weather) {
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT * FROM weather " +
                     "WHERE city= '" + weather[0] + '\'');
             ResultSet rs = pst.executeQuery()) {

            LOGGER.info("Check info in weather base...");
            if (rs.next()) {
                return ((rs.getString(2).equals(weather[1])) &&
                        (Math.abs(rs.getDouble(3) - Double.parseDouble(weather[2])) < 1.) &&
                        (Math.abs(rs.getDouble(4) - Double.parseDouble(weather[3])) < 1.) &&
                        (Math.abs(rs.getDouble(5) - Double.parseDouble(weather[4])) < 1.) &&
                        (rs.getDate(6).equals(new Date(System.currentTimeMillis()))));
            }
            return false;
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
            return false;
        }
    }

    public void setWeather(String[] weather) {
        String query = "SELECT * FROM weather WHERE city='" + weather[0] + '\'';
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                updateWeather(weather);
            } else {
                addNewWeather(weather);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * update weather in weather base
     *
     * @param weather - String array like:
     *                * result[0] = Shuzenji (city)
     *                * result[1] = clear sky
     *                * result[2] = 281.52 (Kelvin)
     *                * result[3] = 278.99 (feels_like)
     *                * result[4] = 0.47 (seed wind)
     *                * result[5] = date
     */
    public void updateWeather(String[] weather) {
        String query = "UPDATE weather SET " +
                "condition = '" + weather[1] +
                "', temp = " + weather[2] +
                ", temp_like = " + weather[3] +
                ", wind_speed = " + weather[4] +
                ", date = '" + new Date(System.currentTimeMillis()) +
                "' WHERE city = '" + weather[0] + '\'';
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement(query)) {
            LOGGER.info("Update info in weather base...");
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }

    }

    /**
     * add new weather in weather base
     *
     * @param weather - String array like:
     *                * result[0] = Shuzenji (city)
     *                * result[1] = clear sky
     *                * result[2] = 281.52 (Kelvin)
     *                * result[3] = 278.99 (feels_like)
     *                * result[4] = 0.47 (seed wind)
     *                * result[5] = date
     */
    public void addNewWeather(String[] weather) {
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

            LOGGER.info("Add new info in weather base...");
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public String[] getWeather(int user_id) {
        String city = getCityUser(user_id);
        String[] weather =  new String[6];
        try (Connection con = DriverManager.getConnection(url, user, passwd);
             PreparedStatement pst = con.prepareStatement("SELECT * FROM weather WHERE city='" + city +'\'');
             ResultSet rs = pst.executeQuery()) {
            rs.next();
            weather[0] = rs.getString(1);
            weather[1] = rs.getString(2);
            weather[2] = String.valueOf(rs.getDouble(3));
            weather[3] = String.valueOf(rs.getDouble(4));
            weather[4] = String.valueOf(rs.getDouble(5));
            weather[5] = String.valueOf(rs.getDate(6));
            return weather;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }
}
