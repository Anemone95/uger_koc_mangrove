package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;

import edu.umd.cs.mangrove.util.BugReport;
import edu.umd.cs.mangrove.util.BugReportUtil;
import edu.umd.cs.mangrove.util.DirectoryUtil;
import edu.umd.cs.mangrove.util.PrintUtil;
import edu.umd.cs.mangrove.util.TaintConfigUtil;

public class ComputeOwaspFeatureVecs {

	private static final String SOURCE_BASE = "/Users/ukoc/workspace-bench/owasp-1.1/src/main/java/";
	private static final String classFileDir = "org/owasp/benchmark/testcode/";
	//static HashMap<String, BugReport> reportMap;

	static final String xmlFile = DirectoryUtil.PROJECT_DIR + "data/findsecbugs/reports/owasp_sqli_reports.xml";
	static final String outDir = DirectoryUtil.PROJECT_DIR + "data/";

	public static void main(final String[] args) throws IOException, ClassHierarchyException, UnsoundGraphException,
			CancelException, ParserConfigurationException, SAXException {

		//reportMap = BugReportUtil.parseXML2Map(new File(xmlFile));
		HashMap<String, Integer> fileStats = BugReportUtil.parseXMLForStats(new File(xmlFile));

		String metadata = "@relation feature_data\n" + "\n" + "@attribute rname {SQL_INJECTION_JDBC}\n"
				+ "@attribute sinkline numeric\n"
				+ "@attribute sinkIdentifier {Connection.prepareStatement,Statement.execute,Statement.executeUpdate,Connection.prepareCall,Statement.addBatch,Statement.executeQuery}\n"
				+ "@attribute sourceIdentifier {cookie.getValue,request.getHeader,request.getHeaderNames,request.getHeaders,request.getParameter,request.getParameterMap,request.getParameterNames,scr.getTheParameter,request.getParameterValues,request.getQueryString,scr.getTheValue,'new sun.misc.BASE64Decoder',thing.doSomething,'new Test',doSomething}\n"
				+ "@attribute witnessLenght numeric\n" + "@attribute numClassesInvolved numeric\n"
				+ "@attribute rank numeric\n" + "@attribute priority numeric\n" + "@attribute sourceLine numeric\n"
				+ "@attribute severity numeric\n" + "@attribute confidence {HighConfidence,NormalConfidence}\n"
				+ "@attribute numBugs {1,null}\n" + "@attribute functions numeric\n" + "@attribute conditions numeric\n"
				+ "@attribute time numeric\n" + "@attribute label {truepositive,falsepositive}\n" + "\n" + "@data\n";

		String wFileName = "data/findsecbugs/reports/CWE89OwaspStatsAll.txt";
		ArrayList<String> warnings = (ArrayList<String>) Files.readAllLines(Paths.get(wFileName));

		String[] train_outFile = { "owasp-train-1.arff", "owasp-train-2.arff", "owasp-train-3.arff", "owasp-train-4.arff",
				"owasp-train-5.arff" };
		String[] test_outFile = { "owasp-test-1.arff", "owasp-test-2.arff", "owasp-test-3.arff", "owasp-test-4.arff",
				"owasp-test-5.arff" };

		String[] testProgs = {
				"02800,09037,09656,16618,08426,02166,08962,16603,10950,05263,09662,10290,00187,07747,02175,02737,04017,07758,08361,02217,04057,09700,02779,09058,07736,07112,20959,04104,08444,14058,08439,03377,12234,15327,08432,04664,05857,02803,08368,05881,04088,05332,09016,13475,21023,16602,10278,08434,16548,00927,13482,21003,09668,04655,17226,03448,08443,14069,05861,20927,01562,04100,04023,14663,19067,01549,00887,14679,02209,01500,07731,02187,17859,07151,08428,04052,03457,09646,00846,17789,02770,08366,04629,08985,12859,21001,02833,05329,14062,11503,12828,20979,14729,07107,17865,02748,02809,19660,07798,06565,02752,05943,21027,19671,04641,02122,08387,04095,08400,09697,18425,17778,14046,00892,00850,04692,03456,07734,07154,07816,05287,06530,04707,02816,08422,05322,07168,07181,06510,11518,09072,07177,04654,20941,10269,08388,19719,02799,19066,16537,11493,00052,02738,12863,02828,06463,17772,16542,16539,06484,01484,20353,08384,05855,09692,02213,04065,10248,21010,08991,01547,02735,09063,09006,00874,08953,20931,01541,14103,02768,04106,05293,09009,19020,04078,09050,08406,17211,09648,05931,20313,14067,04674,00843,02841,14118,01470,02769,16628,08403,04105,05899,08410,02139,14040,20380,09053,02823,14077,08997,10285,14102,04094,10282,07762,16627,00849,06529,14035,14086,14083,17209,20991,07192,06557,06537,04661,14039,02160,20356,07814,05323,08392,01483,06569,06508,02211,07807,12852,10904,14127,05936,04727,05246,15894,01548,14130,14107,10272,20914,04633,07775,01468,01499,09713,15895,10872,06504,12809,17866,02786,10854,15359,10935,08448,01545,00889,21011,06461,04694,06538,05898,14734,05274,02830,12806,20302,05299,01520,02781,07766,02846,04071,01522,12191,14674,01551,03438,20981,17178,07187,00864,01478,00844,15912,05315,17847,17771,17192,09699,06564,07174,00054,20944,09051,11555,00904,02817,03432,04043,08975,05915,00901,14100,09666,14071,09004,10208,05895,03385,07742,05327,07799,16621,04108,20932,10260,04726,18414,02176,14038,20938,08397,14051,10250,01580,10221,06544,13406,06526,01479,01576,01477,19728,20939,12250,16577,17867,12782,09660,09698,13494,05863,04636,08951,09067,05916,06555,06502,10233,12858,05297,07165,09048,00116,03472,02838,02762,09631,05944,14120,00903,03392,21014,09707,08386,06540,07115,01511,04686,05298,03397,08373,19692,02178,06472,02835,14123,05334,09695,12179,04042,14028,01507,07730,00027,05286,06524,05896,04068,04090,02802,04670,04679,08950,07129,00852,02207,09069,00151,09052,03434,19091,14050,10283,07743,09044,03386,10234,07105,11528,01587,18464,04676,04703,00217,08993,05912,07152,09684,14076,20303,07801,20954,04721,07796,02789,20921,15331,17819,14763,02764,02814,06518,17202,14106,04086,00112,05874,00891,20338,02845,07726,07746,01572,19068,07182,03387,20950,03403,09624,01481,14054,04728,07130,05893,18401,07797,03415,10244,00933,09669,03381,07733,06507,02145,11576,00156,09693,09686,01555,03388,04673,03465,01556",
				"00079,04062,10246,00859,02736,08971,05917,02827,07163,00291,15952,07785,02155,12176,02185,05910,01490,07793,10236,04644,02134,05258,01512,08425,00916,04690,10243,12799,08396,04026,01561,10268,03471,21007,07195,02188,01540,06457,10297,09711,10942,05900,06456,01559,09029,00851,05326,20964,07173,21015,04708,15275,14119,00241,08447,12846,07777,07800,06490,14052,08442,17868,01558,02751,11527,06503,05319,05920,05891,02843,19022,07769,19045,09065,00881,19703,05271,09638,09673,20929,08964,10909,04045,05310,02854,01557,07811,04109,17810,09021,02167,10847,00894,07131,06551,09720,03469,10214,14115,20973,02158,02201,02195,07172,07753,00879,10284,01554,09043,04718,02128,03422,05886,14768,00053,02840,05245,10920,14742,07141,05243,05903,02829,08961,02778,14717,02792,06522,00858,00931,02758,05914,08370,16612,21009,03450,05321,02796,03447,03476,02782,04715,03429,00878,07767,00055,07128,01578,01537,02205,06531,15927,20381,20984,04709,05291,09625,02791,12766,02806,01487,09023,07114,04099,01472,05884,01586,20320,09714,02186,17841,01467,06459,19034,02190,08998,02767,08385,05250,10289,07720,08424,14741,08960,08965,00924,05239,09717,08983,08411,02131,01503,12156,19055,05856,12157,10298,04049,05908,17201,19743,05296,07725,00922,03394,20378,01502,06533,03418,09653,08377,05331,11552,05284,09687,09623,12790,09074,05924,00853,05252,05945,02750,20395,01574,04653,14065,01523,07155,21017,07781,04032,00915,20347,14043,07109,18475,07126,03400,10871,09685,02219,09633,04091,07804,15907,09028,16568,14111,04701,10207,09696,11587,08996,03427,13463,05312,01491,13490,00930,03408,07142,09018,21000,09694,02855,05301,14110,06497,13421,07113,03436,05854,20925,10288,08990,08391,01518,05892,00912,05879,14059,07784,01584,08427,05306,03404,14048,15305,09039,08399,18411,08436,07164,04716,02824,00920,19720,02776,06470,10225,14723,09008,09629,03454,16570,19046,07116,16578,12772,08979,19667,09664,09650,13495,01492,07166,19097,03416,03399,07137,07787,16566,08417,20948,13444,05940,15313,17827,06501,19757,16574,02808,09010,02121,02798,10252,02179,15274,07139,04031,08441,04101,01475,00082,02192,04028,17853,09068,19025,17172,05872,07805,14681,04671,00876,05292,19758,07133,16561,10929,09040,10217,16543,14740,02174,04113,02161,01510,02839,06563,17189,20995,03461,14766,05923,07739,07146,06455,10231,05882,04720,10267,08378,20962,07120,01543,07783,21022,12820,10245,10238,01494,14753,17217,09073,02744,14709,02173,21004,09632,07741,06523,09682,08450,06560,00848,10856,03430,20945,09042,15932,04635,15906,05248,02165,04085,04711,07744,20379,15287,00293,01566,02120,08963,02182,07810,17856,05330,10218,00243,14720,03407,05280,01560,10925,08984,21006,08423,07119,08440,13452,20958,00267,07124,02774,07175,04675,11508,01489,13411,02151,09716,04695,06558,05309,20315,05254,07813,02793,07101,06517,09675,12845,04058,05921,10242,03382,01482,07127,02775,10212,06556,06546",
				"10270,05868,14070,07773,11490,04096,19085,07732,08956,10247,09705,05241,02805,03417,03441,04122,09054,06570,02822,04668,16589,04084,07180,20976,14089,04117,04060,05880,07724,04067,05285,09634,04055,04704,06500,00244,10917,17803,02194,14128,07779,05325,14044,01496,08974,17243,02761,07806,04046,00917,13459,12167,07723,20909,02216,04685,07778,09719,07757,05276,04650,04114,06492,17773,08398,01571,05333,20911,00189,09721,07132,03446,07123,05244,06460,13496,06464,04024,04066,16583,17188,05283,06454,14113,01493,00155,01542,02785,00157,09710,04037,02794,09038,07788,10219,08363,15942,02197,03449,00324,09683,14109,12217,01565,09659,08382,15982,09703,10265,09627,12800,00899,19759,06488,02757,10222,03475,04634,18447,07117,10230,00842,00869,11516,11520,15921,08988,00905,02756,05939,01577,11542,01514,12181,20997,14767,02753,09663,04053,07815,10227,01529,08959,14126,03401,02180,19659,07765,07780,02153,00885,01527,15270,04038,15316,20930,14022,09672,05294,06494,03423,11578,04682,05278,17796,20968,09005,18436,12210,01582,10229,07722,08995,04699,01476,02189,11586,00218,05256,04638,10934,20998,10910,05897,05279,06548,01504,09047,09637,20996,14732,04688,14068,14037,20299,14066,16614,01508,20972,08987,02788,02169,01579,10286,20374,20992,04087,14099,06480,09676,04051,14092,09722,09709,09011,10215,06493,15963,00839,12786,08978,04018,10308,00152,01530,03413,00216,05311,05911,04681,03395,05913,08446,06506,10889,07774,04022,06476,04677,04098,03426,00932,08418,07170,00029,14072,19662,17197,20980,02138,02765,12816,04706,05257,09060,00908,04029,04056,02745,05887,00867,06471,01501,12148,04672,02159,03473,09036,06553,05317,04048,09635,02815,16615,08952,09003,10884,10241,09059,04039,19739,00322,14075,10253,08390,04639,15361,10276,16617,10897,09041,14117,08967,06536,04070,20928,04628,15339,03468,04723,12162,05922,07156,07183,05273,14112,08414,03459,04027,07738,09000,19737,04121,19071,02206,10220,04632,20314,00884,02130,08976,00925,02759,19079,07169,05889,05295,00873,00325,05270,07135,02133,08389,08958,08375,18455,05328,06481,17835,14116,02162,07145,13457,06467,04019,01528,12197,06567,08431,03458,12183,08437,13419,02215,10232,15309,06550,07721,05877,02172,01573,06462,17815,04034,12798,21026,07794,04693,07150,09022,08362,15360,04118,03402,01536,10880,06516,19756,20986,06465,17191,07750,02157,02127,07770,07143,03384,03466,04103,09702,05930,00266,06535,20956,04645,11558,14081,09026,05934,12844,02772,13478,13434,14085,02212,03439,07771,13468,04637,08429,05878,20326,08380,05249,08407,05264,09033,00154,01506,02836,07728,19724,07148,20982,06487,08412,06527,12815,09032,20978,05302,14055,09645,20398,04072,07153,10291,14090,00114,08452,20999,14124,09640,10893,09644,05251,08977,09670,14056,07198,02140,12206,02834,17832,01513,21021,14094,02850,12201,02754,03462,00856,04073,19021,08972,05320,03406,10262,09647,06483,10951,05240,03470,05290",
				"00883,04696,10259,09718,04111,01505,11526,12244,04657,04077,09678,05269,09643,10239,09061,00030,00292,05268,07790,05927,02181,01550,07167,14721,00913,04116,05946,07795,07149,09665,20974,09002,07791,18405,20951,02148,14053,09012,04030,06489,04630,20989,08383,09651,04649,09626,04698,02154,01517,07178,21005,13462,15286,12166,20957,05933,02196,04719,06552,06549,16596,01531,06561,12192,04036,04729,10258,04054,14061,20988,20298,00321,20960,02214,17799,06539,04683,03393,18471,04717,04680,10299,08420,10275,08381,03428,01532,12783,07803,01564,07718,05242,14701,02146,18463,21013,08409,03460,01474,14082,11533,21020,11496,12155,10235,20386,14093,10296,04700,03444,03452,07193,02790,20936,04076,05942,01553,14095,10857,05260,10898,05918,08374,03453,19106,08364,06486,19702,05905,07763,02132,01563,05873,07104,20966,12847,15970,08376,20372,04665,17173,19713,17234,02826,00115,02210,05307,06559,15310,04035,20990,20345,14080,20949,05907,09667,06498,17788,06513,08416,07110,07184,17870,00855,09024,04656,20940,09035,10261,02733,21012,07179,14108,01498,14673,07185,03442,02749,08405,05867,01569,10256,01526,03421,06519,06479,00240,02837,04080,20952,18429,04666,10292,14041,00841,09679,00188,01525,04691,14728,20947,16562,14757,19081,08957,12223,02191,07186,03391,02746,06528,16611,07729,01480,01567,09677,02842,07144,10237,06491,03477,06458,00929,09652,03380,09071,01521,00907,09704,19109,05259,05304,02851,00026,00854,11553,14104,09642,07147,08955,14664,16536,08969,04059,05904,04020,01469,06473,01519,08981,00898,10300,06469,02198,03437,12226,07768,06511,01539,09013,07136,05265,10209,20942,20295,06485,09706,19102,15971,05272,00153,19040,09654,20985,00918,10930,14036,18419,10273,02734,05860,08419,17225,07118,07121,00083,06512,08982,09661,15951,17791,10263,09688,05864,20364,05324,21008,09062,05318,04107,07122,12182,08430,14042,06509,14045,07171,00868,02125,12791,09019,00845,02856,04115,20307,10293,21025,14047,04061,10254,02168,05875,05941,09007,17242,01583,05262,04081,20390,02193,02813,00906,02147,11571,01534,08394,08970,10228,07188,07159,13394,07756,02804,00888,19058,06482,21016,02136,06496,09671,01471,08393,09701,03390,14694,04702,07792,02739,03467,19015,06566,20943,05928,09715,03379,00242,16613,00910,21002,15304,06532,02755,17198,00863,11535,04643,04025,09027,09636,21018,17804,19712,04663,02780,06515,07138,20933,04722,05888,09015,08999,02747,02200,08973,04033,01570,09708,07776,10224,12147,08966,02143,04640,09031,14091,14756,08408,02773,01533,10257,07111,08404,20965,07157,14064,07158,04660,05871,08367,08992,14131,00909,07095,02126,21019,02204,20373,03433,00080,05858,03383,09641,05282,07161,02163,09034,11515,02220,03414,20333,01515,00326,03409,02787,09020,08435,09649,02740,04705,15292,02170,08968,14682,15977,07716,06468,21024,09049,05885,19077,07160,04667,05247,02149,02849,09057,14073,05919,14060,07125,07749,07764,06478,02144",
				"00877,14125,20923,09066,19726,02766,12196,19663,15893,00902,10908,19711,05314,02825,01575,07809,04093,14129,07745,06543,04648,01488,14759,09680,02819,04092,18465,02771,07786,04044,02123,04097,10864,20934,04041,00928,00840,03440,14088,04725,10266,07772,20319,05926,06562,10287,02142,07727,00921,08379,01524,07197,12773,05275,04631,13405,01581,02818,14688,15911,08949,14747,09712,00890,05267,14087,10249,04083,20969,19693,09064,00895,20963,08372,07782,08994,05255,08365,02743,20924,10295,15937,02141,08433,06525,09055,13404,04089,06475,04069,07812,05303,14101,01473,03424,02844,08438,03420,02742,02820,08415,20946,19084,18448,02832,05859,02763,11566,06514,01486,19078,05901,07103,08413,10210,17782,12242,09639,10264,15902,08395,00862,02811,00882,09691,00028,08451,06534,11577,02150,02857,00866,03405,10213,15308,10211,20977,00893,09658,02810,13400,10271,08989,00871,03451,11510,11584,14049,01495,14114,00078,09628,08449,20935,06495,02783,18439,09657,13413,20970,10216,05281,09030,00880,20967,10240,16547,05876,08421,14712,05261,02812,04074,00872,14716,04669,04063,05906,04047,15968,02218,14687,20926,15954,09017,05277,06541,04714,10255,00911,06505,20983,07194,07191,07759,02177,09655,04050,09630,02821,00081,19087,00857,12214,00896,00113,08445,00847,11521,14057,09001,04112,05305,05300,08954,07808,01552,14098,00934,04678,04712,04040,17200,02156,20937,08986,07134,15271,20994,05869,02807,02135,04687,18470,06499,05935,03464,07196,13458,04647,04724,05289,03435,06466,02184,02801,14096,09681,20334,05316,00919,13402,07106,14105,13451,04689,04082,06542,08402,00897,02852,04120,05925,05937,00923,02741,20355,09674,06547,00870,12175,10226,00936,00860,14084,19691,03396,01538,05866,07748,03411,07752,15967,07189,04016,02760,02795,07715,08371,15969,04110,00861,04662,16590,09046,05894,10281,02203,02831,02199,00875,02171,01485,20975,11517,00900,01546,03445,04021,16633,05909,07761,05253,10279,07737,07751,05890,13423,02183,00926,12775,04713,14063,18452,00935,01544,20922,02853,03474,19012,20993,05932,06521,07162,06477,03455,04684,14018,02164,02119,15936,02858,04658,20955,16549,04652,05288,05308,14722,11545,10277,14078,00886,04102,03431,07789,03389,03378,06554,02137,17790,07754,01516,08369,05902,06520,07717,03410,09014,20396,07086,20987,00914,17229,07760,02848,07102,18430,05870,16634,07719,07190,04079,03398,02208,01535,01568,20971,03412,12827,10251,04659,14121,04075,19110,09070,14074,00865,00323,05929,07755,03443,14079,04646,13407,17797,19683,09056,02124,06545,03463,02129,04697,19039,15299,05266,14122,01497,01585,09045,04710,01509,07802,03419,04119,03425,18404,04651,02797,19061,02777,10280,05862,07714,08401,07740,13486,05938,04642,17798,09025,02784,06568,05313,10274,19732,10223,14733,13412,09689,02202,02152,06474,13440,08980,18479,20953,14097,16571,07140,05865,09690,07735,19742,17825,20961,02118,05883,07176,04064,02847,10294,07108,18407" };

		TaintConfigUtil.loadTaintConfig();
		ArrayList<ArrayList<String>> test_vectors = initializeVectors();
		ArrayList<ArrayList<String>> train_vectors = initializeVectors();
		for (String w : warnings) {
			String[] fields = w.split(",");
			BugReport bugReport = processOwaspReports(fields[0], fields[5]);
			String sourceFile = fields[0] + ".java";
			String codeStr = String.join("\n",
					Files.readAllLines(Paths.get(SOURCE_BASE + classFileDir + sourceFile), StandardCharsets.UTF_8));
			CompilationUnit cu = JavaParser.parse(codeStr);

			ArrayList<Integer> arrayList = bugReport.getLinenumbers().get(sourceFile);
			// finding tainted src
			cu.getNodesByType(MethodCallExpr.class).stream().filter(c -> c.getBegin().get().line == arrayList.get(0))
					.forEach(c -> {
						String taintSrc = c.toString();
						bugReport.setTaintSrc(taintSrc.substring(0, taintSrc.indexOf("(")));
					});

			// finding num of function call in witness
			int numofCalls = (int) cu.getNodesByType(MethodDeclaration.class).stream()
					.filter(m -> m.getNameAsString().equals(bugReport.getMethodName())).findFirst().get().getBody().get()
					.getNodesByType(MethodCallExpr.class).stream()
					.filter(call -> (call.getBegin().get().line >= arrayList.get(0)
							&& call.getBegin().get().line <= bugReport.getLineNumber()))
					.count();

			// finding num of condition in witness
			int numofCond = (int) cu.getNodesByType(IfStmt.class).stream()
					.filter(c -> (c.getBegin().get().line >= arrayList.get(0)
							&& c.getBegin().get().line <= bugReport.getLineNumber()))
					.count();

			// finding num of loops in witness
			numofCond += (int) cu.getNodesByType(ForStmt.class).stream()
					.filter(c -> (c.getBegin().get().line >= arrayList.get(0)
							&& c.getBegin().get().line <= bugReport.getLineNumber()))
					.count();
			numofCond += (int) cu.getNodesByType(ForeachStmt.class).stream()
					.filter(c -> (c.getBegin().get().line >= arrayList.get(0)
							&& c.getBegin().get().line <= bugReport.getLineNumber()))
					.count();

			String vec = bugReport.toCSV() + "," + arrayList.get(0) + "," + fields[3] + "," + fields[4] + ","
					+ fileStats.get(sourceFile) + "," + numofCalls + "," + numofCond + "," + bugReport.getTime() + ","
					+ fields[5];

			System.out.println(bugReport.getClassName() + " :: " + vec);

			String singleDPFile = DirectoryUtil.PROJECT_DIR + "ml/feature/data/singles/owasp/" + fields[0] + "_"
					+ fields[5] + ".arff";
			PrintUtil.printToFile(singleDPFile, metadata + String.join("\n", vec), false);
			
			//			for (int i = 0; i < testProgs.length; i++) {
			//				if (testProgs[i].contains(fields[0].substring(13))) {
			//					test_vectors.get(i).add(vec);
			//				} else {
			//					train_vectors.get(i).add(vec);
			//				}
			//			}
		}
		//		for (int i = 0; i < test_outFile.length; i++) {
		//			PrintUtil.printToFile(DirectoryUtil.MANGROVEDIR + "ml/feature/data/" + train_outFile[i],
		//					String.join("\n", train_vectors.get(i)), false);
		//			PrintUtil.printToFile(DirectoryUtil.MANGROVEDIR + "ml/feature/data/" + test_outFile[i],
		//					String.join("\n", test_vectors.get(i)), false);
		//
		//		}
	}

	static final String reportsPath = DirectoryUtil.PROJECT_DIR + "data/findsecbugs/reports/reports_owasp/";

	public static BugReport processOwaspReports(String sourceFile, String label)
			throws IOException, ParserConfigurationException, SAXException {
		String fileLabel = label.equals("truepositive") ? "TP" : "FP";
		File report = new File(reportsPath + sourceFile.substring(13) + fileLabel + ".xml");
		return BugReportUtil.parseSingleBugXML(report);
	}

	private static ArrayList<ArrayList<String>> initializeVectors() {
		ArrayList<ArrayList<String>> vectors = new ArrayList<ArrayList<String>>();
		vectors.add(new ArrayList<String>());
		vectors.add(new ArrayList<String>());
		vectors.add(new ArrayList<String>());
		vectors.add(new ArrayList<String>());
		vectors.add(new ArrayList<String>());
		return vectors;
	}
}