package hudson.plugins.git.maintenance;

import antlr.ANTLRException;
import hudson.scheduler.CronTab;
import hudson.scheduler.CronTabList;
import hudson.triggers.Trigger;
import jenkins.plugins.git.AbstractGitSCMSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
//
public class MaintenanceController extends AbstractGitSCMSource {
    public MaintenanceController(){

    }

    @Override
    public String getCredentialsId() {
        return null;
    }

    @Override
    public String getRemote() {
        return null;
    }
//    HashMap<String,String> maintenanceTasks = new HashMap<>();
//    HashMap<String, CronTabList> cronTabs;
//
//    public MaintenanceController() throws ANTLRException {
//        maintenanceTasks.put("prefetch","* * * * *");
//        maintenanceTasks.put("gc","* * * * *");
//        maintenanceTasks.put("loose-objects","* 1 * * *");
//        maintenanceTasks.put("commit-graph","* 2 * * *");
//        maintenanceTasks.put("test","* * * * *");
//
//        this.createCronTabs(maintenanceTasks);
//    }
//
//    private void createCronTabs(HashMap<String,String> maintenanceTasks) throws ANTLRException {
//        for(Map.Entry<String,String> entry : maintenanceTasks.entrySet()){
//            cronTabs.put(entry.getKey(),new CronTabList(Collections.singleton(new CronTab(entry.getValue()))));
//        }
//    }
//
//
}
