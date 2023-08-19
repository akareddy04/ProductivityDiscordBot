import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;

public class Tasks {
    private String name;
    private Date deadline;
    private String type;
    private int priorityLevel;
    private PriorityQueue<TreeNode> subtasks;
    private Progress progressOfTask;
    private String description;

    private static class TreeNodeComparator implements Comparator<TreeNode> {

        @Override
        public int compare(TreeNode firstTask, TreeNode secondTask) {
            int deadlineComparison= firstTask.getDeadLineOfSubTasks().compareTo(secondTask.getDeadLineOfSubTasks());
            if(deadlineComparison!=0) {
                return deadlineComparison;
            }
            return Integer.compare(secondTask.getPriorityLevelOfSubTask(), firstTask.priorityLevelOfSubTask);
        }
    }
    public Tasks(String name, String type, int priorityLevel, String deadline) {
        this.name=name;
        this.type=type;
        this.priorityLevel=priorityLevel;
        try {
            this.deadline= new SimpleDateFormat("MM/dd/yyyy").parse(deadline);
        } catch(ParseException e) {
            throw new RuntimeException(e);
        }
        this.subtasks= new PriorityQueue<TreeNode>(new TreeNodeComparator());
        this.progressOfTask= new Progress();
        this.description="";
    }

    public String getName() {
        return this.name;
    }
    public LocalDate getDeadline() {
        return this.deadline.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description=description;
    }
    public String getType() {
        return this.type;
    }
    public int getPriorityLevel() {
        return this.priorityLevel;
    }
    public Progress getProgressOfTask() {return this.progressOfTask;}
    
    public void addSubTask(TreeNode subTask) {
        boolean add = this.subtasks.add(subTask);
    }
    public ArrayList<TreeNode> getSubTasks() {
        ArrayList<TreeNode> subTasks= new ArrayList<TreeNode>();
        subTasks.addAll(this.subtasks);
        return subTasks;
    }
    public boolean removeSubTask(TreeNode subTask) {
        if(!this.subtasks.contains(subTask)) {
            return false;
        }
        boolean removed=this.subtasks.remove(subTask);
        return removed;
    }
    public void clearAllSubTasks() {
        this.subtasks.clear();
    }
    public String getReport() {
        StringBuilder report = new StringBuilder();
        report.append("Name of task: ").append(this.name)
                .append(". The type of the task is ").append(this.type)
                .append(". The priority level of this task is ").append(this.priorityLevel)
                .append(". The deadline of this task is ").append(this.deadline)
                .append(". The progress of this task is ").append(this.progressOfTask.getStatus()).append(". ");

        if (this.progressOfTask.getStartDate() == null) {
            report.append("There is no start date + end date recorded for starting/finishing this task. ");
        } else {
            if (this.progressOfTask.getEndDate() == null) {
                report.append("The start date of starting this task is ").append(this.progressOfTask.getStartDate()).append(". ");
            } else {
                report.append("The start date of starting this task is ")
                        .append(this.progressOfTask.getStartDate())
                        .append(" and the end date for finishing this task is ")
                        .append(this.progressOfTask.getEndDate()).append(". ");
            }
        }
        report.append("\n\n");
        if (!this.subtasks.isEmpty()) {
            report.append("Subtasks:\n\n");
            for (TreeNode subtask : this.subtasks) {
                report.append(subtask.getSubTaskReport()).append("\n\n");
            }
            report.append("\n");
        } else {
            report.append("There are no subtasks recorded for this task. ");
        }
        return report.toString();
    }

    public String getFormmatedDeadlineTask() {
        SimpleDateFormat output= new SimpleDateFormat("MM/dd/yyy");
        return output.format(this.deadline);
    }
    public void finishedTask() {
        this.progressOfTask.completedTask();
    }




    public static class TreeNode {
        private String nameOfSubTask;
        private int priorityLevelOfSubTask;
        private Date deadLineOfSubTasks;
        private String descriptionOfSubTask;
        private Progress progressOfSubTasks;


        public TreeNode(String nameOfSubTask, int priorityLevelOfSubTask, String deadlineOfSubTask, String descriptionOfSubTask) {
            this.nameOfSubTask = nameOfSubTask;
            this.priorityLevelOfSubTask = priorityLevelOfSubTask;
            this.descriptionOfSubTask = descriptionOfSubTask;
            this.progressOfSubTasks = new Progress();
            try {
                this.deadLineOfSubTasks = new SimpleDateFormat("MM/dd/yyyy").parse(deadlineOfSubTask);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }

        public int getPriorityLevelOfSubTask() {
            return this.priorityLevelOfSubTask;
        }

        public String getSubTaskReport() {
            StringBuilder report = new StringBuilder();
            report.append("Name of SubTask: ").append(this.nameOfSubTask)
                    .append(". The priority level of this task is ").append(this.priorityLevelOfSubTask)
                    .append(". The deadline of this subtask would be ").append(this.deadLineOfSubTasks).append(". ");
            report.append("The progress of this task is ").append(this.progressOfSubTasks.getStatus()).append(". ");

            if (this.progressOfSubTasks.getStartDate() == null) {
                report.append("There is no start date + end date recorded for starting/finishing this subtask. ");
            } else {
                if (this.progressOfSubTasks.getEndDate() == null) {
                    report.append("The start date of starting this subtask is ").append(this.progressOfSubTasks.getStartDate()).append("\n\n");
                } else {
                    report.append("The start date of starting this subtask is ")
                            .append(this.progressOfSubTasks.getStartDate())
                            .append(" and the end date for finishing this subtask is ")
                            .append(this.progressOfSubTasks.getEndDate()).append("\n\n");
                }
            }

            return report.toString();
        }


        public Date getDeadLineOfSubTasks() {
            return this.deadLineOfSubTasks;

        }
        public void completedSubTask() {
            this.progressOfSubTasks.completedTask();
        }
        public String getNameOfSubTask() {
            return this.nameOfSubTask;
        }
        public Progress getProgressOfSubTasks() {
            return this.progressOfSubTasks;
        }
        public void setDescriptionOfSubTask(String description) {
            this.descriptionOfSubTask=description;
        }
        public String getFormattedDeadlineSubTask() {
            SimpleDateFormat output= new SimpleDateFormat("MM/dd/yyy");
            return output.format(this.deadLineOfSubTasks);
        }
    }


}
