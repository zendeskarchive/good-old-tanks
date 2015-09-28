package com.getbase.hackkrk.tanks.server;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;

import java.io.IOException;
import java.text.DecimalFormat;

public class CustomPointSerializer extends JsonSerializer<Point> {
    private static final String PATTERN = "0.##";
    private ThreadLocal<DecimalFormat> formatter = ThreadLocal.withInitial(() -> new DecimalFormat(PATTERN));
    
    @Override
    public void serialize(Point value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        if (value == null) {
            jgen.writeNull();
        } else {
            jgen.writeStartObject();
            jgen.writeFieldName("x");
            jgen.writeNumber(formatDouble(value.getX()));
            jgen.writeFieldName("y");
            jgen.writeNumber(formatDouble(value.getY()));
            jgen.writeEndObject();
        }
    }

    private String formatDouble(double x) {
        final String format = formatter.get().format(x);
        if (format.equals("-0")) {
            return "0";
        }

        return format;
    }

}
