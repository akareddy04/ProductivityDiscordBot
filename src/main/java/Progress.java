import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Progress {
    private class Status {
        private boolean isCompleted;
        private boolean halfCompleted;
        private boolean incomplete;
        private boolean overDue;

        public Status() {
            this.isCompleted=false;
            this.halfCompleted=false;
            this.incomplete=true;
            this.overDue=false;
        }



    }

    private Date startDate;
    private Date endDate;
    private Status status;


    public String getStatus() {
        String status="";
        if(this.status.isCompleted==true) {
            status+="COMPLETED";
        } else if(this.status.halfCompleted==true) {
            status+="PARTIALLY COMPLETED";
        } else if(this.status.incomplete==true) {
            status+="INCOMPLETE";
        } else if (this.status.overDue==true) {
            status+="OVERDUE";
        }
        return status;



    }


    public Progress() {
        this.startDate=null;
        this.endDate=null;
        this.status= new Status();
    }

    public void setNewStartDate(String newStartDate) {
        try {
            this.startDate= new SimpleDateFormat("MM/dd/yyyy").parse(newStartDate);
        }catch(ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void setNewEndDate(String newStartDate) {
        try {
            this.endDate= new SimpleDateFormat("MM/dd/yyyy").parse(newStartDate);
        }catch(ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public void completedTask() {
        this.status.isCompleted=true;
        this.status.halfCompleted=false;
        this.status.incomplete=false;
        this.endDate= new Date();
    }
    public void halfCompletedTask() {
        this.status.isCompleted=false;
        this.status.incomplete=false;
        this.status.halfCompleted=true;
    }


    public Date getStartDate() {
        return this.startDate;
    }
    public Date getEndDate() {
        return this.endDate;
    }


}
