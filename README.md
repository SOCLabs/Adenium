![Adenium: Normalizer](./img/Adenium_400x105.png)

[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://choosealicense.com/licenses/apache-2.0) [![wiki](https://img.shields.io/badge/link-wiki-ff69b4.svg)](https://github.com/SOCLabs/Adenium/wiki) [![contributing](https://img.shields.io/badge/link-CONTRIBUTING-green.svg)](https://github.com/SOCLabs/Adenium/blob/master/CONTRIBUTING.md)

# **Adenium_Normalizer**

Adenium Normalizer는 다양한 Sensor의 Event(Log)를 정규화 합니다. Regular expression 기반으로 범용적인 Tokenization method 와 사전 정의된 정규화 필드 및 사용자 정의 필드를 지원합니다. 독립 실행 구조로 설계되어 독립 실행 어플리케이션 또는 분산처리 Framework 인 Spark와 같은 특정 Framework의 한 부분으로 동작 할 수 있습니다. 

- [Adenium_Normalizer](#adenium_normalizer)
  - [Parser의 동작](#parser의-동작)
  - [Event 프로토콜](#event-프로토콜)
  - [참조 데이터](#참조-데이터)
    - [Agent](#agent)
    - [Tokenization rule](#tokenization-rule)
    - [Arrange Rule](#arrange-rule)
    - [Company IP](#company-ip)
    - [Company IP Range](#company-ip-range)
    - [Fields](#fields)
    - [Replace Fields](#replace-fields)
    - [Signatures](#signatures)
    - [GeoIP range](#geoip-range)
  - [정규화 규칙의 적용](#정규화-규칙의-적용)
    - [정규화 기능의 동작](#정규화-기능의-동작)
      - [1. 전송 프로토콜 처리](#1-전송-프로토콜-처리)
      - [2. 후보 Agent 목록 추출 및 정규화 규칙 추출](#2-후보-agent-목록-추출-및-정규화-규칙-추출)
      - [3. Parsing](#3-parsing)
      - [4. 공유장비, 공유존의 소유자 판정](#4-공유장비-공유존의-소유자-판정)
      - [5. 결과생성](#5-결과생성)
    - [Logging을 통한 정규화 과정 확인](#logging을-통한-정규화-과정-확인)
  - [Build 및 동작 환경](#build-및-동작-환경)
  - [Parser Tester](#parser-tester)
    - [Sample Dataset](#sample-dataset)
    - [Test](#test)
      - [실행 옵션](#실행-옵션)
      - [실행](#실행)
  - [정의 및 약어](#정의-및-약어)



## **Parser의 동작**

1. Syslog format 형태의 Event 를 입력 받아 Regular expression으로 Tokenize 후 정규화 한다.

2. Parser는 Tokenize에 필요한 Regular expression과 정규화 작업에 필요한 참조데이터를 필요로 한다.

3. Parser의 Default 입/출력 소스는 Std In/Out이며 사용 환경에 맞게 변경 가능하다. ( ex : data base, file, stream, kafka ..)

4. Parser는 JVM 8.0 이상이 설치된 모든 환경에서 동작 하며, 독립된 프로세스 또는 Spark와 같은 분산처리 프레임워크 상에서 동작 가능하다.

5. Parser의 실행 과정

    [![Figure_1](https://github.com/SOCLabs/Adenium_Normalizer/raw/master/img/figure_1.png)](https://github.com/SOCLabs/Adenium_Normalizer/blob/master/img/figure_1.png)

## **Event 프로토콜**

### Default 전송 프로토콜 = Syslog

정규화 대상 Event는 Syslog format의 body 형태로 전달된다. 전송 프로토콜의 처리 기능은 분리 설계 되어 있어 필요 시 별도의 프로토콜 처리 Layer을 추가 하여 변경 가능한다.

| 구분       |                            | 내용                                           |
| ---------- | ------------------------------- | ------------------------------------------ |
| **Header** | Priority                        | 이벤트를 발생시킨 Facility와 우선순위 정보 |
|            | DateTime                        | 이벤트 발생시각                            |
|            | Hostname                        | 이벤트를 발생시킨 System의 Host 정보       |
| **Body**   |                                 | **이벤트 내용 ( 정규화 대상 )**           |

## 참조 데이터

참조 데이터는 정규화 작업을 위한 정규식 과 부가 정보이며 Sensor 별 적용 할 참조 데이터는 Host name 을 기준으로 판별하며 각 항목은 TAB으로 구분한다. 기본 구분문자는 변경 가능하다.

### Agent

Agent는 특정한 "Sensor + 소유자 정보"를 포함한 개념이다. 즉, 같은 장비(Sensor)라고 하더라도 다른 소유자가 소유할 경우 다른 Agent로 본다.

| 구분           | Type    | 내용                                              |
| -------------- | ------- | ------------------------------------------------- |
| agentIp        | String  | ip 또는 Host name [syslog header : host]          |
| agentId        | Long    | Agent를 식별하는 고유 ID                          |
| companyId      | Long    | Agent Owner의 ID                                  |
| companyName    | String  | Owner 명                                          |
| companyGroupId | Long    | Owner 그룹 ID                                     |
| sensorId       | Long    | Agent에 등록된 Sensor model ID                    |
| sensor         | String  | Sensor model 명                                   |
| sensorType     | String  | Sensor model Type [ FW, WAF, DDOS, IPS, IDS ... ] |
| vendorId       | Long    | Sensor 제조사 ID                                  |
| vendorName     | String  | 제조사 명                                         |
| active         | Boolean | Agent 사용 유무                                   |

**Data sample**

```
192.168.0.1	1234	2425	SUNLEAF	77	68257	WebFront1	WF	26	PIOLINK	Y
```

### Tokenization rule

Sensor 별 Syslog body 를 Tokenization 하기 위한 Regular expression

| 구분       | Type   | 내용                    |
| ---------- | ------ | ----------------------- |
| id         | Int    | Rule을 식별하기 위한 ID |
| sensorId   | Long   | 연결된 Sensor ID        |
| sensorType | String | 연결된 Sensor Type      |
| regEx      | Regex  |                         |

**Data sample**

```
100	68257	WF src\_ip=\"(.+?)\".+src\_port=\"(.+?)\".+dest\_ip=\"(.+?)\"
```

### Arrange Rule

Tokenization 결과와 Normalization Field를 연결하기 위한 규칙

| 구분           | Type | 내용                       |
| -------------- | ---- | -------------------------- |
| tokenizeRuleId | Int  | Tokenize Rule ID           |
| captureOrder   | Int  | Regex match group sequence |
| fieldId        | Int  | 정규화 Field ID            |

**Data sample**

```
100 1 8
```

### **Company Ip**

Agent가 설치된 소유자 IP정보, 정규화 된 결과의 Agent를 최종 판정하기 위한 정보로 활용 된다.

| 구분      | Type   | 내용                    |
| --------- | ------ | ----------------------- |
| companyId | Long   | Agent 소유주의 ID       |
| publicIp  | String | Agent소유주 의 공인 IP  |
| privateIp | String | Agent 소유주 의 사설 IP |

**Data sample**

```
2425	192.168.0.1	-
```

### **Company Ip Range**

Company ip 정보와 함께 Agent를 최종 판정하기 위한 보조 정보로 활용 된다.

| 구분      | Type   | 내용              |
| --------- | ------ | ----------------- |
| companyId | Long   | Agent 소유주의 ID |
| sip       | String | 대역의 시작 Ip    |
| eip       | String | 대역의 종료 Ip    |

**Data sample**

```
 2425	192.168.0.1	192.168.1.254
```

### Fields

정규화 결과를 구성하는 필드 정의

| 구분      | Type   | 내용       |
| --------- | ------ | ---------- |
| id        | Int    | Field id   |
| fieldName | String | Feild name |

**Data sample**

```
4	Signature
```

### Replace Fields

정규화 후 결과 내용의 변경이 필요한 Field를 정의

| 구분    | Type   | 내용                            |
| ------- | ------ | ------------------------------- |
| fieldId | String | 변경 대상Field Id               |
| outstr  | String | 변경 대상 문자열                |
| instr   | String | 변경 할 문자열                  |
| vendor  | Long   | 변경 대상 조건이 되는 Vendor id |

**Data sample**

```
4	110600275	시스템 폴더 접근 취약점	26
```

### Signatures

Vendor 별 Sensor Signature 와 범주 정의

| 구분      | Type   | 내용      |
| --------- | ------ | --------- |
| vendorId  | Long   | Vendor id |
| signature | String | Signature |
| category1 | String | 구분 1    |
| category2 | String | 구분 2    |
| category3 | String | 구분 3    |
| category4 | String | 구분 4    |

**Data sample**

```
26	시스템접근 취약점	Security	Exploit	Overflow\_Buffers	Web\_Application\_Vulnerability
```

### GeoIp range

국가별 IP 범위

| 구분        | Type   | 내용                    |
| ----------- | ------ | ----------------------- |
| nationCdoe  | String | 국가 코드               |
| startIp_dec | Long   | Decimal format start Ip |
| endIp_dec   | Long   | Decimal format end Ip   |

**Data sample**

```
AU	16777216	16777471
```

## 정규화 규칙의 적용

Event 의 파싱 과 정규화 과정은 아래와 같다.

### 정규화 기능의 동작

[![Figure_2](https://github.com/SOCLabs/Adenium_Normalizer/raw/master/img/figure_2.png)](https://github.com/SOCLabs/Adenium_Normalizer/blob/master/img/figure_2.png)

#### 1. 전송 프로토콜 처리

이벤트 내용을 정규화 하기 위해, 어떤 규칙을 적용할 것인지 판별이 필요하다. 적용할 규칙 판별을 위해 host name 필드의 값을 전송프로토콜( Syslog) 헤더에서 추출한다.

#### 2. 후보 Agent 목록 추출 및 정규화 규칙 추출

host name은 참조 정보 [Agent] 의 agentIp 필드와 대응 되며 Event 에 적용할 정규화 규칙을 선택하는 기준이 된다. 후보 Agent로 표현한 이유는 하나의 Hostname을 공유하는 복수의 Agent가 존재 하기 때문이다.

#### 3. Parsing

##### 1) Multiple-Regular Expression 과 Tokenize

후보 Agent 의 SensorId에 해당 하는 모든 정규식을 Tokenize Rules 에서 선택하여 적용 한다. 동일한 Sensor에서 발생한 Event들 이라고 하더라도, 각기 다른 Regular expression으로 Tokenize 될 수 있고, host name을 공유하는 경우 복수의 후보 Agent에 연결된 sensor의 종류가 다를 수 있어 Multiple-Regular Expression 을 지원한다.

**Multiple-Regular Expression 제한 및 주의**

같은 장비에서 발생한 Event라고 하더라도, sensor version, sensor 설정 값에 의해 전송되는 Event Format이 다를 수 있으며, host name 을 공유하는 경우 (공유 장비, 공유존) 등록된 Sensor의 종류가 복수가 될 수 있다. 따라서 Parser는 선택된 정규식을 순차적으로 적용해 보고, 그중 먼저 성공한 규칙으로 Tokenize가 된다고 본다. 때문에 순차 적용되는 정규표현식이 특정한 이벤트에 대해 모두 성공적으로 Tokenize되지 않도록 주의해야 한다. 특히, **.\*** 와 같이 모든 이벤트에 성공결과를 반환하는 규칙은 적용 순서에 따라 side-effect가 발생할 가능성이 매우 높다.

##### 2) Arrange Result

Tokenize가 성공할 경우, Tokenize 된 결과를 [Arrange] 규칙을 이용하여 어떤 Token이 어떤 필드에 해당하는 지 List of Tuple ( field ID => String) 형태의 결과를 생성한다.

#### 4. 공유장비, 공유존의 소유자 판정

공유장비, 공유존 은 복수의 Sensor가 하나의 장비를 공유하여 로그를 전송 하는 경우, 등록된 1개의 Agent로 복수의 Sensor 이벤트가 유입된다. 복수의 Sensor는 각각의 소유주가 다를 수 있어 Event의 원 소유주를 판별 하여야 한다.

##### 1) Company Ip 비교

정규화 이후 agent ip 필드를 등록된 Company IP와 비교하여 매칭된 해당 Company로 결정한다.

##### 2) Company Ip Range로 비교

1의 과정에서 Company IP로 판별하지 못 하였을 경우 정규화 필드의 SrcIP, DestIP 필드를 Company Ip Range와 비교하여 매칭된 Company로 결정 한다.

##### 3) Company를 결정하지 못 하였을 경우

예를 들어 Company IP 테이블의 관리 이슈로, IP Range가 겹칠 경우, 또는 동일한 Sensor 정보를 가진 복수의 Agent가 발견 되었을 경우에는 가장 최근에 등록된 Agent를 기준으로 장비의 소유주를 결정 한다.

#### 5. 결과생성

파싱 과정이 완료되면 Tokenize 결과로 정규화 결과를 생성한다. 정규화 결과를 구성하는 필드는 정규화 Library가 어떤 의미의 필드인지를 알고 있는 지 여부에 따라 “예약필드”와 “사용자 정의 필드”로 구분되며, 이벤트에서 추출된 필드**(원본필드)**와 이벤트에서 추출한 정보를 참조로 생성한 정보필드**(파생필드)**, 원본 또는 파생필드가 특정한 조건에 해당할 경우, 치환하는 **(치환필드)**로 구분된다.

##### 1) 필드의 구분

| 구분                  | 내용                                                         |
| --------------------- | ------------------------------------------------------------ |
| 예약필드              | 해당 필드가 어떤 의미인지 미리 예약되어 있는 필드            |
| 사용자 정의필드 (UDF) | 사용자가 설정으로 정의한 필드. 사용자정의필드를 특정 이벤트의 정규화 과정에서 추출하려면, 특정 Token이 어떤 UDF-ID에 해당하는 지를 Arrange설정으로 지정한다. |
| 원본필드              | 원본 이벤트에서 추출한 그대로 사용되는 필드                  |
| 파생필드              | 원본 이벤트에서 추출한 정보를 이용하여 생성하는 필드 ex) IP를 이용한 국가명 필드 |
| 치환필드              | 특정한 조건에 해당할 경우, 지정한 값으로 치환하는 치환 필드  |

##### 2) 필드치환

정규화 된 필드가 치환 필드 정보에 정의된 특정 조건 (필드 ID, 특정 Vendor )을 만족 할 경우 Tokenize 된 문자열을 지정된 문자열로 변경한다 ( instr => outstr )

##### 3) 정규화 결과 생성

치환 과정이 완료된 전체 필드의 결과를 정규화 결과로 생성한다

##### 4) 사전 정의된 필드

| **구분**              | **내용**                          |      |
| --------------------- | --------------------------------- | ---- |
| **CATEGORY1**         | 대 분류                           | 파생 |
| **CATEGORY2**         | 중 분류                           | 파생 |
| **CATEGORY3**         | 소 분류                           | 파생 |
| **SIGNATURE**         | Sensor 별 정의된 이벤트 Signature | 파생 |
| **SEVERITY**          | 심각도                            | 원본 |
| **COUNT**             | 공격 카운트                       | 원본 |
| **REPEATCOUNT**       | 공격 반복 카운트                  | 원본 |
| **SRCIP**             | 공격지 IP Address                 | 원본 |
| **SRCPORT**           | 공격지 PORT                       | 원본 |
| **SRCMAC**            | 공격지 MAC Address                | 원본 |
| **SRCCOUNTRY**        | 공격지 국가코드                   | 파생 |
| **DESTIP**            | 목적지 IP                         | 원본 |
| **DESTPORT**          | 목적지 PORT                       | 원본 |
| **DESTMAC**           | 목적지 MAC Address                | 원본 |
| **DESTCOUNTRY**       | 목적지 국가코드                   | 파생 |
| **SRCDIRECTION**      | 공격지 탐지 방향                  | 파생 |
| **DESTDIRECTION**     | 목적지 탐지 방향                  | 파생 |
| **URL**               | URL                               | 원본 |
| **URI**               | URI                               | 원본 |
| **URIPARAMS**         | URI Parameter                     | 원본 |
| **HEADER**            | HTTP Header                       | 원본 |
| **PROTOCOL**          | 프로토콜                          | 원본 |
| **PAYLOAD**           | Payload                           | 원본 |
| **CODE**              | Sensor 의 상태 코드               | 원본 |
| **RCVDBYTES**         | 수신 Bytes 카운트                 | 원본 |
| **SENTBYTES**         | 전송 Bytes 카운트                 | 원본 |
| **MESSAGEID**         | 메시지 ID                         | 원본 |
| **SRCZONE**           | SRC Zone                          | 원본 |
| **DESTZONE**          | DEST Zone                         | 원본 |
| **SERVICE**           | Service                           | 원본 |
| **DURATION**          | Duration                          | 원본 |
| **ACLNM**             | ACL Name                          | 원본 |
| **ACTION**            | Allow/Deny, IPS Action            | 파생 |
| **RAWDATA**           | Raw Data                          | 원본 |
| **SENDER**            | 발신자                            | 원본 |
| **ATTACHMENT**        | 첨부 파일명                       | 원본 |
| **START ATTACK TIME** | 공격 시작 시간                    | 원본 |
| **END ATTACK TIME**   | 공격 종료 시간                    | 원본 |
| **LOGTIME**           | 로그 수집 시간                    | 원본 |
| **SYSLOGTIME**        | Syslog 발생 시간                  | 원본 |
| **SYSLOGHOST**        | System이 발생시킨 Syslog Host     | 원본 |
| **AGENTID**           | Agent ID                          | 파생 |
| **AGENTIP**           | Agent IP                          | 파생 |
| **COMPANYID**         | 소유자(고객사) ID                 | 파생 |
| **COMPANYNM**         | 소유자 명                         | 파생 |
| **COMPANYGROUPID**    | 소유자 그룹 ID                    | 파생 |
| **DEVICETYPE**        | Sensor Type                       | 파생 |
| **DEVICEMODEL**       | Sensor Model 명                   | 파생 |
| **VENDOR**            | Sensor Vendor name                | 파생 |

### Logging을 통한 정규화 과정 확인

Regular Expression 기반 정규화 모듈은 설정 기반으로 동작하므로, 설정 값의 변경에 따라 각기 다른 정규화 결과가 나오게 된다. Library 사용환경에서 원하는 (또는 기대하지 않은) 정규화 결과가 어떤 과정을 통해 나오게 된 것인지 단계별로 추적하는 기능을 제공한다.

정규화 Library는 정규화 과정의 기록을 순차적으로 기록한 로그를 설정에 따라 on/off 할 수 있다. Logging Option을 enable할 경우, 정규화 과정의 기록을 순차적으로 포함하는 List of String을 정규화의 결과와 함께 반환한다.

## Build 및 동작 환경

**Maven 빌드**

```
build/mvn -clean package
```

**Library dependencies**

- scala 2.11.5
- log4j 1.2.17

**동작환경**

Java SE Runtime Environment 8 l

## **Parser Tester**

정규화 과정을 테스트 할 수 있는 별도의 Tool을 제공한다. Test tool은 console 환경에서 동작 가능한 Utility이며 단독 실행 가능한 Jar 패키지로 제공된다. 정규화 과정에 필요한 참조 데이터는 파일로 제공되며, 로그는 Std-In으로 입력 받으며 정규화 결과는 Std-Out으로 출력 된다.

**Test tool의 동작 과정**

[![img](https://github.com/SOCLabs/Adenium_Normalizer/raw/master/img/figure_3.png)](https://github.com/SOCLabs/Adenium_Normalizer/blob/master/img/figure_3.png)

### Sample Dataset

각각의 참조 정보 파일은 Tab으로 구분된 text 파일이며 1 행은 항목의 Header 이다. 파일의 인코딩 은 UTF-8 형식 이다. 샘플로그는 하나의 라인이 하나의 로그이며 파일의 인코딩 은 UTF-8 형식 이다.

**Samples :** 테스트용 샘플 Event log

**RefData :** 정규화 참조 데이터

- **agentInfo.ref : ** Agent 정보
- **arrangeRules.ref :** Tokenize 결과를 Normalization Field로 변경하는 규칙
- **companyIpRange.ref : ** 소유주의 IP 범위
- **companyServerIp.ref : ** 소유주의 IP 정보
- **fields.ref : ** 정규화 필드
- **geoIpRange.ref : ** 국가 IP Band
- **replaceFields.ref : ** 변경 필드
- **signatures.ref : ** Sensor Signature
- **tokenizeRules.ref : ** 정규식

### Test

테스트 프로그램은 실행 시 “|” (파이프) 를 이용하여 Event 로그를 전달하거나, 프로그램 실행 후 Console에서 직접 입력이 가능하다.

#### 실행 옵션

**-path [ File path ]** : 지정한 경로에서 참조 정보를 로드 한다. Default : ../resources

**-logon** : 별도의 옵션 값 없이 옵션만 선언하여 로그 기능을 활성화 한다. –logon 옵션을 입력하지 않으면 로그는 출력하지 않는다.

#### 실행

1. **Run**

   실행 후 Event 로그를 입력 하거나, 실행 시 "|"를 통해 Event 로그를 전달 할 수 있다.

   ```
   > java -jar AdeniumParser.jar -path Resource\RefData
   ```

   ```
   > cat sample.log | java -jar AdeniumParser.jar -path Resource\RefData
   ```

2. **로그 입력**

   Event 로그 없이 실행 하였을 경우, Command에 로그를 입력하면 정규화를 시작한다.

   ```
   > java -jar AdeniumParser.jar -path Resource\RefData
   Ref Files From : Resource\RefData\
   <44>Aug  7 17:49:53 192.169.0.1 (warning) kernel: [WEBFRONT/0x00726001] Violated SQL Injection - the form field isn't allowed. (log_id="2085090068",app_name="07_purunetedu",app_id="7",src_if="waf",src_ip="116.46.237.195",src_port="52453",dest_ip="211.169.244.10",dest_port="80",forwarded_for="",hos
   t="www.purunetedu.com",url="/mystudyroom/questionbank/classlistAjax.prn",sig_warning="Middle",url_param="",block="no",evidence_id="1910044145",owasp="A1",field="subjCd",sigid="110600275",data="0-0-0")
   ```

3. **정규화**

   정규화 결과는 아래와 같이 adenium Header 이후 Tab 으로 구분된 key, value 형태로 출력 된다.

   ```
   adenium 17      OUT     16      OUT     15      KR      11      KR      40      7 Aug 17:49:53  41  211.169.244.2       6       1       7       1       47      WF      48      WebFront K2400  49      PIOLINK 42      12346776        46      77      44      2425    45      SUNLEAF 4       시스템 폴더
   접근 취약점     8       116.46.237.195  18      www.purunetedu.com      12      211.169.244.10  13  80  9       52453   33      no
   ```

4. **종료**

   Blank line 입력 또는 q, quit, bye, exit 입력

5. Logging

   -logon 옵션을 정의하면 정규화 과정의 로그를 확인 할 수 있다.

   ```
   > java -jar AdeniumParser.jar -path Resource\RefData -logon
   ```

   ```
   [ State ] ========== parser.execute : success ? true
    fields log[ Syslog: RFC3164 ] Header3164(44,Aug,7,17:49:53,211.169.244.2)
   [ SOCDeviceTypeHint ] 211.169.244.2
   [ filterAgents ] host ( normal : 211.169.244.2 )
   [ lookupDeviceRules ] ( type, name) = (1234568257,WF)
   [ tryTokenizeRules ] (WF,1234568257) in (WF,1234568257)
   [ findArrangeRule ] 100
   [ arrangeResult ] : (1,8,Some(116.46.237.195))
   [ arrangeResult ] : (2,9,Some(52453))
   [ arrangeResult ] : (3,12,Some(211.169.244.10))
   [ arrangeResult ] : (4,13,Some(80))
   [ arrangeResult ] : (5,18,Some(www.purunetedu.com))
   [ arrangeResult ] : (6,33,Some(no))
   [ arrangeResult ] : (7,4,Some(110600275))
   [ decideAgent ] agent = (2425,SUNLEAF,PIOLINK)
   + makeField: (Some(action),no)
   + makeField: (Some(srcPort),52453)
   + makeField: (Some(destPort),80)
   + makeField: (Some(destIp),211.169.244.10)
   + makeField: (Some(url),www.purunetedu.com)
   + makeField: (Some(srcIp),116.46.237.195)
   + makeField: (Some(signature),110600275)
   + makeField: (Some(companyNm),SUNLEAF)
   + makeField: (Some(companyId),2425)
   + makeField: (Some(companyGroupId),77)
   + makeField: (Some(AgentId),12346776)
   + makeField: (Some(vendor),PIOLINK)
   + makeField: (Some(deviceModel),WebFront K2400)
   + makeField: (Some(deviceType),WF)
   + makeField: (Some(RepeatCount),1)
   + makeField: (Some(count),1)
   + makeField: (Some(SyslogHost),211.169.244.2)
   + makeField: (Some(SyslogTime),7 Aug 17:49:53)
   makeField: Some(srcCountry)
   makeField: Some(destCountry)
   makeField: Some(srcDirection)
   makeField: Some(destDirection)
   + makeField: (Some(signature),시스템 폴더 접근 취약점)
   added: (destDirection,Some(OUT))
   added: (srcDirection,Some(OUT))
   added: (destCountry,Some(KR))
   added: (srcCountry,Some(KR))
   added: (SyslogTime,Some(7 Aug 17:49:53))
   added: (SyslogHost,Some(211.169.244.2))
   added: (count,Some(1))
   added: (RepeatCount,Some(1))
   added: (deviceType,Some(WF))
   added: (deviceModel,Some(WebFront K2400))
   added: (vendor,Some(PIOLINK))
   added: (AgentId,Some(12346776))
   added: (companyGroupId,Some(77))
   added: (companyId,Some(2425))
   added: (companyNm,Some(SUNLEAF))
   changed: (signature,Some(시스템 폴더 접근 취약점))
   added: (signature,Some(110600275))
   added: (srcIp,Some(116.46.237.195))
   added: (url,Some(www.purunetedu.com))
   added: (destIp,Some(211.169.244.10))
   added: (destPort,Some(80))
   added: (srcPort,Some(52453))
   added: (action,Some(no)) ==========
   ```

   **Log Message**

   \- **[ State ]** **:** 정규화 성공 / 실패

   \- **[ SocDeviceTypeHint ] :** Syslog header의 hostname

   \- **[ filterAgents ] :** hostname으로 등록된 Agent의 검색 결과

   \- **[ lookcupDeviceRules ] :** Tokenize에 적용 할 정규식 ID 와 Sensor type

   \- **[ tryTokenizeRules ] :** Tokenize에 적용한 정규식

   \- **[ findArrangeRules ] :** Tokenize 결과를 정규화 필드로 Mapping 하는 규칙의 ID

   \- **[arrangeResult ] :** Mapping 결과 < token order, 정규화 필드 id, value>

   \- **[decideAgent ] :** 최종 결정된 소유주와 Agent 정보

   \- **+ makeField :** 변경 필드가 적용 되기 전 생성된 정규화 필드

   \- **added :** 변경되지 않은 필드

   \- **change :** 변경된 필드

## 정의 및 약어

- **이벤트(Event)** : 일반적으로, 특정 시각에 발생한 사건 내지 상태의 변화를 의미하는데, 정의 상 ‘시각’정보를 필수적으로 포함하는 특징이 있다. 어떤 사건의 기록을 의미하는 log (시각 정보의 필연성은 완화됨)와 유사한 의미를 가지면, 본 문서상에서 이벤트(event)와 로그(log)는 의미의 혼동이 없을 경우 혼용하여 사용한다. 단, 프로그램/라이브러리의 동작을 기록하는 경우도 log(내지 logging)로 표현하니 주의하기 바란다
- **파서(Parser) :** 일반적으로 문자열을 의미있는 토큰(token)으로 분해하고 이들로 이루어진 파스 트리(parse tree)를 만드는 과정을 말한다.
- **정규화(Normalization ) :** 어떤 대상을 일정한 규칙이나 기준에 따르는 ‘정규적인’ 상태로 바꾸거나, 비정상적인 대상을 정상적으로 되돌리는 과정을 말하는데, 본 문서에서 정규화는 Parsing 과정과 Parse tree의 해석과정을 의미하는데, 의미상 혼동이 없을 경우 Parsing과 Normalization은 혼용하여 사용하도록 한다.
- **Sensor :** 이벤트를 만들어낸 S/W 또는 장치
- **Agent :** 일반적으로는 사용자의 개입 없이 주기적으로 정보를 모으거나 또는 일부 다른 서비스를 수행하는 프로그램을 의미하는 데, **본 문서에서 Agent는 일반적인 의미와 다르게 사용되니 주의**하기 바란다. 본 문서에서 Agent는 “특정한 Sensor + 소유자 정보”를 포함한 개념이다. 즉, 같은 장비(Sensor)라고 하더라도 다른 소유자가 소유할 경우 다른 Agent로 본다.
