import tempfile
import unittest
from pathlib import Path

from tools.nexcore_nexacro_dataset_mapper import (
    build_matches,
    parse_bizunits,
    parse_nexacro_screens,
    write_dto_files,
    write_markdown,
)


class NexcoreNexacroDatasetMapperTest(unittest.TestCase):
    def test_matches_bizunit_id_and_generates_dto_candidates(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            nexcore = root / "nexcore"
            nexacro = root / "nexacro"
            nexcore.mkdir()
            nexacro.mkdir()

            (nexcore / "SampleBizUnit.xml").write_text(
                """
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
                """,
                encoding="utf-8",
            )
            (nexacro / "Sample.xfdl").write_text(
                """
                <FDL>
                  <Form id="Sample">
                    <Objects>
                      <Dataset id="dsSearch">
                        <ColumnInfo>
                          <Column id="SAMPLE_NM" type="STRING" size="50"/>
                          <Column id="USE_YN" type="STRING" size="1"/>
                        </ColumnInfo>
                      </Dataset>
                      <Dataset id="dsList">
                        <ColumnInfo>
                          <Column id="SAMPLE_ID" type="STRING" size="20"/>
                          <Column id="SAMPLE_NM" type="STRING" size="50"/>
                        </ColumnInfo>
                      </Dataset>
                    </Objects>
                    <Script><![CDATA[
                      this.transaction("select", "SAMPLE_SELECT", "dsSearch=dsSearch", "dsList=dsList", "", "fnCallback");
                    ]]></Script>
                  </Form>
                </FDL>
                """,
                encoding="utf-8",
            )

            methods = parse_bizunits(nexcore)
            screens = parse_nexacro_screens(nexacro)
            results = build_matches(screens, methods)

            self.assertEqual(["SAMPLE_SELECT"], list(methods.keys()))
            self.assertEqual(1, len(results))
            self.assertEqual("SampleBizUnit", results[0].bizunit_method.bizunit_id)
            self.assertEqual(
                ["SampleSelectRequest", "SampleSelectResponse"],
                [dto.class_name for dto in results[0].dto_candidates],
            )
            self.assertEqual(
                ["sampleNm", "useYn"],
                [field.java_name for field in results[0].dto_candidates[0].fields],
            )

            out = root / "out"
            write_markdown(results, out / "mapping-report.md")
            write_dto_files(results, out / "dto", "com.example.dto")

            self.assertIn("MATCHED", (out / "mapping-report.md").read_text(encoding="utf-8"))
            self.assertTrue((out / "dto" / "SampleSelectRequest.java").exists())


if __name__ == "__main__":
    unittest.main()
