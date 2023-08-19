import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    protected static JDA jda;
    public static void main(String[] args) {
        final String TOKEN="MTEyODA4MjI2ODUyNzgwODYwNA.GXKvht.242cBMSJjSxMl9iyzpvK068u8lz9Gvp5e0cE3E";
        JDABuilder jdaBuilder= JDABuilder.createDefault(TOKEN);
        jda=jdaBuilder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES).addEventListeners(new ReadyListener(),
                new ProductivityDiscordBot()).build();
        jda.upsertCommand("!addTask", "This is a command that allows you to add a task that you must complete to your list of tasks").setGuildOnly(true).queue();
        jda.upsertCommand("!removeTask", "This is a command that allows you to remove a task from your list of tasks").setGuildOnly(true).queue();
        jda.upsertCommand("!getReport", "This is a command that gives a report of all of your task i.e name of task, type of task, priority level, deadline, respective substasks and their details, progress of the task, and also tracks when you started and ended the task").setGuildOnly(true).queue();
        jda.upsertCommand("!getCompletedTasks", "This is a command that would return all of the tasks whose progress is marked COMPLETE for the user").setGuildOnly(true).queue();
        jda.upsertCommand("!getPartialCompletedTasks", "This is a command that would return all the tasks whose progress is marked PARTIALLY COMPLETED. Note that once you start a task, it will be automatically marked as PARTIALLY COMPLETED").setGuildOnly(true).queue();
        jda.upsertCommand("!getIncompleteTasks", "This is a command that would return all the tasks whose progress is marked INCOMPLETE for the user").setGuildOnly(true).queue();
        jda.upsertCommand("!getOverdueTasks", "This is a command that would return all the tasks whose progress is marked OVERDUE").setGuildOnly(true).queue();
        jda.upsertCommand("!numberOfTasks", "This is a command that would return the number of tasks the user has").setGuildOnly(true).queue();
        jda.upsertCommand("!removeTask", "This is a command that would remove a specified task for a user").setGuildOnly(true).queue();
        jda.upsertCommand("!removeSubTask", "This is a command that would remove a specified subtask for a task for a user").setGuildOnly(true).queue();
        jda.upsertCommand("!clearAll", "This is a command that clears all the tasks that a user has").setGuildOnly(true).queue();
        jda.upsertCommand("!focusMode", "This is a command that enables focus mode and mutes everyone in the guild + voice channel").setGuildOnly(true).queue();
        jda.upsertCommand("!disableFM", "This is a command that disables focus mode that a user enabled").setGuildOnly(true).queue();
        jda.upsertCommand("!createChannel", "This is a collaborative feature where a user can create a private texting channel and add their group-mates and work on their shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!finishedTask", "This is a command that marks the specified task's progress as FINISHED").setGuildOnly(true).queue();
        jda.upsertCommand("!finishedSubTask", "This is a command that marks the specified subtask of a task as FINISHED").setGuildOnly(true).queue();
        jda.upsertCommand("!startedTask", "This is a command that marks down the start date of a user starting a task").setGuildOnly(true).queue();
        jda.upsertCommand("!startedSubTask", "This is a command that marks down the start date of a subtask of a task").setGuildOnly(true).queue();
        jda.upsertCommand("!addDescriptionTask", "This is a command that enables the user to add a description to their task").setGuildOnly(true).queue();
        jda.upsertCommand("!addDescriptionSubTask", "This is a command that enables the user to add a description to a specified subtask of a task").setGuildOnly(true).queue();
        jda.upsertCommand("!GaddTask", "Collaborative feature that allows group-mates to add tasks toward their shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GaddSubTask", "Collaborative feature that allows group-mates to add subtasks for specific tasks toward their shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GgetReport", "Collaborative feature that returns a report detailing the list of tasks for each user").setGuildOnly(true).queue();
        jda.upsertCommand("!GgetCompletedTasks", "Collaborative feature that returns all tasks marked COMPLETED for the group-mates ").setGuildOnly(true).queue();
        jda.upsertCommand("!GgetIncompleteTasks", "Collaborative feature that returns all tasks marked INCOMPLETE for the group-mates").setGuildOnly(true).queue();
        jda.upsertCommand("!GgetOverdueTasks", "Collaborative feature that returns all tasks marked OVERDUE for the group-mates").setGuildOnly(true).queue();
        jda.upsertCommand("!GgetPartialCompletedTasks", "Collaborative feature that returns all tasks marked PARTIALLY COMPLETED for the group-mates").setGuildOnly(true).queue();
        jda.upsertCommand("!GaddDescriptionTask", "Collaborative feature that allows you to add a description for a task").setGuildOnly(true).queue();
        jda.upsertCommand("!GaddDescriptionSubTask", "Collaborative feature that allows you to add a description for a subtask").setGuildOnly(true).queue();
        jda.upsertCommand("!GremoveTask", "Collaborative feature that allows you to remove a task toward the shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GremoveSubTask", "Collaborative feature that allows you to remove a subtask for a task toward the shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GstartedTask", "Collaborative feature that allows you to mark a task as STARTED for the shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GstartedSubTask", "Collaborative feature that allows you to mark a subtask as STARTED for the shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GfinishedTask", "Collaborative feature that allows you to mark a task as FINISHED toward the shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GfinishedSubTask", "Collaborative feature that allows you to mark a subtask of a task as FINISHED").setGuildOnly(true).queue();
        jda.upsertCommand("!GclearAll", "Collaborative feature that clears all the tasks toward the shared project").setGuildOnly(true).queue();
        jda.upsertCommand("!GnumberOfTasks", "Collaborative feature that returns the number of tasks each user has in the group").setGuildOnly(true).queue();
        jda.upsertCommand("!timer", "Sets a timer that will alert the user once it is over").setGuildOnly(true).queue();
        jda.upsertCommand("!Gremind", "Reminder System that allows users to be reminded of any tasks due in any amount of days").setGuildOnly(true).queue();


    }
    public static JDA getJDA() {
        return jda;
    }
}
