package com.nelkinda.tools.jarcomp;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import static com.nelkinda.tools.jarcomp.JarWalker.cmp;

public enum JarComp {
    ;

    public static void main(final String[] args) throws IOException {
        final DiffResult diffResult = jarFilesEqual(new File(args[0]), new File(args[1]));
        for (final String difference : diffResult.differences)
            System.err.println(difference);
        if (diffResult.hasDifferences())
            System.exit(1);
    }

    static DiffResult jarFilesEqual(final File file1, final File file2) throws IOException {
        final DiffResult diffResult = new DiffResult();
        final JarWalker jarWalker1 = new JarWalker(new JarFile(file1));
        final JarWalker jarWalker2 = new JarWalker(new JarFile(file2));
        while (jarWalker1.hasNext() || jarWalker2.hasNext()) {
            final int comparisonResult = cmp.compare(jarWalker1, jarWalker2);
            if (comparisonResult == 0) jarWalker1.diffEntry(jarWalker2, diffResult);
            else if (comparisonResult < 0) jarWalker1.consumeOne(diffResult);
            else if (comparisonResult > 0) jarWalker2.consumeOne(diffResult);
            else assert false;
        }
        return diffResult;
    }
}
