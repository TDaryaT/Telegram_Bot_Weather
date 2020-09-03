/*<<<<<<< HEAD
package notification;
=======
package Commands;
>>>>>>> origin/master

import bot.MyWeatherTgBot;
import org.quartz.*;

<<<<<<< HEAD
public class SendBadWeather implements Job {
=======
public class NotUpdateCommand implements Job {
>>>>>>> origin/master
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
         Retrieve the bot instance */
/*
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
*/