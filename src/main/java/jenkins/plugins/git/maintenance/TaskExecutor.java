package jenkins.plugins.git.maintenance;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.git.GitTool;
import hudson.plugins.git.util.GitUtils;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.git.maintenance.Logs.CacheRecord;
import jenkins.plugins.git.maintenance.Logs.XmlSerialize;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.gitclient.CliGitAPIImpl;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TaskExecutor executes the maintenance tasks on all Caches on Jenkins controller. A lock is added to each cache before running maintenance on it.
 * It is an independent thread. If a cache is already locked, it skips the maintenance task. {@link GitClient} manages the execution of maintenance.
 *
 * @author Hrushikesh Rao
 */
public class TaskExecutor implements Runnable {

    /**
     * Boolean to toggle the state of execution of thread.
     */
    private volatile boolean isThreadAlive;

    /**
     * Type of maintenance task being executed on caches.
     */
    Task maintenanceTask;

    /**
     * List of caches present on Jenkins controller.
     */
    private List<GitMaintenanceSCM.Cache> caches;

    XmlSerialize xmlSerialize;

    private static final Logger LOGGER = Logger.getLogger(TaskExecutor.class.getName());

    /**
     * Initializes the thread to execute a maintenance task.
     *
     * @param maintenanceTask Type of maintenance task required for execution.
     */
    public TaskExecutor(Task maintenanceTask){
        this.maintenanceTask = new Task(maintenanceTask);
        caches = getCaches();
        isThreadAlive = true;
        xmlSerialize = new XmlSerialize();
        LOGGER.log(Level.FINE,"New Thread created to execute " + maintenanceTask.getTaskName());
    }

    /**
     * Executes the maintenance task by iterating through the all caches on Jenkins controller.
     */
    @Override
    public void run() {

        LOGGER.log(Level.INFO,"Executing maintenance task " + maintenanceTask.getTaskName() + " on git caches.");
        try {
            for (GitMaintenanceSCM.Cache cache : caches) {
                // For now adding lock to all kinds of maintenance tasks. Need to study on which task needs a lock and which doesn't.
                Lock lock = cache.getLock();
                File cacheFile = cache.getCacheFile();
                executeMaintenanceTask(cacheFile,lock);
            }

            LOGGER.log(Level.INFO,maintenanceTask.getTaskName() + " has been executed successfully.");
        }catch (InterruptedException e){
            LOGGER.log(Level.WARNING,"Interrupted Exception. Msg: " + e.getMessage());
        }

    }

    /**
     * Executes maintenance task on a single cache. The cache is first locked and then undergoes git maintenance.
     *
     * @param cacheFile File of a single cache.
     * @param lock Lock for a single cache.
     * @throws InterruptedException When GitClient is interrupted during maintenance execution.
     */
    void executeMaintenanceTask(File cacheFile,Lock lock) throws InterruptedException{

        TaskType taskType = maintenanceTask.getTaskType();
        long executionDuration = 0;
        boolean executionStatus = false;
        GitClient gitClient;
        // If lock is not available on the cache, skip maintenance on this cache.
        if (isThreadAlive && lock.tryLock()) {

            LOGGER.log(Level.FINE, "Cache " + cacheFile.getName() + " locked.");

            try {
                gitClient = getGitClient(cacheFile);
                if (gitClient == null)
                    throw new InterruptedException("Git Client couldn't be instantiated");

                executionDuration -= System.currentTimeMillis();
                if(gitVersionAtLeast(2,30,0)){
                    executeGitMaintenance(gitClient,taskType);
                }else{
                    executeLegacyGitMaintenance(gitClient,taskType);
                }
                executionDuration += System.currentTimeMillis();
                executionStatus = true;
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, "Couldn't run " + taskType.getTaskName() + ".Msg: " + e.getMessage());
            } finally {
                lock.unlock();
                LOGGER.log(Level.FINE, "Cache " + cacheFile.getName() + " unlocked.");
            }

        } else {
            if(!isThreadAlive)
                throw new InterruptedException("Maintenance thread has been interrupted. Terminating...");
            else
                LOGGER.log(Level.FINE,"Cache is already locked. Can't run maintenance on cache " + cacheFile.getName());
        }

