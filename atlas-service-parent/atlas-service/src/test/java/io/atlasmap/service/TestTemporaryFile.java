package io.atlasmap.service;

import java.io.File;
import java.io.IOException;

public class TestTemporaryFile {

    public static void main(String[] args) throws IOException {
        String prefix = "foobar";
        String suffix = ".tmp";
        
        // by calling deleteOnExit the temp file is deleted when the jvm is 
        // shut down
        File tempFile2 = File.createTempFile(prefix, suffix);
        tempFile2.deleteOnExit();
        
        
        
        System.out.format("Canonical filename: %s\n", tempFile2.getCanonicalFile());
        tempFile2.delete();
    }
}
