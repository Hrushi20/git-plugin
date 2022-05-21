package hudson.plugins.git.maintenance;

import antlr.ANTLRException;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParametersDefinitionProperty;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import static hudson.Util.fixNull;

@Extension @Symbol("parameterizedCron")
public class DescriptorImpl extends TriggerDescriptor {

	/**
	 * I don't like inner classes. Using the declaritive support here by calling super constructor with class.
	 */
	public DescriptorImpl() {
//		super(ParameterizedTimerTrigger.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean isApplicable(Item item) {
		return true;
	}
}
