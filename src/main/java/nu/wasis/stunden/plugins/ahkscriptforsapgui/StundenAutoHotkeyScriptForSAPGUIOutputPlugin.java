package nu.wasis.stunden.plugins.ahkscriptforsapgui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import nu.wasis.stunden.plugins.ahkscriptforsapgui.util.SAPDateUtils;
import nu.wasis.stunden.util.JsonUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import freemarker.template.TemplateException;

@PluginImplementation
public class StundenAutoHotkeyScriptForSAPGUIOutputPlugin implements OutputPlugin {

	private static final String INGORE_INDICATOR = "INGORE";

	private static final Logger LOG = Logger.getLogger(StundenAutoHotkeyScriptForSAPGUIOutputPlugin.class);

	private final SAPScriptGenerator sapScriptGenerator = new SAPScriptGenerator();
	
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
		final Map<String, String> replacements = getSimpleReplacements(configuration, workPeriod);
		final String prelude = sapScriptGenerator.getScript(AHKScriptFile.OPEN_SAP_WORKSHEET, replacements);
		final String postlude = sapScriptGenerator.getScript(AHKScriptFile.EXIT_SAP, replacements);
		final String interlude = generateInterlude(workPeriod, configuration);
		
		final String fullScript = prelude + interlude + postlude;
		
