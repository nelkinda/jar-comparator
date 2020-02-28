package com.nelkinda.tools.jarcomp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class JarWalker {
    private static final String LAST = "\uFFFF";
    static final Comparator<JarWalker> cmp = comparing(JarWalker::getName);
    final JarFile jarFile;
    final List<JarEntry> entries;
    private int i;

    public JarWalker(final JarFile jarFile) {
        this.jarFile = jarFile;
        this.entries = sortedEntryList(jarFile);
    }

    private static List<JarEntry> sortedEntryList(final JarFile jarFile) {
        return jarFile.stream().sorted(comparing(JarEntry::getName)).collect(toList());
    }

    boolean hasNext() {
        return i < entries.size();
    }

    void consumeOne(final DiffResult result) {
        assert (i < entries.size());
        result.onlyIn(jarFile, entries.get(i++));
    }

    byte[] readAllBytes() throws IOException {
        return jarFile.getInputStream(entries.get(i)).readAllBytes();
    }

    void diffEntry(final JarWalker jarWalker, final DiffResult diffResult) throws IOException {
        final byte[] content1 = readAllBytes();
        final byte[] content2 = jarWalker.readAllBytes();
        if (!Arrays.equals(content1, content2)) {
            diffResult.differs(entries.get(i).getName());
        }
        i++;
        jarWalker.i++;
    }

    private String getName() {
        return hasNext() ? entries.get(i).getName() : LAST;
    }
}
