package hudson.plugins.git.maintenance;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.ManagementLink;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;

@Extension
public class MaintenanceUI extends ManagementLink {
    public MaintenanceUI(){

    }
    @Override
    public String getIconFileName() {
        return "/plugin/git-plugin/icons/broom.svg";
    }

    @Override
    public String getDisplayName() {
        return "Git Maintenance";
    }

    @Override
    public String getUrlName() {
        return "maintenance";
    }

    @Override
    public String getDescription() {
        return "Maintenance of your Git Repositories to improve git command performance.";
    }

    public @NonNull String getCategoryName() {
        return "CONFIGURATION";
    }

//    @NonNull
//    @Override
//    public Permission getRequiredPermission() {
//        return Jenkins.ADMINISTER;
//    }

    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doSaveCronConfig(StaplerRequest req, StaplerResponse res) throws Exception {
//        if (!Jenkins.get().hasPermission(Jenkins.SYSTEM_READ)) {
//            res.sendError(HttpServletResponse.SC_FORBIDDEN);
//            return;
//        }

        String gcCron = req.getParameter("gc");
        String prefetchCron = req.getParameter("prefetch");
        String commitGraphCron = req.getParameter("commitGraph");

//        System.out.println(gcCron);


        MaintenanceConfiguration config = GlobalConfiguration.all().get(MaintenanceConfiguration.class);

        if(config != null){
            config.setCronData(gcCron,prefetchCron,commitGraphCron);
            config.save();
        }

        System.out.println(config + " is saved");

//        JSONObject cronData = new JSONObject();
//        cronData.put("gc",gcCron);
//        cronData.put("prefetch",prefetchCron);
//        cronData.put("commitGraph",commitGraphCron);
//        if(config.getCronData() == null) {
//            System.out.println("Setting cron for first time");
//            config.setCronData(cronData);
//            res.getOutputStream().write(12321);
//        }else{
//            System.out.println(config.getCronData());
//        }
//        if(config.getCronData() == null){
//            config.setCronData(gcCron,prefetchCron,commitGraphCron);
//        }
        res.sendRedirect("");

    }

    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doRunMaintenanceTasks(StaplerRequest req,StaplerResponse res) throws IOException, InterruptedException {
        MaintenanceConfiguration config = GlobalConfiguration.all().get(MaintenanceConfiguration.class);
        System.out.println(config);
        System.out.println(config.getCronData());
//        System.out.println(config.getCronData());
//        res.getOutputStream().write(12321);
//        GetGitCachePath path = new GetGitCachePath();
//        String cacheEntry = path.cacheEntry();
//        System.out.println(cacheEntry);
////
//        File filePath = path.getFilePath(cacheEntry);
//        System.out.println(filePath);
//        System.out.println(filePath.getAbsolutePath());

        res.sendRedirect("");
    }

    public void configure(org.kohsuke.stapler.StaplerRequest req, net.sf.json.JSONObject formData){

    }



}
