package com.nelkinda.tools.jarcomp;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JarCompTest {
    public static File createJarFile(final String name, final Map<String, byte[]> entries) throws IOException {
        final File file = File.createTempFile(name, ".jar");
        file.deleteOnExit();
        try (
                final OutputStream base = new FileOutputStream(file);
                final JarOutputStream out = new JarOutputStream(base)
        ) {
            for (final Map.Entry<String, byte[]> entry : entries.entrySet()) {
                final JarEntry jarEntry = new JarEntry(entry.getKey());
                out.putNextEntry(jarEntry);
                out.write(entry.getValue());
            }
        }
        return file;
    }

    private static <T> void assertContains(final Collection<? super T> collection, final T element) {
        assertTrue(collection.contains(element));
    }

    @Test
    void twoEmptyJarFilesAreEqual() throws IOException {
        final File empty = createJarFile("empty1", Map.of());
        final DiffResult diffResult = JarComp.jarFilesEqual(empty, empty);
        assertFalse(diffResult.hasDifferences());
        assertTrue(diffResult.differences.isEmpty());
    }

    @Test
    void emptyJarFileIsNotEqualToNonEmptyJarFile() throws IOException {
        final File empty = createJarFile("empty", Map.of());
        final File single = createJarFile("single", Map.of("empty", new byte[0]));
        {
            final DiffResult diffResult = JarComp.jarFilesEqual(empty, single);
            assertTrue(diffResult.hasDifferences());
            assertContains(diffResult.differences, format("Only in %s: %s", single, "empty"));
        }
        {
            final DiffResult diffResult = JarComp.jarFilesEqual(single, empty);
            assertTrue(diffResult.hasDifferences());
            assertContains(diffResult.differences, format("Only in %s: %s", single, "empty"));
        }
    }

    @Test
    void equalJarFilesWithSingleEntry() throws IOException {
        final File single = createJarFile("single", Map.of("hello", "Hello, world!".getBytes()));
        final DiffResult diffResult = JarComp.jarFilesEqual(single, single);
        assertFalse(diffResult.hasDifferences());
        assertTrue(diffResult.differences.isEmpty());
    }

    @Test
    void unequalJarFilesWithSingleEntry() throws IOException {
        final File single1 = createJarFile("single1", Map.of("hello", "Hello, world!".getBytes()));
        final File single2 = createJarFile("single2", Map.of("hello", "Hallo, Welt!".getBytes()));
        final DiffResult diffResult = JarComp.jarFilesEqual(single1, single2);
        assertTrue(diffResult.hasDifferences());
        assertContains(diffResult.differences, "Jar entry differs: hello");
    }

    @Test
    void unequalJarFilesWithUnequalEntry() throws IOException {
        final File single1 = createJarFile("single1", Map.of("empty", new byte[0]));
        final File single2 = createJarFile("single2", Map.of("leer", new byte[0]));
        final DiffResult diffResult = JarComp.jarFilesEqual(single1, single2);
        assertTrue(diffResult.hasDifferences());
        assertContains(diffResult.differences, format("Only in %s: %s", single1, "empty"));
        assertContains(diffResult.differences, format("Only in %s: %s", single2, "leer"));
    }

    @Test
    void testOnlyIn() {
        final JarFile jarFile = mock(JarFile.class);
        when(jarFile.getName()).thenReturn("foo");
        final DiffResult diffResult = new DiffResult();
        diffResult.onlyIn(jarFile, new JarEntry("bar"));
        assertEquals("Only in foo: bar", diffResult.differences.get(0));
    }

    @Test
    void testDiffers() {
        final DiffResult diffResult = new DiffResult();
        diffResult.differs("hello");
        assertEquals("Jar entry differs: hello", diffResult.differences.get(0));
    }
}
