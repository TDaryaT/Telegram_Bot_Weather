package notification;

import bot.MyWeatherTgBot;
import org.quartz.*;

public class SendBadWeather implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        /* Retrieve the bot instance */
        SchedulerContext schedulerContext = null;
        try {
            schedulerContext = jobExecutionContext.getScheduler().getContext();
        } catch (SchedulerException e1) {
            e1.printStackTrace();
        }
        MyWeatherTgBot bot = (MyWeatherTgBot) schedulerContext.get("bot");

        bot.sendNotification();
    }
}