		if (null != configuration.getOutputScriptFilename()) {
			IOUtils.write(fullScript, new FileOutputStream(new File(configuration.getOutputScriptFilename())));
		}
		
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
		LOG.info("...done.");
	}

	private String generateInterlude(final WorkPeriod workPeriod, final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) throws IOException, TemplateException, InterruptedException {
		Map<String, String> projectMapping = configuration.getProjectMapping();
		if (!isProjectMappingComplete(workPeriod, projectMapping)) {
			projectMapping = createProjectMapping(workPeriod, configuration);
		}
		
		final StringBuilder interludeBuilder = new StringBuilder(300); 
		
		LOG.warn("Not implemented yet.");
		
		// V1
		// foreach covered week
		//     open week
		//     pull all available psps
		//     foreach pulled psp
		//         foreach day in current week
		//             find the entry for this psp and enter it (it will be only one entry due to the magic of the StundenSimplifierPlugin)
		
		// V2
		// foreach day in work period -> easy
		//     if date is first of month or monday -> easy
		//         open worksheet at current date -> easy
		//         pull all worksheet items -> clusterfuck but possible
		//     foreach entry of day (these will already be simplified by StundenSimplifierPlugin, so no worries)
		//         find according psp
		//         enter data to psp
		
		// V3
		// foreach day in work period -> easy
		//     if date is first of month or monday -> easy
		//         open worksheet at current date -> easy
		//     foreach entry of day (these will already be simplified by StundenSimplifierPlugin, so no worries)
		//         pull according psp -> clusterfuck, but easy
		//         enter data to psp
		//         remove psp
		
		for (final Day day : workPeriod.getDays()) {
			final DateTime currentDate = day.getDate();
			if (1 == currentDate.getDayOfMonth() || DateTimeConstants.MONDAY == currentDate.getDayOfWeek()) {
				interludeBuilder.append(sapScriptGenerator.createGotoWeekScript(currentDate));
			}
		}
		return interludeBuilder.toString();
	}
	

	private Map<String, String> createProjectMapping(final WorkPeriod workPeriod, final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) throws IOException, TemplateException, InterruptedException {
		final List<String> sapPspElementNames = getSapPspElementNames(workPeriod, configuration);
		for (final String sapPspElementName : sapPspElementNames) {
			LOG.debug("Element: `" + sapPspElementName + "'");
		}
		return createProjectsToPSPMap(workPeriod, configuration, sapPspElementNames);
	}

	private boolean isProjectMappingComplete(final WorkPeriod workPeriod, final Map<String, String> projectMapping) {
		if (null == projectMapping || projectMapping.isEmpty()) {
			return false;
		}
		if (null != projectMapping || !projectMapping.isEmpty()) {
			for (final Day day : workPeriod.getDays()) {
				for (final Entry entry : day.getEntries()) {
					if (!projectMapping.containsKey(entry.getProject().getName())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private Map<String, String> createProjectsToPSPMap(final WorkPeriod workPeriod, final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration, final List<String> availablePSPNames) throws IOException, TemplateException, InterruptedException {
		final Map<String, String> projectsToPSPMap = configuration.getProjectMapping();
		
		for (final Day day : workPeriod.getDays()) {
			for (final Entry entry : day.getEntries()) {
				final String projectName = entry.getProject().getName();
				if (null == projectsToPSPMap.get(projectName)) {
					projectsToPSPMap.put(projectName, userSelectPspElement(projectName, availablePSPNames));
				}
			}
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Project Mapping");
			LOG.debug("===============");
			for (final java.util.Map.Entry<String, String> entry : projectsToPSPMap.entrySet()) {
				LOG.debug(entry.getKey() + " => " + entry.getValue());
			}
		}
		if (configuration.getPrintProjectMapping()) {
			LOG.info("Printing project mapping:");
			System.out.println(JsonUtils.GSON.toJson(projectsToPSPMap));
		}
		
		return projectsToPSPMap;
	}

	private String userSelectPspElement(final String projectName, final List<String> availablePSPNames) throws IOException {
		String pspElementName = "";
		while(pspElementName.isEmpty()) {
			System.out.println("Projekt `" + projectName + "' ist unbekannt. Bitte wählen sie ein zu verwendendes PSP-Element.");
			for (int i = 0; i < availablePSPNames.size(); ++i) {
				final String currentPspName = availablePSPNames.get(i);
				System.out.println(i + "\t- " + currentPspName);
			}
			System.out.println(availablePSPNames.size() + "\t - [Ignorieren]");
			try {
				final int n = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
				if (n < 0 || n > availablePSPNames.size()) {
					System.out.println("Jo Homie, gib eine Zahl zwischen 0 und " + (availablePSPNames.size() - 1) + " ein.");
				} else {
					if (n < availablePSPNames.size()) {
						pspElementName = availablePSPNames.get(n);
					} else {
						pspElementName = INGORE_INDICATOR;
					}
				}
			} catch (final NumberFormatException e) {
				System.out.println("Jo Homie. Gib eine Zahl ein...");
			}
		}
		return pspElementName;
	}

	public List<String> getSapPspElementNames(final WorkPeriod workPeriod, final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration) throws IOException, TemplateException, InterruptedException {
		LOG.debug("Acquiring PSP elements...");
		final String getPspElementNamesScript = sapScriptGenerator.getScript(AHKScriptFile.GET_PSP_ELEMENT_NAMES, getSimpleReplacements(configuration, workPeriod));
		final String filename = UUID.randomUUID().toString();
		final File tempFile = File.createTempFile(filename, "");
		IOUtils.write(getPspElementNamesScript, new FileOutputStream(tempFile));
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

	private String[] trimAll(final String[] strings) {
		final String[] trimmed = new String[strings.length];
		for (int i = 0; i < strings.length; ++i) {
			trimmed[i] = strings[i].trim();
		}
		return trimmed;
	}
	
	private Map<String, String> getSimpleReplacements(final StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig configuration, final WorkPeriod workPeriod) {
		final Map<String, String> replacements = new HashMap<>();

		replacements.put("sapExecutable", configuration.getSapExecutable());
		replacements.put("username", configuration.getUsername());
		replacements.put("password", configuration.getPassword());
		replacements.put("periodBegin", SAPDateUtils.DATE_FORMATTER.print(workPeriod.getBegin()));

		return replacements;
	}

	@Override
	public Class<StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig> getConfigurationClass() {
		return StundenAutoHotkeyScriptForSAPGUIOutputPluginConfig.class;
	}

}
