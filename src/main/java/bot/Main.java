package bot;

import notification.SendBadWeather;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("Initializing API context...");
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        LOGGER.info("Configuring bot options...");
        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        MyWeatherTgBot bot = new MyWeatherTgBot(botOptions);

        try {
            telegramBotsApi.registerBot(bot);
            LOGGER.info("My weather bot is ready for work!");
        } catch (TelegramApiException e) {
            LOGGER.error("Error while initializing bot! " + e.getMessage());
        }
        /* Schedule tasks not related to updates via Quartz */
        try {
            /* Instantiate the job that will call the bot function */
            JobDetail jobSendNotification = JobBuilder.newJob(SendBadWeather.class)
                    .withIdentity("sendNotification")
                    .build();

            /* Define a trigger for the call */
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("everyMorningAt8")
                    .withSchedule(
                            CronScheduleBuilder.dailyAtHourAndMinute(8, 0)) //TODO: define your schedule
                    .build();

            /* Create a scheduler to manage triggers */
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.getContext().put("bot", bot);
            scheduler.start();
            scheduler.scheduleJob(jobSendNotification, trigger);

        } catch (SchedulerException e) {
            LOGGER.error("Error while do job! " + e.getMessage());
        }

    }
}
