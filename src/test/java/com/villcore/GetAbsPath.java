package com.villcore;

import org.junit.Test;

import java.io.File;

public class GetAbsPath {
    @Test
    public void getAbsPathTest() {
        File f = new File("D:/POSTGR~1/pg96/../pg96/share/postgresql/extension/postgis.control");
        System.out.println(f.getAbsolutePath().toString());
    }
}
