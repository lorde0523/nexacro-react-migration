package com.lorde0523.migration.analysis;

import com.lorde0523.migration.analysis.model.AnalysisRequest;
import com.lorde0523.migration.analysis.model.MigrationAnalysisReport;
import com.lorde0523.migration.analysis.model.MigrationUnit;
import com.lorde0523.migration.analysis.parser.NexacroContractExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyNexcoreMigrationGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    void buildsPfdMigrationUnitAndGeneratedSpringBootFiles() throws Exception {
        Path javaRoot = tempDir.resolve("legacy/src/java/z/zz/zzz/zzzz");
        Files.createDirectories(javaRoot);
        Files.writeString(javaRoot.resolve("PSample.java"), """
                package z.zz.zzz.zzzz;

                public class PSample {
                    private final FSample fSample = new FSample();

                    public Object search(Object input) {
                        return fSample.search(input);
                    }
                }
                """);
        Files.writeString(javaRoot.resolve("FSample.java"), """
                package z.zz.zzz.zzzz;

                public class FSample {
                    private final DSample dSample = new DSample();

                    public Object search(Object input) {
                        return dSample.selectSample(input);
                    }
                }
                """);
        Files.writeString(javaRoot.resolve("DSample.java"), """
                package z.zz.zzz.zzzz;

                public class DSample {
                    public Object selectSample(Object input) {
                        return getSqlMapClientTemplate().queryForList("Sample.selectSample", input);
                    }
                }
                """);

        Path dbRoot = tempDir.resolve("legacy/DB");
        Files.createDirectories(dbRoot);
        Files.writeString(dbRoot.resolve("Sample.xml"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE sqlMap PUBLIC "-//iBATIS.com//DTD SQL Map 2.0//EN" "http://www.ibatis.com/dtd/sql-map-2.dtd">
                <sqlMap namespace="Sample">
                    <select id="selectSample" parameterClass="java.util.Map" resultClass="java.util.Map">
                        SELECT SAMPLE_ID, SAMPLE_NM FROM SAMPLE WHERE SAMPLE_NM = #sampleNm#
                    </select>
                </sqlMap>
                """);

        Path outputRoot = tempDir.resolve("generated");
        AnalysisRequest request = new AnalysisRequest(
                tempDir.resolve("empty-nexacro").toString(),
                null,
                javaRoot.toString(),
                dbRoot.toString(),
                outputRoot.toString(),
                "com.example.migrated"
        );
        Files.createDirectories(tempDir.resolve("empty-nexacro"));

        MigrationAnalysisReport report = new NexacroContractExtractor().analyze(request);

        assertThat(report.migrationUnits()).hasSize(1);
        MigrationUnit unit = report.migrationUnits().get(0);
        assertThat(unit.controllerClassName()).isEqualTo("SampleController");
        assertThat(unit.serviceClassName()).isEqualTo("SampleService");
        assertThat(unit.dataAccessClassNames()).containsExactly("SampleMapper");
        assertThat(unit.methodFlows()).extracting("controllerMethodName").containsExactly("search");
        assertThat(unit.methodFlows().get(0).serviceMethodNames()).contains("search");
        assertThat(unit.methodFlows().get(0).dataMethodNames()).contains("selectSample");
        assertThat(unit.methodFlows().get(0).statementIds()).contains("Sample.selectSample");
        assertThat(unit.dataAccessDecisions()).extracting("decision").contains("MYBATIS_REQUIRED");
        assertThat(unit.warnings()).isEmpty();

        assertThat(outputRoot.resolve("src/main/java/com/example/migrated/controller/SampleController.java")).exists();
        assertThat(outputRoot.resolve("src/main/java/com/example/migrated/service/SampleService.java")).exists();
        assertThat(outputRoot.resolve("src/main/java/com/example/migrated/mapper/SampleMapper.java")).exists();
        assertThat(outputRoot.resolve("src/main/resources/mapper/SampleMapper.xml")).exists();
        assertThat(outputRoot.resolve("migration-report.md")).exists();
        assertThat(Files.readString(outputRoot.resolve("src/main/resources/mapper/SampleMapper.xml")))
                .contains("<mapper namespace=\"com.example.migrated.mapper.SampleMapper\">")
                .contains("#{sampleNm}");
    }

    @Test
    void recordsWarningWhenDStatementIdHasNoIbatisXmlMatch() throws Exception {
        Path javaRoot = tempDir.resolve("legacy/src/java/sample");
        Files.createDirectories(javaRoot);
        Files.writeString(javaRoot.resolve("PMissing.java"), """
                package sample;

                public class PMissing {
                    private final FMissing fMissing = new FMissing();
                    public Object search(Object input) {
                        return fMissing.search(input);
                    }
                }
                """);
        Files.writeString(javaRoot.resolve("FMissing.java"), """
                package sample;

                public class FMissing {
                    private final DMissing dMissing = new DMissing();
                    public Object search(Object input) {
                        return dMissing.selectMissing(input);
                    }
                }
                """);
        Files.writeString(javaRoot.resolve("DMissing.java"), """
                package sample;

                public class DMissing {
                    public Object selectMissing(Object input) {
                        return getSqlMapClientTemplate().queryForObject("Missing.selectMissing", input);
                    }
                }
                """);

        Path dbRoot = tempDir.resolve("legacy/DB");
        Files.createDirectories(dbRoot);
        Path outputRoot = tempDir.resolve("generated");
        Files.createDirectories(tempDir.resolve("empty-nexacro"));

        MigrationAnalysisReport report = new NexacroContractExtractor().analyze(new AnalysisRequest(
                tempDir.resolve("empty-nexacro").toString(),
                null,
                javaRoot.toString(),
                dbRoot.toString(),
                outputRoot.toString(),
                "com.example.migrated"
        ));

        assertThat(report.migrationUnits()).hasSize(1);
        assertThat(report.migrationUnits().get(0).warnings())
                .contains("Manual check required: iBatis statement Missing.selectMissing was referenced by DMissing.selectMissing but not found under legacyDbRoot.");
        assertThat(Files.readString(outputRoot.resolve("migration-report.md")))
                .contains("Missing.selectMissing")
                .contains("Manual check required");
    }
}
