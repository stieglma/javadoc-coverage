/*
 * Copyright 2017-2017 Manoel Campos da Silva Filho
 *
 * Licensed under the General Public License Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.manoelcampos.javadoc.coverage;

import com.manoelcampos.javadoc.coverage.stats.*;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.RootDoc;

import java.io.*;
import java.util.List;

/**
 * A renderer class that actually exports JavaDoc comments.
 * It is used when the {@link CoverageDoclet} is started.
 *
 * @author Manoel Campos da Silva Filho
 * @since 1.0.0
 */
public class ReportGenerator {
    private final JavaDocsStats stats;
    private final CoverageDoclet doclet;

    public ReportGenerator(final CoverageDoclet doclet) {
        this.doclet = doclet;
        this.stats = new JavaDocsStats(doclet.getRootDoc());
    }

    /**
     * Exports classes and packages JavaDocs, inside a {@link RootDoc} object.
     *
     * @return true if successful, false otherwise
     */
    public boolean start() {
        try (final PrintWriter writer = new PrintWriter(System.out)) {
            exportClassesDocStats(writer);
            exportPackagesDocStats(writer);
            writer.printf("Project Documentation Coverage: %.2f%%\n\n", stats.getDocumentedMembersPercent());
            writer.flush();
        }

        return true;
    }

    private void exportPackagesDocStats(final PrintWriter writer) {
        final PackagesDocStats packagesDocStats = stats.getPackagesDocStats();
        exportPkgsOrClassesDocStats(writer, packagesDocStats);
        packagesDocStats.getPackagesDoc().forEach(pkg -> exportPackageDocStats(pkg, writer));
        writer.println();
    }

    private void exportPkgsOrClassesDocStats(PrintWriter writer, MembersDocStats packagesDocStats) {
        writer.printf("%-26s: \t%11d Undocumented: %6d Documented: %6d (%.2f%%)\n",
                packagesDocStats.getType(), packagesDocStats.getMembersNumber(), packagesDocStats.getUndocumentedMembers(),
                packagesDocStats.getDocumentedMembers(), packagesDocStats.getDocumentedMembersPercent());
    }

    /**
     * Exports the statistics about JavaDoc coverage of a given package.
     *
     * @param doc the object containing the JavaDoc coverage data
     * @param writer the {@link PrintWriter} used to output the statistics.
     */
    private void exportPackageDocStats(final PackageDoc doc, final PrintWriter writer) {
        writer.printf("\tPackage %s. Documented: %s\n", doc.name(), Utils.isNotStringEmpty(doc.commentText()));
    }

    private void exportClassesDocStats(final PrintWriter writer) {
        final ClassesDocStats classesDocStats = stats.getClassesDocStats();
        exportPkgsOrClassesDocStats(writer, classesDocStats);

        for (final ClassDocStats classStats : stats.getClassesDocStats().getClassesList()) {
            exportClassDocStats(classStats, writer);
        }
        writer.println();
    }

    /**
     * Exports the statistics about JavaDoc coverage of a given class.
     *
     * @param classStats the object containing the JavaDoc coverage data
     * @param writer the {@link PrintWriter} used to output the statistics.
     */
    private void exportClassDocStats(final ClassDocStats classStats, final PrintWriter writer) {
        writer.printf("\t%s: %s Package: %s Documented: %s (%.2f%%)\n",
                classStats.getType(), classStats.getName(), classStats.getPackageName(),
                classStats.hasDocumentation(), classStats.getDocumentedMembersPercent());

        exportMembersDocStats(writer, classStats.getFieldsStats());
        exportMethodsDocStats(writer, classStats.getConstructorsStats());
        exportMethodsDocStats(writer, classStats.getMethodsStats());
        exportMembersDocStats(writer, classStats.getEnumsStats());
        writer.flush();
    }

    private void exportMethodsDocStats(final PrintWriter writer, final List<MethodDocStats> methodStatsList) {
        final String memberTypeFormat = "\t\t\t%-12s";
        for (final MethodDocStats methodStats : methodStatsList) {
            writer.printf("\t\t%s: %s Documented: %s (%.2f%%)\n",
                    methodStats.getType(), methodStats.getMethodName(),
                    methodStats.hasDocumentation(), methodStats.getDocumentedMembersPercent());
            exportMembersDocStats(writer, methodStats.getParamsStats(), memberTypeFormat);

            if(methodStats.getThrownExceptions().getMembersNumber() > 0) {
                exportMembersDocStats(writer, methodStats.getThrownExceptions(), memberTypeFormat);
            }
        }
    }

    private void exportMembersDocStats(final PrintWriter writer, final MembersDocStats membersDocStats) {
        exportMembersDocStats(writer, membersDocStats, "");
    }

    private void exportMembersDocStats(final PrintWriter writer, final MembersDocStats membersDocStats, String memberTypeFormat) {
        if (membersDocStats.getMembersNumber() == 0 && !membersDocStats.isPrintIfNoMembers()) {
            return;
        }

        memberTypeFormat = Utils.isStringEmpty(memberTypeFormat) ? "\t\t%-20s" : memberTypeFormat;

        final String format = memberTypeFormat+" %6d Undocumented: %6d Documented: %6d (%.2f%%) \n";
        writer.printf(format,
                membersDocStats.getType()+":", membersDocStats.getMembersNumber(),
                membersDocStats.getUndocumentedMembers(),
                membersDocStats.getDocumentedMembers(),  membersDocStats.getDocumentedMembersPercent());
    }

}
