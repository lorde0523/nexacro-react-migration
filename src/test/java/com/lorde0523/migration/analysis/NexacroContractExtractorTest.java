package com.lorde0523.migration.analysis;

import com.lorde0523.migration.analysis.model.MigrationAnalysisReport;
import com.lorde0523.migration.analysis.model.ScreenAnalysis;
import com.lorde0523.migration.analysis.parser.NexacroContractExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class NexacroContractExtractorTest {

    @TempDir
    Path tempDir;

    @Test
    void extractsDatasetsTransactionsAndSearchParametersFromXfdlAndXjs() throws Exception {
        Path screen = tempDir.resolve("sample/UserList.xfdl");
        Files.createDirectories(screen.getParent());
        Files.writeString(screen, """
                <?xml version="1.0" encoding="utf-8"?>
                <FDL>
                  <Form id="UserList">
                    <Objects>
                      <Dataset id="dsSearch">
                        <ColumnInfo>
                          <Column id="USER_NM" type="STRING" size="50"/>
                          <Column id="USE_YN" type="STRING" size="1"/>
                        </ColumnInfo>
                      </Dataset>
                      <Dataset id="dsUserList">
                        <ColumnInfo>
                          <Column id="USER_ID" type="STRING" size="20"/>
                          <Column id="USER_NM" type="STRING" size="50"/>
                        </ColumnInfo>
                      </Dataset>
                    </Objects>
                    <Script><![CDATA[
                      this.dsSearch.setColumn(0, "USER_NM", this.edtUserNm.value);
                      this.transaction("searchUsers", "/user/search.do", "dsSearch=dsSearch", "dsUserList=dsUserList", "USE_YN=" + this.cboUseYn.value, "fnCallback");
                    ]]></Script>
                  </Form>
                </FDL>
                """);

        Path script = tempDir.resolve("sample/UserList.xjs");
        Files.writeString(script, """
                this.dsSearch.setColumn(0, "USE_YN", this.cboUseYn.value);
                var name = this.dsSearch.getColumn(0, "USER_NM");
                """);

        MigrationAnalysisReport report = new NexacroContractExtractor().analyze(tempDir, null);

        assertThat(report.screens()).hasSize(1);
        ScreenAnalysis result = report.screens().get(0);
        assertThat(result.screenId()).isEqualTo("UserList");
        assertThat(result.datasets()).extracting("name").containsExactlyInAnyOrder("dsSearch", "dsUserList");
        assertThat(result.transactions()).hasSize(1);
        assertThat(result.transactions().get(0).name()).isEqualTo("searchUsers");
        assertThat(result.transactions().get(0).serviceUrl()).isEqualTo("/user/search.do");
        assertThat(result.transactions().get(0).inputDatasets()).containsEntry("dsSearch", "dsSearch");
        assertThat(result.transactions().get(0).outputDatasets()).containsEntry("dsUserList", "dsUserList");
        assertThat(result.searchParameters()).extracting("name").contains("USER_NM", "USE_YN");
        assertThat(result.endpointCandidates()).extracting("path").contains("/api/user/search");
    }

    @Test
    void matchesNexcoreTransactionIdsAndBuildsDtoCandidatesFromDatasets() throws Exception {
        Path nexacro = tempDir.resolve("nexacro/sample/Sample.xfdl");
        Files.createDirectories(nexacro.getParent());
        Files.writeString(nexacro, """
                <?xml version="1.0" encoding="utf-8"?>
                <FDL>
                  <Form id="Sample">
                    <Objects>
                      <Dataset id="dsSearch">
                        <ColumnInfo>
                          <Column id="SAMPLE_NM" type="STRING" size="50"/>
                          <Column id="USE_YN" type="STRING" size="1"/>
                        </ColumnInfo>
                      </Dataset>
                      <Dataset id="dsSampleList">
                        <ColumnInfo>
                          <Column id="SAMPLE_ID" type="STRING" size="20"/>
                          <Column id="SAMPLE_NM" type="STRING" size="50"/>
                        </ColumnInfo>
                      </Dataset>
                    </Objects>
                    <Script><![CDATA[
                      this.transaction("select", "SAMPLE_SELECT", "dsSearch=dsSearch", "dsSampleList=dsSampleList", "", "fnCallback");
                    ]]></Script>
                  </Form>
                </FDL>
                """);

        Path nexcore = tempDir.resolve("legacy/SampleBizUnit.xml");
        Files.createDirectories(nexcore.getParent());
        Files.writeString(nexcore, """
                <bizUnit id="SampleBizUnit">
                    <bizUnitName>샘플업무</bizUnitName>
                    <componentId>sampleComponent</componentId>
                    <method-list>
                        <method>
                            <methodId>select</methodId>
                            <methodName>조회</methodName>
                            <transactionId>SAMPLE_SELECT</transactionId>
                        </method>
                    </method-list>
                </bizUnit>
                """);

        MigrationAnalysisReport report = new NexacroContractExtractor()
                .analyze(tempDir.resolve("nexacro"), tempDir.resolve("legacy"));

        ScreenAnalysis screen = report.screens().get(0);
        assertThat(screen.serviceMatches()).hasSize(1);
        assertThat(screen.serviceMatches().get(0).transactionId()).isEqualTo("SAMPLE_SELECT");
        assertThat(screen.serviceMatches().get(0).matched()).isTrue();
        assertThat(screen.serviceMatches().get(0).bizUnitId()).isEqualTo("SampleBizUnit");
        assertThat(screen.dtoCandidates()).extracting("name")
                .contains("SampleSelectRequest", "SampleSelectResponse");
        assertThat(screen.dtoCandidates())
                .filteredOn(candidate -> candidate.name().equals("SampleSelectRequest"))
                .first()
                .extracting("fields")
                .satisfies(fields -> assertThat(fields.toString()).contains("sampleNm", "useYn"));
    }

    @Test
    void resolvesTransactionArgumentsStoredInVariables() throws Exception {
        Path nexacro = tempDir.resolve("nexacro/sample/VariableService.xfdl");
        Files.createDirectories(nexacro.getParent());
        Files.writeString(nexacro, """
                <?xml version="1.0" encoding="utf-8"?>
                <FDL>
                  <Form id="VariableService">
                    <Objects>
                      <Dataset id="dsSearch">
                        <ColumnInfo>
                          <Column id="SAMPLE_NM" type="STRING" size="50"/>
                        </ColumnInfo>
                      </Dataset>
                      <Dataset id="dsSampleList">
                        <ColumnInfo>
                          <Column id="SAMPLE_ID" type="STRING" size="20"/>
                        </ColumnInfo>
                      </Dataset>
                    </Objects>
                    <Script><![CDATA[
                      var transactionName = "select";
                      var serviceId = "SAMPLE_SELECT";
                      var inputDatasets = "dsSearch=dsSearch";
                      var outputDatasets = "dsSampleList=dsSampleList";
                      this.transaction(transactionName, serviceId, inputDatasets, outputDatasets, "", "fnCallback");
                    ]]></Script>
                  </Form>
                </FDL>
                """);

        Path nexcore = tempDir.resolve("legacy/SampleBizUnit.xml");
        Files.createDirectories(nexcore.getParent());
        Files.writeString(nexcore, """
                <bizUnit id="SampleBizUnit">
                    <componentId>sampleComponent</componentId>
                    <method-list>
                        <method>
                            <methodId>select</methodId>
                            <transactionId>SAMPLE_SELECT</transactionId>
                        </method>
                    </method-list>
                </bizUnit>
                """);

        MigrationAnalysisReport report = new NexacroContractExtractor()
                .analyze(tempDir.resolve("nexacro"), tempDir.resolve("legacy"));

        ScreenAnalysis screen = report.screens().get(0);
        assertThat(screen.transactions()).hasSize(1);
        assertThat(screen.transactions().get(0).name()).isEqualTo("select");
        assertThat(screen.transactions().get(0).serviceUrl()).isEqualTo("SAMPLE_SELECT");
        assertThat(screen.transactions().get(0).inputDatasets()).containsEntry("dsSearch", "dsSearch");
        assertThat(screen.transactions().get(0).outputDatasets()).containsEntry("dsSampleList", "dsSampleList");
        assertThat(screen.serviceMatches().get(0).matched()).isTrue();
        assertThat(screen.dtoCandidates()).extracting("name")
                .contains("VariableServiceSelectRequest", "VariableServiceSelectResponse");
    }
}
