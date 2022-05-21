package hudson.plugins.git.maintenance;

import com.google.gson.JsonObject;
import edu.umd.cs.findbugs.annotations.NonNull;
import jenkins.model.GlobalConfiguration;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;

public class MaintenanceConfiguration extends GlobalConfiguration {

    JSONObject cronData;

    public MaintenanceConfiguration(){
        load();
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
    public void setCronData(String gc, String prefetch, String commitGraph){
        this.cronData = new JSONObject();
        cronData.put("gc",gc);
        cronData.put("prefetch",prefetch);
        cronData.put("commitGraph",commitGraph);
    }

}
