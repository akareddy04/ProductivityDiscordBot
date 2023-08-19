import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ProductivityDiscordBot extends ListenerAdapter {
    private final HashMap<String, PriorityQueue<Tasks>> userTasks = new HashMap<>();
    private final HashMap<String, HashMap<String, PriorityQueue<Tasks>>> groupWork = new HashMap<>();
    private boolean focusModeEnabled = false;
    private final HashSet<String> mutedUsers = new HashSet<>();

    private static class TaskComparator implements Comparator<Tasks> {

        @Override
        public int compare(Tasks firstTask, Tasks secondTask) {
            int deadlineComparison = firstTask.getDeadline().compareTo(secondTask.getDeadline());
            if (deadlineComparison != 0) {
                return deadlineComparison;
            }
            return Integer.compare(secondTask.getPriorityLevel(), firstTask.getPriorityLevel());
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        if (messageContent.startsWith("!addTask")) {
            addUserTask(event);
        } else if (messageContent.startsWith("!addSubTask")) {
            addUserSubTask(event);
        } else if (messageContent.startsWith("!getReport")) {
            sendReport(event, false);
        } else if (messageContent.startsWith("!getCompletedTasks")) {
            getTasksBasedOnProgress(event, "COMPLETED", false);
        } else if (messageContent.startsWith("!getPartialCompletedTasks")) {
            getTasksBasedOnProgress(event, "PARTIALLY COMPLETED", false);
        } else if (messageContent.startsWith("!getIncompleteTasks")) {
            getTasksBasedOnProgress(event, "INCOMPLETE", false);
        } else if (messageContent.startsWith("!getOverdueTasks")) {
            getTasksBasedOnProgress(event, "OVERDUE", false);
        } else if (messageContent.startsWith(("!numberOfTasks"))) {
            numberOfTasks(event, false);
        } else if (messageContent.startsWith("!removeTask")) {
            removeUserTask(event);
        } else if (messageContent.startsWith("!removeSubTask")) {
            removeUserSubTask(event);
        } else if (messageContent.startsWith("!clearAll")) {
            clearAll(event, false);
        } else if (messageContent.startsWith("!focusMode")) {
            muteNotificationsSetting(event);
        } else if (messageContent.startsWith("!disableFM")) {
            disableFocusMode(event);
        } else if (messageContent.startsWith("!createChannel")) {
            createTextChannel(event);
        } else if (messageContent.startsWith("!finishedTask")) {
            finishedUserTask(event);
        } else if (messageContent.startsWith("!finishedSubTask")) {
            finishedUserSubTask(event);
        } else if (messageContent.startsWith("!startedTask")) {
            startedTaskUser(event);
        } else if (messageContent.startsWith("!startedSubTask")) {
            startedSubTaskUser(event);
        } else if (messageContent.startsWith("!addDescriptionTask")) {
            addTaskDescriptionUser(event);
        } else if (messageContent.startsWith("!addDescriptionSubTask")) {
            addSubTaskDescriptionUser(event);
        } else if (messageContent.startsWith("!GaddTask")) {
            addGroupTask(event);
        } else if (messageContent.startsWith("!GaddSubTask")) {
            addGroupSubTask(event);
        } else if (messageContent.startsWith("!GgetReport")) {
            sendReportGroup(event);
        } else if (messageContent.startsWith("!GgetCompletedTasks")) {
            getTasksG(event, "COMPLETED");
        } else if (messageContent.startsWith("!GgetIncompleteTasks")) {
            getTasksG(event, "INCOMPLETE");
        } else if (messageContent.startsWith("!GgetOverdueTasks")) {
            getTasksG(event, "OVERDUE");
        } else if (messageContent.startsWith("!GgetPartialCompletedTasks")) {
            getTasksG(event, "PARTIALLY COMPLETED");
        } else if (messageContent.startsWith("!GaddDescriptionTask")) {
            addTaskDescriptionGroup(event);
        } else if (messageContent.startsWith("!GaddDescriptionSubTask")) {
            addSubTaskDescriptionGroup(event);
        } else if (messageContent.startsWith("!GremoveTask")) {
            removeGroupTask(event);
        } else if (messageContent.startsWith("!GremoveSubTask")) {
            removeGroupSubTask(event);
        } else if (messageContent.startsWith("!GstartedTask")) {
            startedTaskGroup(event);
        } else if (messageContent.startsWith("!GstartedSubTask")) {
            startedSubTaskGroup(event);
        } else if (messageContent.startsWith("!GfinishedTask")) {
            finishedGroupTask(event);
        } else if (messageContent.startsWith("!GfinishedSubTask")) {
            finishedGroupSubTask(event);
        } else if (messageContent.startsWith("!GclearAll")) {
            clearAll(event, true);
        } else if (messageContent.startsWith("!GnumberOfTasks")) {
            numberOfTasks(event, true);
        } else if (messageContent.startsWith("!timer")) {
            startTimer(event);
        } else if(messageContent.startsWith("!remind")) {
            checkAndSendReminders(event, false, "");
        } else if(messageContent.startsWith("!Gremind")) {
            checkAndSendReminders(event, true, event.getChannel().getName());
        }
    }


    private void getTasksBasedOnProgress(MessageReceivedEvent event, String progress, boolean isGroupWork) {
        StringBuilder report = new StringBuilder();
        if (isGroupWork) {
            String groupName = event.getChannel().getName();
            HashMap<String, PriorityQueue<Tasks>> tasks = this.groupWork.get(groupName);
            if (tasks == null || tasks.isEmpty()) {
                event.getChannel().sendMessage("There are no tasks to generate a report").queue();
                return;
            }
            for (Map.Entry<String, PriorityQueue<Tasks>> userTasks : tasks.entrySet()) {
                String nameOfUser = userTasks.getKey();
                ArrayList<Tasks> values = new ArrayList<>(userTasks.getValue());
                if (!values.isEmpty()) {
                    StringBuilder detail = new StringBuilder();
                    report.append("List of tasks for ").append(nameOfUser).append("from group ").append(groupName).append(" that have the progress ").append(progress).append(": ").append("\n");
                    for (Tasks task : values) {
                        String status = task.getProgressOfTask().getStatus();
                        if (status.equalsIgnoreCase(progress)) {
                            detail.append(task.getReport()).append("\n");
                        }
                    }
                    if (detail.length() == 0) {
                        report.append("There are no tasks that have the progress ").append(progress);
                    } else {
                        report.append(detail);
                    }
                    event.getChannel().sendMessage(report).queue();
                }
            }
        } else {
            String nameOfUser = getNameOfUser(event);
            if (!this.userTasks.containsKey(nameOfUser)) {
                event.getChannel().sendMessage("No tasks have been assigned for user " + nameOfUser).queue();
                return;
            }
            ArrayList<Tasks> allTasks = getTasksForUser(event, false);
            for (Tasks task : allTasks) {
                String status = task.getProgressOfTask().getStatus();
                if (status.equalsIgnoreCase(progress)) {
                    report.append(task.getReport());
                }
            }
            if (report.length() == 0) {
                event.getChannel().sendMessage("No tasks for user " + nameOfUser + " have the progress " + progress).queue();
                return;
            }
            event.getChannel().sendMessage("The list of requested tasks that are " + progress.toUpperCase() + " can now be seen: " + "\n" + report).queue();

        }

    }

    private void addTaskCommand(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String nameOfUser = getNameOfUser(event);
        String nameOfTask = message[1];
        String typeOfTask = message[2];
        int priorityLevel = Integer.parseInt(message[3]);
        String deadlineOfTask = message[4];
        Tasks task = new Tasks(nameOfTask, typeOfTask, priorityLevel, deadlineOfTask);
        LocalDate date = LocalDate.parse(deadlineOfTask, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        if (date.isBefore(LocalDate.now())) {
            event.getChannel().sendMessage("Task was not added for user " + nameOfUser + " as the deadline has passed").queue();
            return;
        }
        if (tasks != null) {
            if (findTask(event, nameOfTask, tasks) != null) {
                event.getChannel().sendMessage("The task has already been added for user: " + nameOfUser).queue();
                return;
            }
        } else {
            tasks = new HashMap<>();
        }
        PriorityQueue<Tasks> setOfTasks = tasks.getOrDefault(nameOfUser, new PriorityQueue<>(new TaskComparator()));
        boolean added = setOfTasks.add(task);
        if (!isGroupWork) {
            this.userTasks.put(nameOfUser, setOfTasks);
        } else {
            tasks.put(nameOfUser, setOfTasks);
            this.groupWork.put(event.getChannel().getName(), tasks);
        }
        event.getChannel().sendMessage("Task was successfully added for user " + nameOfUser + ". Keep up the productivity grind!").queue();

    }


    private void sendReport(MessageReceivedEvent event, boolean isGroupWork) {
        StringBuilder report = new StringBuilder();
        if (isGroupWork) {
            HashMap<String, PriorityQueue<Tasks>> tasks = this.groupWork.get(event.getChannel().getName());
            if (tasks == null || tasks.isEmpty()) {
                event.getChannel().sendMessage("There are no tasks to generate a report").queue();
                return;
            }
            for (Map.Entry<String, PriorityQueue<Tasks>> userTasks : tasks.entrySet()) {
                String nameOfUser = userTasks.getKey();
                ArrayList<Tasks> values = new ArrayList<>(userTasks.getValue());
                if (!values.isEmpty()) {
                    report.append("Report for user ").append(nameOfUser).append(" for group ").append(event.getChannel().getName()).append(": ").append("\n");
                    for (Tasks task : values) {
                        report.append(task.getReport()).append("\n").append("\n");
                    }
                }
            }
        } else {
            String nameOfUser = getNameOfUser(event);
            ArrayList<Tasks> tasks = getTasksForUser(event, false);
            if (tasks.isEmpty()) {
                event.getChannel().sendMessage("There are no tasks for user " + nameOfUser).queue();
                return;
            }
            for (Tasks task : tasks) {
                report.append(task.getReport()).append("\n").append("\n");
            }

        }
        String[] taskReports = report.toString().split("\n");
        for (String taskReport : taskReports) {
            if (!taskReport.trim().isEmpty()) {
                event.getChannel().sendMessage(taskReport+"\n").queue();
            }
        }


    }

    private void addSubTaskCommand(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String nameOfUser = getNameOfUser(event);
        String nameOfTask = message[1];
        String nameOfSubTask = message[2];
        int priorityLevel = Integer.parseInt(message[3]);
        String deadline = message[4];
        LocalDate date = LocalDate.parse(deadline, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        if (findTask(event, nameOfTask, tasks) == null) {
            event.getChannel().sendMessage("User " + nameOfUser + " does not have the task " + nameOfTask + " registered." +
                    "Please try again and add the task first before adding the necessary subtasks").queue();
            return;
        }
        if (findSubTask(event, nameOfTask, nameOfSubTask, tasks) != null) {
            event.getChannel().sendMessage("The subtask for user " + nameOfUser + " is already present").queue();
            return;
        }
        if (date.isAfter(getDeadlineForTask(event, nameOfTask, isGroupWork))) {
            event.getChannel().sendMessage("Subtask was not added for user " + nameOfUser + " as the deadline is after the deadline for the main task").queue();
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            event.getChannel().sendMessage("Subtask was not added for user " + nameOfUser + " as the deadline has passed").queue();
            return;
        }
        Tasks task = findTask(event, nameOfTask, tasks);
        assert task != null;
        task.addSubTask(new Tasks.TreeNode(nameOfSubTask, priorityLevel, deadline, ""));

        event.getChannel().sendMessage("Subtask was added successfully to the task " + nameOfTask + " for user " + nameOfUser + ".").queue();
    }

    private Tasks findTask(MessageReceivedEvent event, String nameOfTask, HashMap<String, PriorityQueue<Tasks>> tasks) {
        if (!tasks.containsKey(getNameOfUser(event))) {
            return null;
        }
        PriorityQueue<Tasks> setOfTasks = tasks.get(getNameOfUser(event));
        for (Tasks task : setOfTasks) {
            if (task.getName().equalsIgnoreCase(nameOfTask)) {
                return task;
            }
        }
        return null;

    }

    private String getNameOfUser(MessageReceivedEvent event) {
        return event.getAuthor().getName();
    }

    private ArrayList<Tasks> getTasksForUser(MessageReceivedEvent event, boolean isGroupWork) {
        PriorityQueue<Tasks> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()).get(getNameOfUser(event)) : this.userTasks.get(getNameOfUser(event));
        return new ArrayList<>(tasks);
    }

    private void numberOfTasks(MessageReceivedEvent event, boolean isGroupWork) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        if (isGroupWork) {
            List<Member> members = event.getMessage().getMentions().getMembers();
            for (Member member : members) {
                String name = member.getUser().getName();
                if (tasks.get(name).isEmpty()) {
                    event.getChannel().sendMessage("There are no tasks for user " + name).queue();
                } else {
                    event.getChannel().sendMessage("Number of tasks for user " + name + " is: " + tasks.get(name).size()).queue();
                }

            }
        } else {
            String user = getNameOfUser(event);
            if (!tasks.containsKey(user)) {
                event.getChannel().sendMessage("There are no tasks for user " + user).queue();
            } else {
                int numberOfTasks = tasks.get(user).size();
                event.getChannel().sendMessage("Number of tasks for user " + user + " is " + numberOfTasks).queue();
            }
        }


    }

    private LocalDate getDeadlineForTask(MessageReceivedEvent event, String nameOfTask, boolean isGroupWork) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        Tasks task = findTask(event, nameOfTask, tasks);
        assert task != null;
        return LocalDate.parse(task.getFormmatedDeadlineTask(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    private Tasks.TreeNode findSubTask(MessageReceivedEvent event, String nameOfTask, String nameOfSubTask, HashMap<String, PriorityQueue<Tasks>> tasks) {
        Tasks.TreeNode desiredSubTask = null;
        Tasks task = findTask(event, nameOfTask, tasks);
        if (task != null) {
            for (Tasks.TreeNode subTask : task.getSubTasks()) {
                if (subTask.getNameOfSubTask().equalsIgnoreCase(nameOfSubTask)) {
                    desiredSubTask = subTask;
                }
            }
        } else {
            return null;
        }
        return desiredSubTask;

    }

    private void removeTask(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String nameOfUser = getNameOfUser(event);
        String nameOfTask = message[1];
        if (findTask(event, nameOfTask, tasks) == null) {
            event.getChannel().sendMessage("The task " + nameOfTask + " is not in the database").queue();
            return;
        }
        boolean remove = tasks.get(nameOfUser).remove(findTask(event, nameOfTask, tasks));
        event.getChannel().sendMessage("Task has been removed for user " + nameOfUser).queue();

    }

    private void removeSubTask(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String nameOfUser = getNameOfUser(event);
        String nameOfTask = message[1], nameOfSubTask = message[2];
        Tasks.TreeNode subTask = findSubTask(event, nameOfTask, nameOfSubTask, tasks);
        if (subTask == null) {
            event.getChannel().sendMessage("Subtask is not registered for the task " + nameOfTask + " for user " + nameOfUser).queue();
            return;
        }
        boolean removed = Objects.requireNonNull(findTask(event, nameOfTask, tasks)).removeSubTask(subTask);
        if (removed) {
            event.getChannel().sendMessage("Subtask: " + nameOfSubTask + " was successfully removed").queue();
        } else {
            event.getChannel().sendMessage("Subtask: " + nameOfSubTask + " was unable to be removed").queue();
        }
    }

    private void clearAll(MessageReceivedEvent event, boolean isGroupWork) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (!isGroupWork) {
            if (message.length != 1) {
                event.getChannel().sendMessage("Invalid format. Correct format is !clearAll").queue();
                return;
            }
            String nameOfUser = getNameOfUser(event);
            if (!this.userTasks.containsKey(nameOfUser)) {
                event.getChannel().sendMessage("User " + nameOfUser + " does not have any tasks to clear").queue();
                return;
            }
            this.userTasks.get(nameOfUser).clear();
            event.getChannel().sendMessage("All of the tasks for user " + nameOfUser + " have been removed").queue();
        } else {
            if (message.length != 1) {
                event.getChannel().sendMessage("Invalid format. Correct format is !clearAllG").queue();
                return;
            }
            if (this.groupWork.get(event.getChannel().getName()).isEmpty()) {
                event.getChannel().sendMessage("There are no tasks to be cleared for the group " + event.getChannel().getName()).queue();
                return;
            }
            this.groupWork.get(event.getChannel().getName()).clear();
            event.getChannel().sendMessage("All of the tasks for the group " + event.getChannel().getName() + " have been cleared").queue();
        }
    }


    private void muteNotificationsSetting(MessageReceivedEvent event) {
        if (!focusModeEnabled) {
            focusModeEnabled = true;
            for (Guild guild : Main.getJDA().getGuilds()) {
                for (Member member : guild.getMembers()) {
                    if (!member.getUser().isBot()) {
                        mutedUsers.add(member.getId());
                        try {
                            guild.mute(member, true).queue();
                        } catch (Exception e) {
                            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                                privateChannel.sendMessage("You can only mute members if they are in a voice channel").queue();
                            });
                        }

                    }
                }
            }
            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Focus mode is now enabled").queue();
            });

        } else {
            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Focus mode is already enabled").queue();
            });
        }


    }

    private void disableFocusMode(MessageReceivedEvent event) {
        if (!focusModeEnabled) {
            event.getAuthor().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Focus mode is already disabled").queue();
                return;
            });
        }
        focusModeEnabled = false;
        for (Guild guild : Main.getJDA().getGuilds()) {
            for (String user : mutedUsers) {
                Member member = guild.getMemberById(user);
                if (member != null) {
                    guild.mute(member, false).queue();
                    mutedUsers.remove(user);
                }
            }
        }
        event.getAuthor().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Focus mode is now disabled ").queue();
        });

    }

    private void createTextChannel(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        String[] message = messageContent.split(" ");
        if (message.length < 2) {
            event.getChannel().sendMessage("Error occurred. Next time, please add the name of the text channel you wish " +
                    "to create").queue();
            return;
        }
        String channelName = message[1];

        try {
            event.getGuild().createTextChannel(channelName).queue(
                    channel -> {
                        event.getChannel().sendMessage("Text channel '" + channelName + "' created.").queue();
                        List<Member> members = event.getMessage().getMentions().getMembers();
                        for (Member member : members) {
                            channel.upsertPermissionOverride(member).setAllowed(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND).queue();
                        }
                        channel.upsertPermissionOverride(event.getGuild().getPublicRole()).setDenied(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND).queue();
                        channel.upsertPermissionOverride(event.getGuild().getSelfMember())
                                .setAllowed(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                                .queue();
                    },
                    error -> event.getChannel().sendMessage("Error creating text channel: " + error.getMessage()).queue()
            );
        } catch (Exception e) {
            event.getChannel().sendMessage("An error occurred: " + e.getMessage()).queue();
        }
    }

    private void finishedTask(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String user = getNameOfUser(event);
        Tasks task = findTask(event, message[1], tasks);
        if (task == null) {
            event.getChannel().sendMessage("The task does not exist. ").queue();
            return;
        }
        task.finishedTask();
        for (Tasks.TreeNode subtask : task.getSubTasks()) {
            subtask.completedSubTask();
        }
        event.getChannel().sendMessage("The progress of the task " + message[1] + " has been set to COMPLETE for user " + user).queue();

    }

    private void finishedSubTask(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String user = getNameOfUser(event);
        if (!tasks.get(user).contains(findTask(event, message[1], tasks))) {
            event.getChannel().sendMessage("The name of the overarching task for the subtask does not exist for user " + user).queue();
            return;
        }
        Tasks.TreeNode subtask = findSubTask(event, message[1], message[2], tasks);
        if (subtask == null) {
            event.getChannel().sendMessage("The subtask does not exist for the task " + message[1] + " for user " + user).queue();
            return;
        }
        subtask.completedSubTask();
        event.getChannel().sendMessage("The progress of the subtask " + message[2] + " for the task " + message[1] + " for user " + user + " has been set to COMPLETE").queue();

    }

    private void startedTask(MessageReceivedEvent event, boolean isGroupWork, String groupName, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(groupName) : this.userTasks;
        String user = getNameOfUser(event);
        Tasks task = findTask(event, message[1], tasks);
        if (task == null) {
            event.getChannel().sendMessage("The task does not exist. ").queue();
            return;
        }
        if (task.getProgressOfTask().getStartDate() != null) {
            event.getChannel().sendMessage("A start date has already been recorded for the task " + message[1] + " for user " + user).queue();
            return;

        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = format.format(new Date());
        task.getProgressOfTask().setNewStartDate(currentDate);
        task.getProgressOfTask().halfCompletedTask();
        event.getChannel().sendMessage("Start date for the task " + message[1] + " has successfully been recorded for user " + user).queue();


    }

    private void startedSubTask(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String user = getNameOfUser(event);
        Tasks task = findTask(event, message[1], tasks);
        if (task == null) {
            event.getChannel().sendMessage("The task does not exist. ").queue();
            return;
        }
        Tasks.TreeNode subtask = findSubTask(event, message[1], message[2], tasks);
        if (subtask == null) {
            event.getChannel().sendMessage("The subtask does not exist for the task " + message[1] + " for user " + user).queue();
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = format.format(new Date());
        subtask.getProgressOfSubTasks().setNewStartDate(currentDate);
        subtask.getProgressOfSubTasks().halfCompletedTask();
        event.getChannel().sendMessage("Start date for the subtask " + message[2] + " has successfully been recorded for the task " + message[1] + " for user " + user).queue();
    }

    private void addDescriptionTask(MessageReceivedEvent event, boolean isGroupWork, String groupName, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(groupName) : this.userTasks;
        String user = getNameOfUser(event);
        Tasks task = findTask(event, message[1], tasks);
        if (task == null) {
            event.getChannel().sendMessage("The task does not exist. ").queue();
            return;
        }
        StringBuilder description = new StringBuilder();
        if (isGroupWork) {
            for (int i = 3; i < message.length - 1; i++) {
                description.append(message[i]).append(" ");
            }
        } else {
            for (int i = 3; i < message.length; i++) {
                description.append(message[i]).append(" ");
            }
        }
        task.setDescription(description.toString());
        event.getChannel().sendMessage("Description has successfully been added for task " + message[1] + " for user " + user).queue();

    }

    private void addDescriptionSubTask(MessageReceivedEvent event, boolean isGroupWork, String[] message) {
        HashMap<String, PriorityQueue<Tasks>> tasks = isGroupWork ? this.groupWork.get(event.getChannel().getName()) : this.userTasks;
        String user = getNameOfUser(event);
        Tasks task = findTask(event, message[1], tasks);
        if (task == null) {
            event.getChannel().sendMessage("The task does not exist. ").queue();
            return;
        }
        Tasks.TreeNode subtask = findSubTask(event, message[1], message[2], tasks);
        if (subtask == null) {
            event.getChannel().sendMessage("The subtask does not exist for the task " + message[1] + " for user " + user).queue();
            return;
        }
        StringBuilder description = new StringBuilder();
        if (isGroupWork) {
            for (int i = 3; i < message.length - 1; i++) {
                description.append(message[i]).append(" ");
            }
        } else {
            for (int i = 3; i < message.length; i++) {
                description.append(message[i]).append(" ");
            }
        }
        subtask.setDescriptionOfSubTask(description.toString());
        event.getChannel().sendMessage("Description has successfully been added for subtask " + message[2] + " for task " + message[1] + " for user " + user).queue();

    }

    private void addGroupTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 5) {
            event.getChannel().sendMessage("Invalid format: !GaddTask [name of task] [type of task] [priority level] [deadline: (MM/dd/yyyy)]").queue();
            return;
        }
        addTaskCommand(event, true, message);

    }

    private void addUserTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 5) {
            event.getChannel().sendMessage("Invalid format. Correct format: !addTask [name of task] [type of task] [priority level] [deadline: (MM/dd/yyyy)]").queue();
            return;
        }
        addTaskCommand(event, false, message);
    }

    private void addGroupSubTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 5) {
            event.getChannel().sendMessage("Invalid format. Correct format as follows: !GaddSubTask [name of task] [name of subtask] [priority level] [deadline: (MM/dd/yyyy)]").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("There are no tasks stored for the group " + event.getChannel().getName()).queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The user " + getNameOfUser(event) + " does not have the overarching task " + message[1]).queue();
            return;
        }
        addSubTaskCommand(event, true, message);

    }

    private void addUserSubTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 5) {
            event.getChannel().sendMessage("Invalid format.Correct format as follows: !addSubTask [name of task] [name of subtask] [priority level] [deadline: (MM/dd/yyyy)]").queue();
            return;
        }
        addSubTaskCommand(event, false, message);
    }

    private void removeGroupTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid Format Correct format as follows: !GremoveTask [name of task] ").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("Group " + event.getChannel().getName() + " has no tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The task you wish to remove does not exist").queue();
            return;
        }
        removeTask(event, true, message);
    }

    private void removeUserTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid Format Correct format as follows: !removeTask [name of task] ").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("The task you wish to remove does not exist").queue();
            return;
        }
        removeTask(event, false, message);

    }

    private void removeUserSubTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 3) {
            event.getChannel().sendMessage("Invalid format. Correct format as follows: !removeSubTask [name of task] [name of subtask]").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("The overarching task for the subtask does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.userTasks) == null) {
            event.getChannel().sendMessage("The subtask you wish to remove does not exist").queue();
            return;
        }
        removeSubTask(event, false, message);

    }

    private void removeGroupSubTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 3) {
            event.getChannel().sendMessage("Invalid Format. Correct format as follows: !GremoveSubTask [name of task] [name of subtask]").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("Group " + event.getChannel().getName() + " does not have any tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The task you wish to remove does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The subtask you wish to remove for the task " + message[1] + " does not exist.").queue();
            return;
        }
        removeSubTask(event, true, message);

    }

    private void finishedUserTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !finishedTask [name of task]").queue();
            return;
        }
        if (!this.userTasks.containsKey(getNameOfUser(event))) {
            event.getChannel().sendMessage("The user " + getNameOfUser(event) + " does not have any tasks.").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("The task you wish to remove does not exist").queue();
            return;
        }
        finishedTask(event, false, message);
    }

    private void finishedGroupTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !GfinishedTask [name of task] ").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("The group " + event.getChannel().getName() + " has no tasks recorded").queue();
            return;
        }
        if (!this.groupWork.get(event.getChannel().getName()).containsKey(getNameOfUser(event))) {
            event.getChannel().sendMessage("The user " + getNameOfUser(event) + " does not have any tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The task you wish to mark finished does not exist").queue();
            return;
        }
        finishedTask(event, true, message);


    }

    private void finishedUserSubTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 3) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !finishedTask [name of task] [name of subtask]").queue();
            return;
        }
        if (!this.userTasks.containsKey(getNameOfUser(event))) {
            event.getChannel().sendMessage("The user " + getNameOfUser(event) + " does not have any tasks.").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("The overarching task of the subtask you wish to remove does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.userTasks) == null) {
            event.getChannel().sendMessage("The subtask you wish to mark finished does not exist").queue();
            return;
        }
        finishedSubTask(event, false, message);
    }

    private void finishedGroupSubTask(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 3) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !GfinishedSubTask [name of task] [name of subtask] ").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("The group " + event.getChannel().getName() + " does not exist").queue();
            return;
        }
        if (!this.groupWork.get(event.getChannel().getName()).containsKey(getNameOfUser(event))) {
            event.getChannel().sendMessage("The user " + getNameOfUser(event) + " does not have any tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The overarching task for the subtask you wish to mark finished does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The subtask you wish to mark finished does not exist ").queue();
            return;
        }
        finishedSubTask(event, true, message);

    }

    private void startedTaskUser(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !startedTask [name of task]").queue();
            return;
        }
        if (!this.userTasks.containsKey(getNameOfUser(event))) {
            event.getChannel().sendMessage("User " + getNameOfUser(event) + " does not have any tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("The task you wish to mark as started does not exist").queue();
            return;
        }
        startedTask(event, false, "", message);

    }

    private void startedTaskGroup(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !GstartedTask [name of task]").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("The group " + event.getChannel().getName() + " does not exist").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The task you wish to mark as started does not exist").queue();
            return;
        }
        startedTask(event, true, event.getChannel().getName(), message);


    }

    private void addTaskDescriptionUser(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length < 3) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !startedTask [name of task] [description]").queue();
            return;
        }
        if (!this.userTasks.containsKey(getNameOfUser(event))) {
            event.getChannel().sendMessage("User " + getNameOfUser(event) + " does not have any tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("The task you wish to mark as started does not exist").queue();
            return;
        }
        addDescriptionTask(event, false, "", message);


    }

    private void addTaskDescriptionGroup(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length < 3) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !GaddDescriptionTask [name of task] [description]").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("The group " + event.getChannel().getName() + " has no tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The overarching task of the subtask you wish to mark as started does not exist").queue();
            return;
        }
        addDescriptionTask(event, true, event.getChannel().getName(), message);

    }

    private void addSubTaskDescriptionUser(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length < 4) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !addSubTaskDescription [name of task] [name of subtask] [description]").queue();
            return;
        }
        if (!this.userTasks.containsKey(getNameOfUser(event))) {
            event.getChannel().sendMessage("User " + getNameOfUser(event) + " does not have any tasks").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("The overarching task of the subtask you wish to mark as started does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.userTasks) == null) {
            event.getChannel().sendMessage("The subtask you wish to mark as started does not exist").queue();
            return;
        }
        addDescriptionSubTask(event, false, message);
    }

    private void addSubTaskDescriptionGroup(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length < 4) {
            event.getChannel().sendMessage("Invalid Format. Correct format is !GaddSubTaskDescription[name of task] [name of subtask] [description]").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("The group-work " + event.getChannel().getName() + " does not exist").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The overarching task of the subtask you wish to mark as started does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The subtask you wish to mark as started does not exist").queue();
            return;
        }
        addDescriptionSubTask(event, true, message);

    }

    private void sendReportGroup(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 1) {
            event.getChannel().sendMessage("Invalid format. Correct format is as follows !GgetReport").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("There are no tasks for group" + event.getChannel().getName()).queue();
            return;
        }
        if(this.groupWork.get(event.getChannel().getName()).get(getNameOfUser(event)).isEmpty()) {
            event.getChannel().sendMessage("There are no tasks for user "+getNameOfUser(event)+" in group "+event.getChannel().getName()).queue();
            return;
        }
        sendReport(event, true);
    }

    private void getTasksG(MessageReceivedEvent event, String progress) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 1) {
            event.getChannel().sendMessage("Invalid format. Correct format as follows: !GgetCompletedTasks or !GgetPartialCompletedTasks " +
                    "or !GgetIncompleteTasks or !GgetOverdueTasks").queue();
            return;
        }
        getTasksBasedOnProgress(event, progress.toUpperCase(), true);
    }

    private void startedSubTaskGroup(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 3) {
            event.getChannel().sendMessage("Invalid Format. Correct format as follows: !GstartedSubTask [name of task] [name of subtask]").queue();
            return;
        }
        if (!this.groupWork.containsKey(event.getChannel().getName())) {
            event.getChannel().sendMessage("Group work " + event.getChannel().getName() + " does not exist").queue();
            return;
        }
        if (findTask(event, message[1], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("Overarching task for subtask you wish to mark as started does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.groupWork.get(event.getChannel().getName())) == null) {
            event.getChannel().sendMessage("The subtask you wish to mark as started does not exist").queue();
            return;
        }
        startedSubTask(event, true, message);
    }

    private void startedSubTaskUser(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 3) {
            event.getChannel().sendMessage("Invalid format. Correct format as follows: !startedSubTask [name of task] [name of subtask]").queue();
            return;
        }
        if (findTask(event, message[1], this.userTasks) == null) {
            event.getChannel().sendMessage("Overarching task of subtask you wish to mark as started does not exist").queue();
            return;
        }
        if (findSubTask(event, message[1], message[2], this.userTasks) == null) {
            event.getChannel().sendMessage("The subtask you wish to mark as started does not exist").queue();
            return;
        }
        startedSubTask(event, false, message);
    }

    private void startTimer(MessageReceivedEvent event) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid Usage. Correct usage is: !timer [duration in minutes]").queue();
            return;
        }
        int durationInMinutes = Integer.parseInt(message[1]);
        int durationInSeconds = durationInMinutes * 60;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                event.getChannel().sendMessage(event.getAuthor().getAsMention() + ", your timer has ended!").queue();
            }
        }, durationInSeconds * 1000L);
        event.getChannel().sendMessage("Timer started for " + durationInMinutes + " minutes.").queue();
    }


    private void checkAndSendReminders(MessageReceivedEvent event, boolean isGroup, String groupName) {
        String[] message = event.getMessage().getContentRaw().split(" ");
        if (message.length != 2) {
            event.getChannel().sendMessage("Invalid format. Correct format is !reminder [number of days] or !Greminder [number of days]").queue();
            return;
        }
        LocalDate now = LocalDate.now();
        int targetNumberDays=Integer.parseInt(message[1]);
        if (!isGroup) {
            PriorityQueue<Tasks> tasks = this.userTasks.get(getNameOfUser(event));
            if (tasks.isEmpty()) {
                event.getChannel().sendMessage("There are no tasks for user " + getNameOfUser(event)).queue();
                return;
            }
            for (Tasks task : tasks) {
                LocalDate deadline = task.getDeadline();
                long daysUntilDeadline = ChronoUnit.DAYS.between(now, deadline);
                if (daysUntilDeadline == Integer.parseInt(message[1])) {
                    event.getAuthor().openPrivateChannel().queue(channel -> {
                        channel.sendMessage("Reminder: You have a task due in " + daysUntilDeadline + " days. Task: " + task.getReport()).queue();
                    });
                }


            }

        } else {
            HashMap<String, PriorityQueue<Tasks>> groupTasks = this.groupWork.get(groupName);
            if (groupTasks == null || groupTasks.isEmpty()) {
                event.getChannel().sendMessage("There are no tasks for group " + groupName).queue();
                return;
            }
            for (Map.Entry<String, PriorityQueue<Tasks>> entry : groupTasks.entrySet()) {
                String memberId = entry.getKey();
                PriorityQueue<Tasks> tasks=entry.getValue();
                for (Tasks task : tasks) {
                    LocalDate deadline = task.getDeadline();
                    long daysUntilDeadline = ChronoUnit.DAYS.between(now, deadline);
                    if (Math.abs(daysUntilDeadline)==targetNumberDays) {
                        event.getAuthor().openPrivateChannel().queue(channel -> {
                            channel.sendMessage("Reminder: Group task due in " + daysUntilDeadline + " days. Task: " + task.getReport()).queue();
                        });

                        }

                    }

            }
        }
    }
}





















    


