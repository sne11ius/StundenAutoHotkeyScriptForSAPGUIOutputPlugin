package nu.wasis.stunden.plugins.ahkscriptforsapgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import nu.wasis.stunden.exception.InvalidConfigurationException;
import nu.wasis.stunden.model.Day;
import nu.wasis.stunden.model.Entry;
import nu.wasis.stunden.model.WorkPeriod;
import nu.wasis.stunden.plugin.OutputPlugin;
import nu.wasis.stunden.plugins.ahkscriptforsapgui.config.StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig;
import nu.wasis.stunden.plugins.ahkscriptforsapgui.util.DateUtil;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@PluginImplementation
public class StundenAutoHotkeyScriptForSAPGUIOutputPlugin implements OutputPlugin {

	private static final String INGORE_INDICATOR = "INGORE";

	private static final Logger LOG = Logger.getLogger(StundenAutoHotkeyScriptForSAPGUIOutputPlugin.class);

	private static final List<String> TEMPLATES = Arrays.asList(
		"get_psp_element_names.ftl",
		"open_sap_worksheet.ftl",
		"exit_sap.ftl",
		"enter_work_hours.ftl"
	);

	private StringTemplateLoader templateLoader;

	@Override
	public void output(final WorkPeriod workPeriod, final Object configuration) {
		if (null == configuration || !(configuration instanceof StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig)) {
			throw new InvalidConfigurationException("Configuration null or wrong type.");
		}
		final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig myConfig = (StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig) configuration;
		try {
			run(workPeriod, myConfig);
		} catch (IOException | TemplateException | InterruptedException e) {
			LOG.error("Something went wrong", e);
		}
	}

	private void run(final WorkPeriod workPeriod, final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) throws IOException, TemplateException, InterruptedException {
		LOG.info("Generating script file...");
		StringWriter writer = new StringWriter();
		Map<String, String> replacements = getSimpleReplacements(configuration, workPeriod);
		getTemplateConfiguration().getTemplate("open_sap_worksheet.ftl").process(replacements, writer);
		final String prelude = writer.toString();
		writer = new StringWriter();
		getTemplateConfiguration().getTemplate("exit_sap.ftl").process(replacements, writer);
		final String postlude = writer.toString();
		
		final String interlude = generateInterlude(workPeriod, configuration);
		
		final String fullScript = prelude + interlude + postlude;
		
		System.out.println("Result Script:");
		System.out.println("==============");
		System.out.println(fullScript);
		
		if (configuration.isAutoRun()) {
			final String filename = UUID.randomUUID().toString();
			final File tempFile = File.createTempFile(filename, "");
			IOUtils.write(fullScript, new FileOutputStream(tempFile));
			Runtime.getRuntime().exec(new String[] {
				configuration.getAutoHotkeyExecutable(),
				tempFile.getAbsolutePath()
			});
			tempFile.delete();
		 }
	}

	private String generateInterlude(WorkPeriod workPeriod, StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) throws IOException, TemplateException, InterruptedException {
		final StringBuilder interludeBuilder = new StringBuilder(300); 
		final List<String> sapPspElementNames = getSapPspElementNames(workPeriod, configuration);
		for (String sapPspElementName : sapPspElementNames) {
			LOG.debug("Element: `" + sapPspElementName + "'");
		}
		@SuppressWarnings("unused")
		final Map<String, String> projectsToPSP = createProjectsToPSPMap(workPeriod, configuration, sapPspElementNames);
		LOG.warn("Not implemented yet.");
		return interludeBuilder.toString();
	}

	private Map<String, String> createProjectsToPSPMap(WorkPeriod workPeriod, StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration, final List<String> availablePSPNames) throws IOException, TemplateException, InterruptedException {
		final Map<String, String> projectsToPSPMap = new HashMap<>();
		
		for (Day day : workPeriod.getDays()) {
			for (Entry entry : day.getEntries()) {
				final String projectName = entry.getProject().getName();
				if (null == projectsToPSPMap.get(projectName)) {
					projectsToPSPMap.put(projectName, userSelectPspElement(projectName, availablePSPNames));
				}
			}
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Project Mapping");
			LOG.debug("===============");
			for (java.util.Map.Entry<String, String> entry : projectsToPSPMap.entrySet()) {
				LOG.debug(entry.getKey() + " => " + entry.getValue());
			}
		}
		
		return projectsToPSPMap;
	}

