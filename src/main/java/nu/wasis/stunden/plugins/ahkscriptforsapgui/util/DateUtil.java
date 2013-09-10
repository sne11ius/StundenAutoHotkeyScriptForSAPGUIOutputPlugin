package nu.wasis.stunden.plugins.ahkscriptforsapgui.util;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public interface DateUtil {
	
	DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendDayOfMonth(2).appendLiteral(".").appendMonthOfYear(2).appendLiteral(".").appendYear(4,  4).toFormatter();
	
}
