package utils;

import com.vdurmont.emoji.EmojiParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Weather {
    private final static String API_NOW = "http://api.openweathermap.org/data/2.5/weather?";
    private final static String API_5DAYS_3H = "http://api.openweathermap.org/data/2.5//forecast?";
    private final static String API_KEY = "&appid=" + System.getenv("API_KEY");
    private static String latitude;
    private static String longitude;

    private static final Logger LOGGER = LogManager.getLogger(Weather.class);

    public Weather(double latitude, double longitude) {
        LOGGER.info("generate Weather...");
        Weather.latitude = "lat=" + latitude;
        Weather.longitude = "&lon=" + longitude;
    }

    private static String getURLNow() {
        return API_NOW + latitude + longitude + API_KEY;
    }

    private static String getURL5Days_3H() {
        return API_5DAYS_3H + latitude + longitude + API_KEY;
    }

    /**
     * @param url - url link with API content
     * @return String with API content
     */
    private static String getAPIContentForJSON(String url) {
        StringBuilder responseStrBuilder = new StringBuilder();
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            InputStream inputStream = httpResponse.getEntity().getContent();
            BufferedReader streamReader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
        } catch (IOException e) {
            LOGGER.info("Trouble with URL " + url);
        }
        return responseStrBuilder.toString();
    }

    /**
     * @return массив с информацией о погоде вида:
     * result[0] = Shuzenji (city)
     * result[1] = clear sky
     * result[2] = 281.52 (Kelvin)
     * result[3] = 278.99 (feels_like)
     * result[4] = 0.47 (seed wind)
     */
    public String[] getWeatherNow() {
        String[] weatherInfo = new String[5];
        JSONObject obj = new JSONObject(getAPIContentForJSON(getURLNow()));
        LOGGER.info("get URL ");

        if (obj.get("cod").toString().equals("401")) {
            LOGGER.debug("Invalid API key");
        } else {

            weatherInfo[0] = obj.get("name").toString();
            weatherInfo[1] = obj.getJSONArray("weather").getJSONObject(0).get("description").toString();
            weatherInfo[2] = obj.getJSONObject("main").get("temp").toString();
            weatherInfo[3] = obj.getJSONObject("main").get("feels_like").toString();
            weatherInfo[4] = obj.getJSONObject("wind").get("speed").toString();
        }
        return weatherInfo;
    }

    public String[] getWeather3H() {
        String[] weatherInfo = new String[6];
        JSONObject obj = new JSONObject(getAPIContentForJSON(getURL5Days_3H()));
        LOGGER.info("get URL ");

        weatherInfo[0] = obj.getJSONObject("city").get("name").toString();
        JSONArray list = obj.getJSONArray("list");

        double temp = Double.parseDouble(list.getJSONObject(0).getJSONObject("main").get("temp").toString());

        for (Object o : list) {
            JSONObject elem = (JSONObject) o;

            weatherInfo[1] = elem.getJSONArray("weather").getJSONObject(0).get("description").toString();
            weatherInfo[2] = elem.getJSONObject("main").get("temp").toString();
            weatherInfo[3] = elem.getJSONObject("main").get("feels_like").toString();
            weatherInfo[4] = elem.getJSONObject("wind").get("speed").toString();
            weatherInfo[5] = elem.get("dt_txt").toString();

            boolean tempChanges = Math.abs(temp - Double.parseDouble(weatherInfo[2])) > 7;
            temp = Double.parseDouble(weatherInfo[2]);
            if (hasBigChanges(weatherInfo) || tempChanges) {
                break;
            }
        }
        return weatherInfo;
    }

    /**
     * translate weather for telegram bot
     *
     * @param weather - massive with information
     * @return - String with message
     */
    public String toWrap(String[] weather) {
        StringBuilder stringBuilder = new StringBuilder();
        double temp = Double.parseDouble(weather[2]) - 273.15;
        double tempFeelsLike = Double.parseDouble(weather[3]) - 273.15;

        stringBuilder
                        .append(EmojiParser.parseToUnicode(":house: "))
                        .append("Location: ").append(weather[0]).append('\n')
                .append(EmojiParser.parseToUnicode(":white_sun_small_cloud: "))
                .append("Weather condition: ").append(weather[1]).append('\n')
                .append(EmojiParser.parseToUnicode(":thermometer: "))
                .append("Temperature: ").append(String.format(Locale.US, "%.2f", temp)).append(" C\n")
                .append(EmojiParser.parseToUnicode(":thermometer: "))
                .append("Feels like: ").append(String.format(Locale.US, "%.2f", tempFeelsLike)).append(" C\n")
                .append(EmojiParser.parseToUnicode(":cloud_tornado: "))
                .append("Speed wind: ").append(weather[4]).append(" meter/sec\n");

        return stringBuilder.toString();
    }

    /**
     * @param weatherInfo массив с информацией о погоде вида:
     *                    result[0] = Shuzenji (city)
     *                    result[1] = clear sky
     *                    result[2] = 281.52 (Kelvin)
     *                    result[3] = 278.99 (feels_like)
     *                    result[4] = 0.47 (seed wind)
     *                    result[5] = date
     */
    public static boolean hasBigChanges(String[] weatherInfo) {
        boolean description = weatherInfo[1].contains("shower rain") ||
                weatherInfo[1].contains("rain") ||
                weatherInfo[1].contains("thunderstorm") ||
                weatherInfo[1].contains("snow");

        boolean speed_wind = Double.parseDouble(weatherInfo[4]) > 5.;
        return description || speed_wind;
    }
}
