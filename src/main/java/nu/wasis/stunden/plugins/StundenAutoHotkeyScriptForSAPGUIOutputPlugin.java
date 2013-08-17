package nu.wasis.stunden.plugins;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import nu.wasis.stunden.model.WorkPeriod;
import nu.wasis.stunden.plugin.OutputPlugin;

@PluginImplementation
public class StundenAutoHotkeyScriptForSAPGUIOutputPlugin implements OutputPlugin {

    @Override
    public void output(final WorkPeriod workPeriod, final Object configuration) {
        System.out.println("StundenAutoHotkeyScriptForSAPGUIOutputPlugin doing it's stuff...");
    }

    @Override
    public Class<?> getConfigurationClass() {
        return null;
    }

}
