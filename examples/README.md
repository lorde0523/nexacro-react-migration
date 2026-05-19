# 예시 파일 구조

이 폴더는 Python 사이드 분석 도구가 읽는 Nexacro/Nexcore 입력 형식을 보여주는 샘플입니다.

```text
examples/
  nexacro/
    sample/
      Sample.xfdl
      Sample.xjs
  nexcore/
    biz/
      SampleBizUnit.xml
```

## 매칭 기준

Nexcore BizUnit에는 아래처럼 `transactionId`가 있습니다.

```xml
<method>
    <methodId>select</methodId>
    <transactionId>SAMPLE_SELECT</transactionId>
</method>
```

Nexacro 화면에서는 transaction 두 번째 인자에 같은 ID를 사용합니다.

```javascript
this.transaction(
  "select",
  "SAMPLE_SELECT",
  "dsSearch=dsSearch",
  "dsSampleList=dsSampleList",
  "",
  "fnCallback"
);
```

이 경우 분석 도구는 `SAMPLE_SELECT`를 매칭하고 아래 DTO 후보를 만듭니다.

```text
SampleSelectRequest  <- dsSearch
SampleSelectResponse <- dsSampleList
```

## 실행 예시

```bash
python tools/nexcore_nexacro_dataset_mapper.py \
  --nexacro-root examples/nexacro \
  --nexcore-root examples/nexcore \
  --out build/example-analysis \
  --write-dtos
```

생성 결과:

```text
build/example-analysis/
  mapping-report.md
  mapping-report.csv
  dto/
    SampleSelectRequest.java
    SampleSelectResponse.java
    SampleInsertRequest.java
    SampleInsertResponse.java
```
