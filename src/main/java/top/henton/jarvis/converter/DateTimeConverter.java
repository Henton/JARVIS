package top.henton.jarvis.converter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import picocli.CommandLine.ITypeConverter;

public class DateTimeConverter implements ITypeConverter<Long> {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Long convert(String value) throws Exception {
        DateTime dateTime = dateTimeFormatter.parseDateTime(value);
        return dateTime.getMillis();
    }
}
