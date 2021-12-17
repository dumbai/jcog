/*
 * Copyright (C) 2015 Machine Learning Lab - University of Trieste, 
 * Italy (http:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:
 */
package jcog.grammar.evolve.postprocessing;

import jcog.grammar.evolve.configuration.Configuration;
import jcog.grammar.evolve.outputs.Results;
import jcog.io.Serials;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * extends the BasicPostProcessor but saves the result.json file. 
 * @author MaleLabTs
 */
public class JsonPostProcessor extends BasicPostprocessor {


    @Override
    public void elaborate(Configuration config, Results results, long timeTaken) {
        super.elaborate(config, results, timeTaken);
        
        System.out.println("Saving results...");

        String dateFormatted = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pathOfFile = config.getOutputFolder().getAbsolutePath() + File.separator + "results-"+dateFormatted+".json";
        saveToJson(results, pathOfFile);
        
        String time = String.format("%d h, %d m, %d s",
                TimeUnit.MILLISECONDS.toHours(timeTaken),
                TimeUnit.MILLISECONDS.toMinutes(timeTaken) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeTaken)),
                TimeUnit.MILLISECONDS.toSeconds(timeTaken) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken)));
        System.out.println("Time taken: "+time);
        
    }

    private static void saveToJson(Results results, String pathOfFile) {
        try {
            Serials.jsonMapper.writerFor(Results.class).writeValue(new File(pathOfFile), results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }

    private static void saveFile(String text, String pathOfFile) {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(pathOfFile), StandardCharsets.UTF_8);
            writer.write(text);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(JsonPostProcessor.class.getName()).log(Level.SEVERE, "Cannot save:", ex);
        }

    }
   
}
