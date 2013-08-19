package nu.wasis.stunden.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import nu.wasis.stunden.exception.InvalidConfigurationException;
import nu.wasis.stunden.model.WorkPeriod;
import nu.wasis.stunden.plugin.OutputPlugin;
import nu.wasis.stunden.plugins.ahkscriptforsapgui.StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@PluginImplementation
public class StundenAutoHotkeyScriptForSAPGUIOutputPlugin implements OutputPlugin {

	private static final Logger LOG = Logger.getLogger(StundenAutoHotkeyScriptForSAPGUIOutputPlugin.class);
	
    @Override
    public void output(final WorkPeriod workPeriod, final Object configuration) {
    	if (null == configuration || !(configuration instanceof StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig)) {
    		throw new InvalidConfigurationException("Configuration null or wrong type.");
    	}
    	final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig myConfig = (StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig) configuration;
        try {
			run(workPeriod, myConfig);
		} catch (IOException | TemplateException e) {
			LOG.error("Something went wrong", e);
		}
    }
    
    private void run(final WorkPeriod workPeriod, final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) throws IOException, TemplateException {
    	LOG.info("Generating script file...");
        // generate simple replacements map from config
        final Map<String, String> simpleReplacements = getSimpleReplacements(configuration);
        // generate script from workPeriod
        // ...
        // load script_template.ftl
        StringTemplateLoader templateLoader = new StringTemplateLoader();
		templateLoader.putTemplate("script_template", IOUtils.toString(getClass().getResourceAsStream("/script_template.ftl")));
        // do replacements
        Configuration templateConfig = new Configuration();
        templateConfig.setTemplateLoader(templateLoader);
        Template template = templateConfig.getTemplate("script_template");

        final StringWriter writer = new StringWriter();
		template.process(simpleReplacements, writer);
        System.out.println(writer.toString());
        // save result
        // execute result if desired
        if (configuration.isAutoRun()) {
        	final String filename = UUID.randomUUID().toString();
        	final File tempFile  = File.createTempFile(filename, "");
			IOUtils.write(writer.toString(), new FileOutputStream(tempFile));
			Runtime.getRuntime().exec(new String[] {configuration.getAutoHotkeyExecutable(), tempFile.getAbsolutePath()});
        }
    }

    private Map<String, String> getSimpleReplacements(final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) {
		final Map<String, String> replacements = new HashMap<>();
		
		replacements.put("sapExecutable", configuration.getSapExecutable());
		
		return replacements;
	}

	@Override
    public Class<StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig> getConfigurationClass() {
        return StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig.class;
    }

}
