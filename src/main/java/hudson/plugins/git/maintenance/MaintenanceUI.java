package hudson.plugins.git.maintenance;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Main;
import hudson.model.ManagementLink;
import hudson.security.Permission;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.http.HttpServletResponse;

@Extension
public class MaintenanceUI extends ManagementLink {
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

    @NonNull
    @Override
    public Permission getRequiredPermission() {
        return Jenkins.ADMINISTER;
    }


    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void doSaveCronConfig(StaplerRequest req, StaplerResponse res) throws Exception {
        if (!Jenkins.get().hasPermission(Jenkins.SYSTEM_READ)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String gcCron = req.getParameter("gc");
        String prefetchCron = req.getParameter("prefetch");
        String commitGraphCron = req.getParameter("commitGraph");

        MaintenanceConfiguration config = GlobalConfiguration.all().get(MaintenanceConfiguration.class);

        if(config.getCronData() == null){
            config.setCronData(gcCron,prefetchCron,commitGraphCron);
        }

        res.getOutputStream().write(12321);
    }

    @RequirePOST
    @Restricted(NoExternalUse.class)
    public void runMaintenanceTasks(){

    }
}
