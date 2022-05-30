package hudson.plugins.git.maintenance;

import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Descriptor;
import io.jenkins.cli.shaded.jakarta.websocket.OnClose;
import jenkins.model.GlobalConfiguration;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class MaintenanceConfiguration extends GlobalConfiguration {

    JSONObject cronData;

    public MaintenanceConfiguration(){
        System.out.println("loading....");
        load();
        System.out.println(cronData);
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Maintenance Configuration";
    }

    public JSONObject getCronData(){
        return cronData;
    }

    @DataBoundSetter
    public void setCronData(JSONObject cronData){
        this.cronData = cronData;
    }

    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        System.out.println(json);
        req.bindJSON(this, net.sf.json.JSONObject.fromObject(cronData));
        save();
        return super.configure(req, net.sf.json.JSONObject.fromObject(cronData));
    }
}
