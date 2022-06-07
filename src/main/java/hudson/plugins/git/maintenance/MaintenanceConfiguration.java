package hudson.plugins.git.maintenance;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.HashMap;

@Extension
public class MaintenanceConfiguration extends GlobalConfiguration {

    String gc;
    String commitGraph;
    String prefetch;

//    @DataBoundConstructor
//    public MaintenanceConfiguration(JSONObject cronData) {
//        this.cronData = cronData;
//    }

    public MaintenanceConfiguration(){
        System.out.println("loading....");
        load();
        System.out.println(gc + " " + prefetch + " " + commitGraph);
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Maintenance Configuration";
    }

    public HashMap<String, String> getCronData(){
        HashMap<String,String> hm = new HashMap<>();
        hm.put("gc",this.gc);
        hm.put("prefetch",this.prefetch);
        hm.put("commitGraph",this.commitGraph);
        return hm;
    }

    @DataBoundSetter
    public void setCronData(String gc, String prefetch, String commitGraph){
        this.gc = gc;
        this.prefetch = prefetch;
        this.commitGraph = commitGraph;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return super.configure(req, json);
    }

//    @Extension
//    public static class DescriptorImpl extends Descriptor<GlobalConfiguration> {
//
//        public DescriptorImpl(){this.load();}
//        @Override
//        public boolean configure(StaplerRequest req, net.sf.json.JSONObject json) throws FormException {
//            save();
//            return super.configure(req, json);
//        }
//    }
}
