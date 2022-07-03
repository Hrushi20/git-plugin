package jenkins.plugins.git.maintenance;

import antlr.ANTLRException;
import hudson.scheduler.CronTabList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TaskSchedulerTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private TaskScheduler taskScheduler;
    private MaintenanceTaskConfiguration config;

    @Before
    public void setUp() throws Exception {
        taskScheduler = new TaskScheduler();
        config = new MaintenanceTaskConfiguration();

    }

    // Tested all the internal functions of this method
    @Test
    public void testScheduleTasks() throws ANTLRException {
        taskScheduler.scheduleTasks();
    }

    @Test
    public void testCheckIsTaskInQueue() throws Exception {
        config.setCronSyntax(TaskType.PREFETCH,"* * * * *");
        config.setIsTaskConfigured(TaskType.PREFETCH,true);

        config.setCronSyntax(TaskType.COMMIT_GRAPH,"* * * * *");
        config.setIsTaskConfigured(TaskType.COMMIT_GRAPH,true);

        List<Task> maintenanceTasks = config.getMaintenanceTasks();
        taskScheduler.addTasksToQueue(maintenanceTasks);

        List<Task> configuredTask = config.getMaintenanceTasks().stream().filter(Task::getIsTaskConfigured).collect(Collectors.toList());

        configuredTask.forEach(task -> assertTrue(taskScheduler.checkIsTaskInQueue(task)));
    }

    @Test
    public void testAddTasksToQueue() throws Exception {
        // Adding Maintenance tasks configuration;
        config.setCronSyntax(TaskType.PREFETCH,"* * * * *");
        config.setIsTaskConfigured(TaskType.PREFETCH,true);
        config.setCronSyntax(TaskType.LOOSE_OBJECTS,"* * * * *");
        config.setIsTaskConfigured(TaskType.LOOSE_OBJECTS,true);

        config.setCronSyntax(TaskType.GC,"5 1 1 1 1");
        config.setIsTaskConfigured(TaskType.GC,true);
        config.setCronSyntax(TaskType.COMMIT_GRAPH,"H * * * *");
        List<Task> maintenanceTasks = config.getMaintenanceTasks();

        int length = 2;

        taskScheduler.addTasksToQueue(maintenanceTasks);
        assertThat(taskScheduler.maintenanceQueue.size(),is(length));
    }

    @Test
    public void testIsGitMaintenanceTaskRunning(){
        // Setting value to true
        config.setIsGitMaintenanceRunning(true);
        boolean isGitMaintenanceTaskRunning = taskScheduler.isGitMaintenanceTaskRunning(config);
        assertTrue(isGitMaintenanceTaskRunning);

        // set value to false
        config.setIsGitMaintenanceRunning(false);
        isGitMaintenanceTaskRunning = taskScheduler.isGitMaintenanceTaskRunning(config);
        assertFalse(isGitMaintenanceTaskRunning);
    }

    @Test
    public void testCreateNoExecutorThread() throws Exception{
        config.setCronSyntax(TaskType.PREFETCH,"5 1 1 1 1");
        config.setIsTaskConfigured(TaskType.PREFETCH,true);

        List<Task> maintenanceTasks = config.getMaintenanceTasks();
        taskScheduler.addTasksToQueue(maintenanceTasks);
        taskScheduler.createTaskExecutorThread();
        assertNull(taskScheduler.taskExecutor);

    }

    @Test
    public void testCreateExecutionThread() throws Exception{

        config.setCronSyntax(TaskType.PREFETCH,"* * * * *");
        config.setIsTaskConfigured(TaskType.PREFETCH,true);

        List<Task> maintenanceTasks = config.getMaintenanceTasks();
        taskScheduler.addTasksToQueue(maintenanceTasks);
        taskScheduler.createTaskExecutorThread();
        assertTrue(taskScheduler.taskExecutor.isAlive());
    }

    @Test
    public void testCorrectCronSyntaxInput() throws ANTLRException{
        List<String> correctCronSyntax = new ArrayList<>();
        correctCronSyntax.add("H * * * *");
        correctCronSyntax.add("* * * * *");
        correctCronSyntax.add("@hourly");
        correctCronSyntax.add("@weekly");
        correctCronSyntax.add("@daily");
        correctCronSyntax.add("H H 1,15 1-11 *");

        for(String cronSyntax : correctCronSyntax){
            CronTabList cronTabList = taskScheduler.getCronTabList(cronSyntax);
            assertNotNull(cronTabList);
        }
    }

    @Test(expected = ANTLRException.class)
    public void testIncorrectCronSyntaxInput() throws ANTLRException{
        List<String> incorrectCronSyntax = new ArrayList<>();
        incorrectCronSyntax.add("");
        incorrectCronSyntax.add("**");

        // Valid format is (0-59) (0–23) (1–31) (1–12) (0–7)
        incorrectCronSyntax.add("60 1 1 1 1");
        incorrectCronSyntax.add("1 24 32 11 5");
        incorrectCronSyntax.add("1 1 1 1 9");

        for(String cronSyntax : incorrectCronSyntax){
            // Should throw an exception.
            taskScheduler.getCronTabList(cronSyntax);
        }

    }

}