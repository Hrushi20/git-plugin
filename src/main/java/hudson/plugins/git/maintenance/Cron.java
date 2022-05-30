package hudson.plugins.git.maintenance;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.model.PeriodicWork;
import jenkins.model.Jenkins;

import java.util.Calendar;

//@Extension
public class Cron extends PeriodicWork {

    MaintenanceController contoller = new MaintenanceController();

    public Cron() throws ANTLRException {
    }

    @Override
    public long getRecurrencePeriod() {
        System.out.println("Time has occurred");
        return 1000;
    }

    @Override
    public long getInitialDelay(){
        return MIN - (Calendar.getInstance().get(Calendar.SECOND) * 1000);
    }

    @Override
    protected void doRun() throws Exception {
//        MaintenanceExecutor.Executor.checkCronSyntaxAndExecute();

    }
}
