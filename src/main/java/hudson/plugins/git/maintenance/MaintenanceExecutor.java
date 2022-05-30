package hudson.plugins.git.maintenance;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.TaskThread;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;

import java.io.IOException;
import java.util.*;

public class MaintenanceExecutor{
    static Thread maintenanceExecutionThread = null;
    static Queue<String> maintenanceQueue = new LinkedList<>();

    public static class Executor {
        static ArrayList<String> cronSyntax = new ArrayList<>();
        static CronTabList cronTabList;

        static {
            cronSyntax.add("H/2 * * * *");
//            cronSyntax.add("* * * * *");
//            cronSyntax.add("3 * * * *");
            try {
                cronTabList = new CronTabList(Collections.singletonList(new CronTab(cronSyntax.get(0),0,null,null)));
            } catch (ANTLRException e) {
                throw new RuntimeException(e);
            }
        }

        public static void checkCronSyntaxAndExecute() {
            boolean isCronSyntaxValid = cronTabList.check(new GregorianCalendar());
            System.out.println(isCronSyntaxValid);
            if (isCronSyntaxValid) {
                maintenanceQueue.add("H/2 * * * *");
            }
            ;

            class MyRunnable implements Runnable {

                Queue<String> maintenanceQueue;

                MyRunnable(Queue<String> maintenanceQueue) {
                    this.maintenanceQueue = maintenanceQueue;
                }

                @Override
                public void run() {
                    System.out.println("Printed inside the thread");
                }
            }


            if (maintenanceExecutionThread == null) {
                maintenanceExecutionThread = new Thread(new MyRunnable(maintenanceQueue));
                maintenanceExecutionThread.start();
            }
                System.out.println(maintenanceExecutionThread.isAlive());
        }
    }
}
