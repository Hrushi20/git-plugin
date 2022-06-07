package hudson.plugins.git.maintenance;

import jenkins.plugins.git.AbstractGitSCMSource;

import java.io.File;

public class GetGitCachePath extends AbstractGitSCMSource {
    @Override
    public String getCredentialsId() {
        return "";
    }

    @Override
    public String getRemote() {
        return "https://github.com/Hrushi20/multibranch-sample-app";
    }

    public String cacheEntry(){
        return getCacheEntry();
    }

    public File getFilePath(String cacheEntry){
        return getCacheDir(cacheEntry,true);
    }
}
