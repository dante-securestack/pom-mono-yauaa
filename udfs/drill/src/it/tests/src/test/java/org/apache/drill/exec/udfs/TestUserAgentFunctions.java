/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.drill.exec.udfs;

import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.apache.drill.categories.SqlFunctionTest;
import org.apache.drill.categories.UnlikelyTest;
import org.apache.drill.common.expression.ExpressionStringBuilder;
import org.apache.drill.exec.util.JsonStringHashMap;
import org.apache.drill.exec.util.Text;
import org.apache.drill.test.ClusterFixture;
import org.apache.drill.test.ClusterFixtureBuilder;
import org.apache.drill.test.ClusterTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.apache.drill.test.TestBuilder.parsePath;

@Category({UnlikelyTest.class, SqlFunctionTest.class})
public class TestUserAgentFunctions extends ClusterTest {

    @BeforeClass
    public static void setup() throws Exception {
        ClusterFixtureBuilder builder = ClusterFixture.builder(dirTestWatcher);
        startCluster(builder);
    }

    @Test
    public void testParseUserAgentString() throws Exception {
        String query =
            "SELECT t1.ua.DeviceClass                     AS DeviceClass," +
            "       t1.ua.DeviceName                      AS DeviceName," +
            "       t1.ua.DeviceBrand                     AS DeviceBrand," +
            "       t1.ua.DeviceCpuBits                   AS DeviceCpuBits," +
            "       t1.ua.OperatingSystemClass            AS OperatingSystemClass," +
            "       t1.ua.OperatingSystemName             AS OperatingSystemName," +
            "       t1.ua.OperatingSystemVersion          AS OperatingSystemVersion," +
            "       t1.ua.OperatingSystemVersionMajor     AS OperatingSystemVersionMajor," +
            "       t1.ua.OperatingSystemNameVersion      AS OperatingSystemNameVersion," +
            "       t1.ua.OperatingSystemNameVersionMajor AS OperatingSystemNameVersionMajor," +
            "       t1.ua.LayoutEngineClass               AS LayoutEngineClass," +
            "       t1.ua.LayoutEngineName                AS LayoutEngineName," +
            "       t1.ua.LayoutEngineVersion             AS LayoutEngineVersion," +
            "       t1.ua.LayoutEngineVersionMajor        AS LayoutEngineVersionMajor," +
            "       t1.ua.LayoutEngineNameVersion         AS LayoutEngineNameVersion," +
            "       t1.ua.LayoutEngineBuild               AS LayoutEngineBuild," +
            "       t1.ua.AgentClass                      AS AgentClass," +
            "       t1.ua.AgentName                       AS AgentName," +
            "       t1.ua.AgentVersion                    AS AgentVersion," +
            "       t1.ua.AgentVersionMajor               AS AgentVersionMajor," +
            "       t1.ua.AgentNameVersionMajor           AS AgentNameVersionMajor," +
            "       t1.ua.AgentLanguage                   AS AgentLanguage," +
            "       t1.ua.AgentLanguageCode               AS AgentLanguageCode," +
            "       t1.ua.AgentSecurity                   AS AgentSecurity " +
            "FROM (" +
            "    SELECT parse_user_agent('Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11') AS ua" +
            "    FROM (values(1))" +
            ") AS t1";

        testBuilder()
            .sqlQuery(query)
            .unOrdered()
            .baselineColumns(
                "DeviceClass",
                "DeviceName",
                "DeviceBrand",
                "DeviceCpuBits",
                "OperatingSystemClass",
                "OperatingSystemName",
                "OperatingSystemVersion",
                "OperatingSystemVersionMajor",
                "OperatingSystemNameVersion",
                "OperatingSystemNameVersionMajor",
                "LayoutEngineClass",
                "LayoutEngineName",
                "LayoutEngineVersion",
                "LayoutEngineVersionMajor",
                "LayoutEngineNameVersion",
                "LayoutEngineBuild",
                "AgentClass",
                "AgentName",
                "AgentVersion",
                "AgentVersionMajor",
                "AgentNameVersionMajor",
                "AgentLanguage",
                "AgentLanguageCode",
                "AgentSecurity")
            .baselineValues(
                "Desktop",
                "Desktop",
                "Unknown",
                "32",
                "Desktop",
                "Windows NT",
                "XP",
                "XP",
                "Windows XP",
                "Windows XP",
                "Browser",
                "Gecko",
                "1.8.1.11",
                "1",
                "Gecko 1.8.1.11",
                "20071127",
                "Browser",
                "Firefox",
                "2.0.0.11",
                "2",
                "Firefox 2",
                "English (United States)",
                "en-us",
                "Strong security"
            )
            .go();
    }