        xmlSerialize.addMaintenanceRecord(createRecord(cacheFile,taskType,executionStatus,executionDuration)); // Stores the record in jenkins.
    }

    /**
     * Executes git maintenance on single cache for git versions >= 2.30 .{@link TaskType} defines the type of maintenance task.
     *
     * @param gitClient {@link GitClient} on a single cache.
     * @param taskType Type of maintenance task.
     * @throws InterruptedException When GitClient is interrupted during maintenance execution.
     */
    void executeGitMaintenance(GitClient gitClient,TaskType taskType) throws InterruptedException {
        // checking git version for every cache. Reason is because in the UI, the git version can be changed anytime.
        LOGGER.log(Level.FINE,"Git version >= 2.30.0 detected. Using official git maintenance command.");
        if(taskType.equals(TaskType.GC)){
            gitClient.maintenance("gc");
        }else if(taskType.equals(TaskType.COMMIT_GRAPH)){
            gitClient.maintenance("commit-graph");
        }else if(taskType.equals(TaskType.PREFETCH)){
            gitClient.maintenance("prefetch");
        }else if(taskType.equals(TaskType.INCREMENTAL_REPACK)){
            gitClient.maintenance("incremental-repack");
        }else if(taskType.equals(TaskType.LOOSE_OBJECTS)){
            gitClient.maintenance("loose-objects");
        }else{
            LOGGER.log(Level.WARNING,"Invalid maintenance task.");
            terminateThread();
        }
    }

    /**
     * Executes git maintenance on single cache for git versions < 2.30 .{@link TaskType} defines the type of maintenance task.
     *
     * @param gitClient {@link GitClient} on single cache.
     * @param taskType Type of maintenance task.
     * @throws InterruptedException When GitClient is interrupted during maintenance execution.
     */
    void executeLegacyGitMaintenance(GitClient gitClient,TaskType taskType) throws InterruptedException{
        LOGGER.log(Level.FINE,"Git version < 2.30.0 detected. Using legacy git maintenance commands");

        // If git version < 2.18.0 =====> run only gc.
        // If git version >= 2.18.0 && git version <=2.19.1 ======> run gc and commit-graph
        // If git version >= 2.20.0 && git version < 2.30 ========> run gc , commit-graph && multi-pack-index

        if(gitVersionAtLeast(2,20,0)){
            // execute gc, commit-graph && multi-pack-index
            if(taskType.equals(TaskType.GC)){
                gitClient.maintenanceLegacy("gc");
            }else if(taskType.equals(TaskType.INCREMENTAL_REPACK)){
                gitClient.maintenanceLegacy("incremental-repack");
            }else if(taskType.equals(TaskType.COMMIT_GRAPH)){
                gitClient.maintenanceLegacy("commit-graph");
            }else{
                LOGGER.log(Level.INFO,"Cannot execute " + taskType.getTaskName() + " maintenance task due to older git version");
                terminateThread();
            }
        }else if(gitVersionAtLeast(2,18,0)){
            // execute gc && commit-graph
            if(taskType.equals(TaskType.GC))
                gitClient.maintenanceLegacy("gc");
            else if(taskType.equals(TaskType.COMMIT_GRAPH))
                gitClient.maintenanceLegacy("commit-graph");
            else {
                LOGGER.log(Level.INFO, "Cannot execute " + taskType.getTaskName() + " maintenance task due to older git version");
                terminateThread();
            }
        }else {
            // These are git versions less than 2.18.0
            // execute gc only
            if(taskType.equals(TaskType.GC))
                gitClient.maintenanceLegacy("gc");
            else {
                LOGGER.log(Level.INFO, "Cannot execute " + taskType.getTaskName() + " maintenance task due to older git version");
                terminateThread();
            }
        }
    }

    /**
     * Checks if the git version used for maintenance is matching the requirements passed by the method parameters.
     *
     * @param neededMajor Major git version.
     * @param neededMinor Minor git version.
     * @param neededPatch Patch git version.
     * @return a boolean that checks the git version used for maintenance is greater than or equal to the required version.
     */
    boolean gitVersionAtLeast(int neededMajor, int neededMinor, int neededPatch){
        return MaintenanceTaskConfiguration.gitVersionAtLeast(neededMajor,neededMinor,neededPatch);
    }

    /**
     * Returns a list of caches present on Jenkins controller. See {@link GitMaintenanceSCM#getCaches()}
     *
     * @return List of caches on Jenkins controller.
     */
    List<GitMaintenanceSCM.Cache> getCaches(){
        List<GitMaintenanceSCM.Cache> caches =  GitMaintenanceSCM.getCaches();
        LOGGER.log(Level.INFO,"Fetched all caches present on Jenkins Controller.");
        return caches;
    }

    /**
     * Returns {@link GitClient} on a single cache.
     *
     * @param file File object of a single cache.
     * @return GitClient on single cache.
     */
    GitClient getGitClient(File file){
        try {
            TaskListener listener = new LogTaskListener(LOGGER, Level.FINE);
            final Jenkins jenkins = Jenkins.getInstanceOrNull();
            GitTool gitTool = GitUtils.resolveGitTool(null, jenkins, null, listener);
            if (gitTool == null) {
                LOGGER.log(Level.WARNING, "No GitTool found while running " + maintenanceTask.getTaskName());
                return null;
            }

            String gitExe = gitTool.getGitExe();
            if (file != null) {
                FilePath workspace = new FilePath(file);
                Git git = Git.with(listener, null).in(workspace).using(gitExe);

                GitClient gitClient = git.getClient();
                if (gitClient instanceof CliGitAPIImpl)
                    return gitClient;
                LOGGER.log(Level.WARNING, "JGit requested, but does not execute maintenance tasks");
            } else {
                LOGGER.log(Level.WARNING, "Cli Git will not execute maintenance tasks due to null file arg");
            }

        }catch (InterruptedException | IOException e ){
            LOGGER.log(Level.WARNING,"Git Client couldn't be initialized.");
        }
        return null;
    }

    /**
     * Terminates this thread and stops further execution of maintenance tasks.
     */
    public void terminateThread(){
        isThreadAlive = false;
    }

    /**
     * Returns a Record ({@link CacheRecord}) which contains the result of maintenance data on a single cache.
     *
     * @param cacheFile File object of a single cache.
     * @param taskType Type of maintenance task.
     * @param executionStatus Whether maintenance task has been executed successfully on a cache.
     * @param executionDuration Amount of time taken to execute maintenance task in milliseconds.
     * @return {@link CacheRecord} of a single cache.
     */
    CacheRecord createRecord(File cacheFile, TaskType taskType,boolean executionStatus,long executionDuration){

       CacheRecord cacheRecord = new CacheRecord(cacheFile.getName(),taskType.getTaskName());
       long repoSizeInBytes = FileUtils.sizeOfDirectory(cacheFile);
       String repoSize = FileUtils.byteCountToDisplaySize(repoSizeInBytes); // Converts the bytes to KB,MB,GB
       cacheRecord.setRepoSize(repoSize);
       cacheRecord.setExecutionStatus(executionStatus);
       if(!executionStatus)
           cacheRecord.setExecutionDuration(-1);
       else cacheRecord.setExecutionDuration(executionDuration);
       cacheRecord.setTimeOfExecution(System.currentTimeMillis()/1000); // Store the unix timestamp
       return cacheRecord;
    }
}
