package com.nelkinda.tools.jarcomp;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.String.format;

class DiffResult {
    final List<String> differences = new ArrayList<>();

    boolean hasDifferences() {
        return !differences.isEmpty();
    }

    void differs(final String entryName) {
        differences.add(format("Jar entry differs: %s", entryName));
    }

    void onlyIn(final String jarFile, final String jarEntry) {
        differences.add(format("Only in %s: %s", jarFile, jarEntry));
    }

    void onlyIn(final JarFile jarFile, final JarEntry jarEntry) {
        onlyIn(jarFile.getName(), jarEntry.getName());
    }
}