    @Test
    public void testValidFieldName() throws Exception {
        String query =
            "SELECT parse_user_agent('Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11', 'AgentSecurity') AS agent " +
            "FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues("Strong security")
            .go();
    }

    @Test
    public void testEmptyFieldName() throws Exception {
        String query =
            "SELECT parse_user_agent('Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11', '') AS agent " +
            "FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues("Unknown")
            .go();
    }

    @Test
    public void testBadFieldName() throws Exception {
        String query =
            "SELECT parse_user_agent('Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11', 'NoSuchField') AS agent " +
            "FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues("Unknown")
            .go();
    }

    @Test
    public void testNullUserAgent() throws Exception {
        // If a null value is provided then the UserAgentAnalyzer will classify this as a Hacker because all requests normally have a User-Agent.
        UserAgentAnalyzer analyzer = UserAgentAnalyzer.newBuilder().showMinimalVersion().dropTests().immediateInitialization().build();
        Map<String, String> expected = analyzer.parse((String)null).toMap(analyzer.getAllPossibleFieldNamesSorted());

        Map<String, Text> expectedRecord = new TreeMap<>();
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            expectedRecord.put(entry.getKey(), new Text(entry.getValue()));
        }

        String query = "SELECT parse_user_agent(CAST(null as VARCHAR)) AS agent FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues(expectedRecord)
            .go();
    }

    @Test
    public void testEmptyUAStringAndFieldName() throws Exception {
        String query = "SELECT parse_user_agent('', '') AS agent FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues("Unknown")
            .go();
    }

    @Test
    public void testNullUAStringAndEmptyFieldName() throws Exception {
        String query = "SELECT parse_user_agent(CAST(null as VARCHAR), '') AS agent FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues((String) null)
            .go();
    }

    @Test
    public void testNullUAStringAndBadFieldName() throws Exception {
        String query = "SELECT parse_user_agent(CAST(null as VARCHAR), 'NoSuchField') AS agent FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues((String) null)
            .go();
    }

    @Test
    public void testNullUAStringAndNullFieldName() throws Exception {
        String query = "SELECT parse_user_agent(CAST(null as VARCHAR), CAST(null as VARCHAR)) AS agent FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues((String) null)
            .go();
    }

    @Test
    public void testNullUAStringAndFieldName() throws Exception {
        String query = "SELECT parse_user_agent(CAST(null as VARCHAR), 'AgentSecurity') AS agent FROM (values(1))";
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("agent")
            .baselineValues((String) null)
            .go();
    }

    @Test
    public void testEmptyUAString() throws Exception {
        String query = "SELECT t1.ua.AgentName AS AgentName FROM (SELECT parse_user_agent('') AS ua FROM (values(1))) as t1";

        // If the UA string is empty, all returned fields default to "Hacker"
        testBuilder()
            .sqlQuery(query)
            .ordered()
            .baselineColumns("AgentName")
            .baselineValues("Hacker")
            .go();
    }


    @Test
    public void testClientHints() throws Exception {
        String query =
            "SELECT " +
            "   t1.ua.DeviceClass                               AS DeviceClass,\n" +
            "   t1.ua.DeviceName                                AS DeviceName,\n" +
            "   t1.ua.DeviceBrand                               AS DeviceBrand,\n" +
            "   t1.ua.DeviceCpu                                 AS DeviceCpu,\n" +
            "   t1.ua.DeviceCpuBits                             AS DeviceCpuBits,\n" +
            "   t1.ua.OperatingSystemClass                      AS OperatingSystemClass,\n" +
            "   t1.ua.OperatingSystemName                       AS OperatingSystemName,\n" +
            "   t1.ua.OperatingSystemVersion                    AS OperatingSystemVersion,\n" +
            "   t1.ua.OperatingSystemVersionMajor               AS OperatingSystemVersionMajor,\n" +
            "   t1.ua.OperatingSystemNameVersion                AS OperatingSystemNameVersion,\n" +
            "   t1.ua.OperatingSystemNameVersionMajor           AS OperatingSystemNameVersionMajor,\n" +
            "   t1.ua.LayoutEngineClass                         AS LayoutEngineClass,\n" +
            "   t1.ua.LayoutEngineName                          AS LayoutEngineName,\n" +
            "   t1.ua.LayoutEngineVersion                       AS LayoutEngineVersion,\n" +
            "   t1.ua.LayoutEngineVersionMajor                  AS LayoutEngineVersionMajor,\n" +
            "   t1.ua.LayoutEngineNameVersion                   AS LayoutEngineNameVersion,\n" +
            "   t1.ua.LayoutEngineNameVersionMajor              AS LayoutEngineNameVersionMajor,\n" +
            "   t1.ua.AgentClass                                AS AgentClass,\n" +
            "   t1.ua.AgentName                                 AS AgentName,\n" +
            "   t1.ua.AgentVersion                              AS AgentVersion,\n" +
            "   t1.ua.AgentVersionMajor                         AS AgentVersionMajor,\n" +
            "   t1.ua.AgentNameVersion                          AS AgentNameVersion,\n" +
            "   t1.ua.AgentNameVersionMajor                     AS AgentNameVersionMajor\n" +
            "FROM (" +
            "   SELECT" +
            "       parse_user_agent(" +
            "           'User-Agent',                   'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36'," +
            "           'Sec-Ch-Ua',                    '\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"100\", \"Google Chrome\";v=\"100\"'," +
            "           'Sec-Ch-Ua-Arch',               '\"x86\"'," +
            "           'Sec-Ch-Ua-Bitness',            '\"64\"'," +
            "           'Sec-Ch-Ua-Full-Version',       '\"100.0.4896.127\"'," +
            "           'Sec-Ch-Ua-Full-Version-List',  '\" Not A;Brand\";v=\"99.0.0.0\", \"Chromium\";v=\"100.0.4896.127\", \"Google Chrome\";v=\"100.0.4896.127\"'," +
            "           'Sec-Ch-Ua-Mobile',             '?0'," +
            "           'Sec-Ch-Ua-Model',              '\"\"'," +
            "           'Sec-Ch-Ua-Platform',           '\"Linux\"'," +
            "           'Sec-Ch-Ua-Platform-Version',   '\"5.13.0\"'," +
            "           'Sec-Ch-Ua-Wow64',              '?0'" +
            "       ) AS ua " +
            "   FROM (values(1))" +
            ") AS t1";

        testBuilder()
            .sqlQuery(query)
            .unOrdered()
            .baselineColumns(
                "DeviceClass",
                "DeviceName",
                "DeviceBrand",
                "DeviceCpu",
                "DeviceCpuBits",
                "OperatingSystemClass",
                "OperatingSystemName",
                "OperatingSystemVersion",
                "OperatingSystemVersionMajor",
                "OperatingSystemNameVersion",
                "OperatingSystemNameVersionMajor",
                "LayoutEngineClass",
                "LayoutEngineName",
                "LayoutEngineVersion",
                "LayoutEngineVersionMajor",
                "LayoutEngineNameVersion",
                "LayoutEngineNameVersionMajor",
                "AgentClass",
                "AgentName",
                "AgentVersion",
                "AgentVersionMajor",
                "AgentNameVersion",
                "AgentNameVersionMajor"
            )
            .baselineValues(
                "Desktop",
                "Linux Desktop",
                "Unknown",
                "Intel x86_64",
                "64",
                "Desktop",
                "Linux",
                "5.13.0",
                "5",
                "Linux 5.13.0",
                "Linux 5",
                "Browser",
                "Blink",
                "100.0",
                "100",
                "Blink 100.0",
                "Blink 100",
                "Browser",
                "Chrome",
                "100.0.4896.127",
                "100",
                "Chrome 100.0.4896.127",
                "Chrome 100"
            )
            .go();
    }

    // ====================================================================

    @Test
    public void testEmptyClientHints() throws Exception {
        String query =
            "SELECT " +
            "   t1.ua.DeviceClass                               AS DeviceClass,\n" +
            "   t1.ua.DeviceName                                AS DeviceName,\n" +
            "   t1.ua.DeviceBrand                               AS DeviceBrand,\n" +
            "   t1.ua.DeviceCpu                                 AS DeviceCpu,\n" +
            "   t1.ua.DeviceCpuBits                             AS DeviceCpuBits,\n" +
            "   t1.ua.OperatingSystemClass                      AS OperatingSystemClass,\n" +
            "   t1.ua.OperatingSystemName                       AS OperatingSystemName,\n" +
            "   t1.ua.OperatingSystemVersion                    AS OperatingSystemVersion,\n" +
            "   t1.ua.OperatingSystemVersionMajor               AS OperatingSystemVersionMajor,\n" +
            "   t1.ua.OperatingSystemNameVersion                AS OperatingSystemNameVersion,\n" +
            "   t1.ua.OperatingSystemNameVersionMajor           AS OperatingSystemNameVersionMajor,\n" +
            "   t1.ua.LayoutEngineClass                         AS LayoutEngineClass,\n" +
            "   t1.ua.LayoutEngineName                          AS LayoutEngineName,\n" +
            "   t1.ua.LayoutEngineVersion                       AS LayoutEngineVersion,\n" +
            "   t1.ua.LayoutEngineVersionMajor                  AS LayoutEngineVersionMajor,\n" +
            "   t1.ua.LayoutEngineNameVersion                   AS LayoutEngineNameVersion,\n" +
            "   t1.ua.LayoutEngineNameVersionMajor              AS LayoutEngineNameVersionMajor,\n" +
            "   t1.ua.AgentClass                                AS AgentClass,\n" +
            "   t1.ua.AgentName                                 AS AgentName,\n" +
            "   t1.ua.AgentVersion                              AS AgentVersion,\n" +
            "   t1.ua.AgentVersionMajor                         AS AgentVersionMajor,\n" +
            "   t1.ua.AgentNameVersion                          AS AgentNameVersion,\n" +
            "   t1.ua.AgentNameVersionMajor                     AS AgentNameVersionMajor\n" +
            "FROM (" +
            "   SELECT" +
            "       parse_user_agent(" +
            // NOTE: Here we do NOT say "User-Agent" --> It is just the first one in the list.
            "           'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.127 Safari/537.36'," +
            "           'Sec-Ch-Ua',                    ''," +
            "           'Sec-Ch-Ua-Arch',               ''," +
            "           'Sec-Ch-Ua-Bitness',            ''," +
            "           'Sec-Ch-Ua-Full-Version',       ''," +
            "           'Sec-Ch-Ua-Full-Version-List',  ''," +
            "           'Sec-Ch-Ua-Mobile',             ''," +
            "           'Sec-Ch-Ua-Model',              ''," +
            "           'Sec-Ch-Ua-Platform',           ''," +
            "           'Sec-Ch-Ua-Platform-Version',   ''," +
            "           'Sec-Ch-Ua-Wow64',              ''" +
            "       ) AS ua " +
            "   FROM (values(1))" +
            ") AS t1";

        testBuilder()
            .sqlQuery(query)
            .unOrdered()
            .baselineColumns(
                "DeviceClass",
                "DeviceName",
                "DeviceBrand",
                "DeviceCpu",
                "DeviceCpuBits",
                "OperatingSystemClass",
                "OperatingSystemName",
                "OperatingSystemVersion",
                "OperatingSystemVersionMajor",
                "OperatingSystemNameVersion",
                "OperatingSystemNameVersionMajor",
                "LayoutEngineClass",
                "LayoutEngineName",
                "LayoutEngineVersion",
                "LayoutEngineVersionMajor",
                "LayoutEngineNameVersion",
                "LayoutEngineNameVersionMajor",
                "AgentClass",
                "AgentName",
                "AgentVersion",
                "AgentVersionMajor",
                "AgentNameVersion",
                "AgentNameVersionMajor"
            )
            .baselineValues(
                "Desktop",
                "Linux Desktop",
                "Unknown",
                "Intel x86_64",
                "64",
                "Desktop",
                "Linux",
                "??",
                "??",
                "Linux ??",
                "Linux ??",
                "Browser",
                "Blink",
                "100.0",
                "100",
                "Blink 100.0",
                "Blink 100",
                "Browser",
                "Chrome",
                "100.0.4896.127",
                "100",
                "Chrome 100.0.4896.127",
                "Chrome 100"
            )
            .go();
    }

}
