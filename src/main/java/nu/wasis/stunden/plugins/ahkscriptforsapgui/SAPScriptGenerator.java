package nu.wasis.stunden.plugins.ahkscriptforsapgui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class SAPScriptGenerator {

	private static final Logger LOG = Logger.getLogger(SAPScriptGenerator.class);
	
	private static final List<String> TEMPLATES = Arrays.asList(
        "close_sap_worksheet.ftl",
		"enter_work_hours.ftl",
		"exit_sap.ftl",
		"fill_psp.ftl",
		"get_psp_element_names.ftl",
		"goto_week.ftl",
		"open_sap_worksheet.ftl",
		"pull_all_psps.ftl",
		"run_sap.ftl"
	);
	
	private StringTemplateLoader templateLoader;
	
	public String getScript(final String templateName, final Map<String, String> replacements) throws TemplateException, IOException {
		StringWriter writer = new StringWriter();
		getTemplateConfiguration().getTemplate(templateName).process(replacements, writer);
		return writer.toString();
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

	
}
