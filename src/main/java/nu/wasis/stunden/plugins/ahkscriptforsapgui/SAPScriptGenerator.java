package nu.wasis.stunden.plugins.ahkscriptforsapgui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import nu.wasis.stunden.plugins.ahkscriptforsapgui.util.SAPDateUtils;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class SAPScriptGenerator {

	private static final Logger LOG = Logger.getLogger(SAPScriptGenerator.class);
	
	private StringTemplateLoader templateLoader;
	
	public String createGotoWeekScript(DateTime currentDate) throws IOException, TemplateException {
		final Map<String, String> replacements = new HashMap<>();
		replacements.put("periodBegin", SAPDateUtils.DATE_FORMATTER.print(currentDate));
		return getScript(AHKScriptFile.GOTO_WEEK, replacements);
	}
	
	public String getScript(final AHKScriptFile template, final Map<String, String> replacements) throws TemplateException, IOException {
		StringWriter writer = new StringWriter();
		getTemplateConfiguration().getTemplate(template.getFilename()).process(replacements, writer);
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
			for (final AHKScriptFile template : AHKScriptFile.values()) {
				templateLoader.putTemplate(template.getFilename(), IOUtils.toString(getClass().getResourceAsStream("/templates/" + template.getFilename())));
			}
			
			LOG.debug("... done");
		}
		return templateLoader;
	}

	
}