	private String userSelectPspElement(String projectName, List<String> availablePSPNames) throws IOException {
		String pspElementName = "";
		while(pspElementName.isEmpty()) {
			System.out.println("Projekt `" + projectName + "' ist unbekannt. Bitte wählen sie ein zu verwendendes PSP-Element.");
			for (int i = 0; i < availablePSPNames.size(); ++i) {
				final String currentPspName = availablePSPNames.get(i);
				System.out.println(i + "\t- " + currentPspName);
			}
			System.out.println(availablePSPNames.size() + "\t - [Ignorieren]");
			try {
				int n = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
				if (n < 0 || n > availablePSPNames.size()) {
					System.out.println("Jo Homie, gib eine Zahl zwischen 0 und " + (availablePSPNames.size() - 1) + " ein.");
				} else {
					if (n < availablePSPNames.size()) {
						pspElementName = availablePSPNames.get(n);
					} else {
						pspElementName = INGORE_INDICATOR;
					}
				}
			} catch (NumberFormatException e) {
				System.out.println("Jo Homie. Gib eine Zahl ein...");
			}
		}
		return pspElementName;
	}

	public List<String> getSapPspElementNames(final WorkPeriod workPeriod, final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) throws IOException, TemplateException, InterruptedException {
		LOG.debug("Acquiring PSP elements...");
		final StringWriter writer = new StringWriter();
		final Template template =  getTemplateConfiguration().getTemplate("get_psp_element_names.ftl");
		final Map<String, String> simpleReplacements = getSimpleReplacements(configuration, workPeriod);
		template.process(simpleReplacements, writer);
		final String filename = UUID.randomUUID().toString();
		final File tempFile = File.createTempFile(filename, "");
		IOUtils.write(writer.toString(), new FileOutputStream(tempFile));
		final Process process = Runtime.getRuntime().exec(new String[] {
			configuration.getAutoHotkeyExecutable(),
			tempFile.getAbsolutePath()
		});
		final int processResult = process.waitFor();
		if (0 != processResult) {
			LOG.error("Process did not return successfully D:");
			return new LinkedList<>();
		}
		if (!tempFile.delete()) {
			LOG.warn("Could not delete temporary script file.");
		}
		final String processOutput = IOUtils.toString(process.getInputStream());
		return Arrays.asList(trimAll(processOutput.split("\n")));
	}

	private String[] trimAll(String[] strings) {
		final String[] trimmed = new String[strings.length];
		for (int i = 0; i < strings.length; ++i) {
			trimmed[i] = strings[i].trim();
		}
		return trimmed;
	}
	
	private Configuration getTemplateConfiguration() throws IOException {
		final Configuration templateConfiguration = new Configuration();
		templateConfiguration.setTemplateLoader(getTemplateLoader());
		return templateConfiguration;
	}

	private TemplateLoader getTemplateLoader() throws IOException {
		if (null == templateLoader) {
			LOG.debug("Loading templates...");

			templateLoader = new StringTemplateLoader();
			for (final String template : TEMPLATES) {
				templateLoader.putTemplate(template, IOUtils.toString(getClass().getResourceAsStream("/templates/" + template)));
			}
			
			LOG.debug("... done");
		}
		return templateLoader;
	}

	private Map<String, String> getSimpleReplacements(final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration, final WorkPeriod workPeriod) {
		final Map<String, String> replacements = new HashMap<>();

		replacements.put("sapExecutable", configuration.getSapExecutable());
		replacements.put("username", configuration.getUsername());
		replacements.put("password", configuration.getPassword());
		replacements.put("periodBegin", DateUtil.DATE_FORMATTER.print(workPeriod.getBegin()));

		return replacements;
	}

	@Override
	public Class<StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig> getConfigurationClass() {
		return StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig.class;
	}

}
