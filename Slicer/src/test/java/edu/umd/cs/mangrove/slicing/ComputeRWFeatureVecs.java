package edu.umd.cs.mangrove.slicing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
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

public class ComputeRWFeatureVecs {

	static HashMap<String, BugReport> reportMap;

	private final static File xmlFile = new File(DirectoryUtil.PROJECT_DIR + "data/findsecbugs/reports/AllReports.xml");

	static final String[] benchList = { "Apollo", "Biojava", "Freecs", "Giraph", "H2", "Hsqldb", "Jackrabbit", "Jetty",
			"JodaTime", "JPF", "Mybatis", "Okhttp3", "UPM", "Susi" };

	static HashSet<String> dataPoints = new HashSet<String>();

	public static void main(final String[] args) throws IOException, ClassHierarchyException, UnsoundGraphException,
			CancelException, ParserConfigurationException, SAXException {
		HashMap<String, String> project_src = new HashMap<String, String>();
		HashMap<String, String> reportFiles = new HashMap<String, String>();
		String wFileName = "data/SliceThese.txt";
		ArrayList<String> warnings = (ArrayList<String>) Files.readAllLines(Paths.get(wFileName));
		String metadata = "@relation rw-all\n" + "\n"
				+ "@attribute rname {PATH_TRAVERSAL_IN,CRLF_INJECTION_LOGS,XSS_SERVLET,XXE_DOCUMENT,PATH_TRAVERSAL_OUT,HTTPONLY_COOKIE,UNVALIDATED_REDIRECT,WEAK_TRUST_MANAGER,WEAK_HOSTNAME_VERIFIER,UNENCRYPTED_SOCKET,HARD_CODE_PASSWORD,COMMAND_INJECTION,SQL_INJECTION_JDBC,XXE_SAXPARSER,XXE_XMLREADER,INSECURE_COOKIE,HTTP_RESPONSE_SPLITTING}\n"
				+ "@attribute sinkline numeric\n"
				+ "@attribute sinkIdentifier {File,Logger.info,PrintWriter.write,FileInputStream,DocumentBuilder.parse,Log.debug,FileOutputStream,Logger.debug,HttpServletResponse.sendError,Logger.error,null,javax.servlet.http.HttpServletResponse.sendRedirect,Logger.warning,Logger.severe,FileReader,localhost,remotehost,FileWriter,Runtime.exec,Statement.executeQuery,Connection.prepareStatement,Statement.addBatch,Statement.execute,Statement.executeUpdate,Logger.warn,SAXParser.parse,XMLReader.parse,Logger.log,HttpServletResponse.setHeader,ProcessBuilder.command,Connection.nativeSQL,PrintWriter.println}\n"
				+ "@attribute witnessLenght numeric\n" + "@attribute numClassesInvolved numeric\n"
				+ "@attribute rank numeric\n" + "@attribute priority numeric\n"
				+ "@attribute numBugs {null,1,2,3,10,4,6,5,21,17,9}\n" + "@attribute functions numeric\n"
				+ "@attribute conditions numeric\n" + "@attribute time numeric\n"
				+ "@attribute label {falsepositive,truepositive}\n" + "\n" + "@data\n";
		String[] train_outFile = { "rw-pw-train-1.arff", "rw-pw-train-2.arff", "rw-pw-train-3.arff",
				"rw-pw-train-4.arff", "rw-pw-train-5.arff" };
		String[] test_outFile = { "rw-pw-test-1.arff", "rw-pw-test-2.arff", "rw-pw-test-3.arff", "rw-pw-test-4.arff",
				"rw-pw-test-5.arff" };

				String[] testProgsPW = { "Biojava::Freecs::Jackrabbit", "Susi", "Apollo::Giraph::JPF::Mybatis", "Jetty::H2::UPM",
						"Hsqldb::Okhttp3::JodaTime" };
//		String[] testProgsRand = {
//				"Hsqldb.HsqlSocketFactory.140.ClientConnection.openConnection.truepositive::Hsqldb.DbBackup.97.DbBackupMain.main.truepositive::JPF.PathnameExpander.176.PathnameExpander.expandPath.falsepositive::H2.Db.323.Db.executeQuery.falsepositive::Susi.JsonFile.96.SusiServer.main.falsepositive::JPF.Config.414.JPF.main.truepositive::Apollo.NamespaceService.131.NamespaceService.findNamespaceBOs.falsepositive::Okhttp3.Main$1.254.Main.run.truepositive::H2.TestTwoPhaseCommit.98.TestTwoPhaseCommit.test.falsepositive::JPF.Config.2115.Config.getPath.falsepositive::Biojava.SimpleMMcifParser.577.AllChemCompProvider.run.falsepositive::Apollo.LocalFileConfigRepository.271.LocalFileConfigRepository.<init>.falsepositive::Okhttp3.CacheTest.2453.CacheTest.testGoldenCacheResponse.falsepositive::Mybatis.SqlRunner.107.SqlRunnerTest.shouldInsert.falsepositive::Jetty.ContextHandler.1048.ContextHandler.checkContext.truepositive::H2.BuildBase.390.Build.warConsole.falsepositive::UPM.TestPasswordDatabase.48.TestPasswordDatabase.testOpenNonExistantFile.falsepositive::Susi.ModifySkillService.235.AbstractAPIHandler_doPost.truepositive::Jackrabbit.IndexingQueueStore.145.SearchIndex.doInit.falsepositive::Jetty.ResourceService.606.ResourceService.passConditionalHeaders.falsepositive::Hsqldb.CodeSwitcher.475.CodeSwitcher.main.truepositive::Hsqldb.DatabaseManager.1057.DatabaseManager.actionPerformed.truepositive::Biojava.ScanSymmetry.76.ScanSymmetry.run.falsepositive::Biojava.Astral.229.Astral.getRepresentatives.truepositive::Biojava.RemoteRawBioUnitDataProvider.143.RemoteRawBioUnitDataProvider.main.truepositive::Jetty.CookieCutter.372.SessionHandler.checkRequestedSessionId.truepositive::Okhttp3.Http2Server.187.Http2Server.main.truepositive::UPM.TestPasswordDatabase.94.TestPasswordDatabase.testAddAccount.falsepositive::H2.DbConnection.61.DbConnection.reset.falsepositive::Susi.UndoDeleteSkillService.76.AbstractAPIHandler_doPost.truepositive::Okhttp3.SslClient$Builder.145.SslClient$Builder.build.truepositive::Susi.ModifySkillService.273.AbstractAPIHandler_doPost.truepositive::Hsqldb.Preprocessor.656.Preprocessor.processInclude.falsepositive::JPF.Config.889.JPF.main.truepositive::JPF.FileUtils.83.JPF.main.falsepositive::JPF.JPF.690.JPF.main.truepositive::Hsqldb.CodeSwitcher.153.CodeSwitcher.main.truepositive::JPF.RunTest.288.RunTest.main.truepositive::Freecs.StaticRequestHandler.78.RequestReader.run.truepositive::H2.TestFunctionOverload.59.TestFunctionOverload.test.falsepositive::Giraph.AnnotationUtils$GeneralClassesIterator.197.GiraphRunner.run.falsepositive::Susi.DisableSkillService.76.AbstractAPIHandler.doPost.truepositive::Biojava.PositionInQueueXMLConverter.69.JFatCatClient.getPositionInQueue.truepositive::Susi.DeleteSkillService.68.AbstractAPIHandler.doPost.truepositive::Susi.ModifySkillService.314.AbstractAPIHandler_doPost.falsepositive::Giraph.ZooKeeperManager.500.GraphTaskManager.startZooKeeperManager.falsepositive::UPM.TestPasswordDatabase.130.TestPasswordDatabase.testRemoveAccount.falsepositive::Hsqldb.CodeSwitcher.151.CodeSwitcher.main.truepositive::Jetty.AbstractDatabaseLoginModule.95.AbstractLoginModule.login.falsepositive::Susi.EnableSkillService.75.AbstractAPIHandler.doPost.truepositive::Hsqldb.TarFileOutputStream.135.TarGeneratorMain.main.truepositive::H2.SourceCompiler.316.SourceCompiler.getMethod.falsepositive::JPF.ESParser.76.ESParser.main.truepositive::Giraph.ZooKeeperManager.434.GraphTaskManager.startZooKeeperManager.falsepositive::Hsqldb.LdapAuthBeanTester.132.LdapAuthBeanTester.main.truepositive::Susi.CreateSkillService.174.CreateSkillService.doPost.falsepositive::H2.WebApp.1330.WebApp.editResult.truepositive::Susi.SusiSkill.462.SusiSkill.main.falsepositive::JPF.RunJPF.220.JPF.main.truepositive::Jackrabbit.DatabasePersistenceManager.1147.DatabasePersistenceManager.reestablishConnection.falsepositive::Freecs.LogFile$LogFileShrinker.177.LogFile.main.truepositive::JPF.Config.786.JPF.main.truepositive::Biojava.DownloadChemCompProvider.329.ChemCompGroupFactory.getChemComp.truepositive::JPF.Config.560.JPF.main.falsepositive::Susi.CreateSkillService.87.CreateSkillService.doPost.truepositive::Freecs.MessageTemplateFinder.56.MessageTemplateFinder.main.truepositive::Biojava.SimpleMMcifParser.225.AllChemCompProvider.run.falsepositive::Biojava.SimpleMMcifParser.299.AllChemCompProvider.run.falsepositive::Susi.JsonFile.95.SusiServer.main.falsepositive::Jetty.ResourceService.263.ResourceService.doGet.truepositive::Susi.CreateSkillService.97.CreateSkillService.doPost.truepositive::Susi.SusiServer.316.SusiServer.main.falsepositive::Hsqldb.Preprocessor.322.PreprocessorAntTask.execute.falsepositive::Hsqldb.ServerAcl.506.ServerAcl.main.truepositive::H2.TestFunctionOverload.91.TestFunctionOverload.test.falsepositive::Susi.SusiSkill.444.SusiSkill.main.falsepositive::Jackrabbit.DatabasePersistenceManager.1184.DatabasePersistenceManager.reestablishConnection.falsepositive::Biojava.SimpleMMcifParser.840.AllChemCompProvider.run.falsepositive::H2.TestCsv.487.TestCsv.test.falsepositive::Susi.GetFileAtCommitID.53.AbstractAPIHandler_doPost.truepositive::",
//				"Susi.AIML2Susi.55.SusiSkill.main.falsepositive::H2.TestFullText$1.291.TestFullText.test.falsepositive::Hsqldb.DbBackup.121.Logger.backup.falsepositive::H2.TestFunctionOverload.107.TestFunctionOverload.test.falsepositive::Biojava.DownloadChemCompProvider.329.ChemCompDistribution.main.falsepositive::Susi.ModifySkillService.125.AbstractAPIHandler_doPost.truepositive::Jetty.ResourceService.302.ResourceService.doGet.falsepositive::Freecs.LogFile.136.LogFile.main.truepositive::Mybatis.SqlRunner.105.SqlRunnerTest.shouldInsert.falsepositive::Giraph.ZooKeeperManager.462.GraphTaskManager.startZooKeeperManager.falsepositive::Hsqldb.DbBackupMain.128.DbBackupMain.main.truepositive::Hsqldb.CodeSwitcher.299.CodeSwitcher.main.truepositive::H2.TestFuzzOptimizations.111.TestFuzzOptimizations.test.falsepositive::JPF.Source.203.Source.findSrcRoot.falsepositive::Hsqldb.TarGeneratorMain.59.TarGeneratorMain.main.truepositive::Hsqldb.CodeSwitcher.193.CodeSwitcher.main.truepositive::Susi.GetFileAtCommitID.51.AbstractAPIHandler_doPost.truepositive::H2.TestFunctions.2200.TestFunctions.addRow.truepositive::Susi.CreateSkillService.136.CreateSkillService.doPost.truepositive::Susi.EnableSkillService.69.AbstractAPIHandler.doPost.truepositive::Biojava.SimpleMMcifParser.315.AllChemCompProvider.run.falsepositive::Susi.JsonRepository.152.SusiServer.main.falsepositive::Susi.UndoDeleteSkillService.67.AbstractAPIHandler_doPost.truepositive::Susi.SusiMemory.254.SusiSkill.main.falsepositive::Susi.AIML2Susi.118.AIML2Susi.main.falsepositive::Susi.SusiSkill.423.SusiSkill.main.falsepositive::Hsqldb.LdapAuthBeanTester.104.LdapAuthBeanTester.main.truepositive::Biojava.JFatCatClient.121.JFatCatClient.hasPrecalculatedResult.truepositive::UPM.TestPasswordDatabase.65.TestPasswordDatabase.testOpenExistingDB.falsepositive::Susi.ModifySkillService.193.AbstractAPIHandler_doPost.truepositive::Biojava.SimpleMMcifParser.287.AllChemCompProvider.run.falsepositive::Hsqldb.DbBackupMain.84.DbBackupMain.main.truepositive::Hsqldb.TarReaderMain.55.TarReaderMain.main.truepositive::Biojava.SimpleMMcifParser.317.AllChemCompProvider.run.falsepositive::Susi.JsonRepository.178.SusiServer.main.falsepositive::Hsqldb.Preprocessor.224.PreprocessorAntTask.execute.falsepositive::JPF.PathnameExpander.152.PathnameExpander.expandPath.falsepositive::Jetty.GzipHandler.487.GzipHandler.handle.truepositive::JPF.JPFSiteUtils.55.JPF.main.truepositive::JPF.LogHandler.170.LogHandler.<init>.falsepositive::Biojava.MultipleAlignmentXMLParser.87.TestMultipleAlignmentXMLParser.testRecovery2.falsepositive::Giraph.AllOptions.154.AllOptions.main.truepositive::Biojava.EcodInstallation$EcodParser.589.EcodInstallation.main.truepositive::Freecs.GroupManager.492.RequestReader.run.truepositive::Susi.DeleteSkillService.51.AbstractAPIHandler.doPost.truepositive::Biojava.ReadUtils.83.RCSBDescriptionFactoryTest.testUrl.truepositive::UPM.Preferences.105.DatabaseActions.newDatabase.truepositive::Hsqldb.HsqlSocketFactory.125.ClientConnection.openConnection.truepositive::Biojava.DownloadChemCompProvider.217.ChemCompDistribution.main.falsepositive::Mybatis.ScriptRunner.235.AbstractLazyTest.before.falsepositive::Biojava.AbstractUserArgumentProcessor.342.CeMain.main.truepositive::Biojava.AllChemCompProvider.128.AllChemCompProvider.downloadFile.falsepositive::Biojava.JFatCatClient.255.JFatCatClient.sendMultiAFPChainToServer.truepositive::Susi.SusiMemory.254.SusiMind.main.falsepositive::Biojava.AllChemCompProvider.101.AllChemCompProvider.ensureFileExists.falsepositive::Susi.SusiSkill.431.SusiSkill.main.falsepositive::Jetty.ContextHandler.1046.ContextHandler.checkContext.truepositive::Okhttp3.SampleServer.122.SampleServer.main.truepositive::H2.Db.338.SamplesTest.test.falsepositive::H2.TestFullText.204.TestFullText.test.falsepositive::JodaTime.ZoneInfoCompiler.118.ZoneInfoCompiler.main.truepositive::H2.SQLInjection.132.SQLInjection.main.truepositive::Apollo.ConsumerAuthenticationFilterTest.70.ConsumerAuthenticationFilterTest.testAuthFailed.falsepositive::Biojava.AllChemCompProvider.153.AllChemCompProvider.getLocalFileName.falsepositive::Susi.ModifySkillService.351.AbstractAPIHandler_doPost.falsepositive::H2.TestFullText.195.TestFullText.test.falsepositive::Biojava.AFPChainXMLParser.235.JFatCatClient.main.truepositive::Biojava.SimpleMMcifParser.216.AllChemCompProvider.run.falsepositive::Susi.JsonRepository.161.SusiSkill.main.falsepositive::Apollo.CtripLogoutHandler.28.CtripLogoutHandler.logout.truepositive::Susi.DisableSkillService.74.AbstractAPIHandler.doPost.truepositive::Susi.CreateSkillService.167.CreateSkillService.doPost.falsepositive::Susi.DAO.355.SusiMind.main.falsepositive::Biojava.BiologicalAssemblyTransformation.239.RemoteBioUnitDataProvider.getBioUnitTransformationList.truepositive::Susi.DeleteSkillService.65.AbstractAPIHandler.doPost.truepositive::Biojava.Astral.224.Astral.getRepresentatives.truepositive::Hsqldb.DbBackup.100.DbBackupMain.main.truepositive::H2.WebApp.1453.WebApp.editResult.truepositive::Mybatis.PreparedStatementHandler.87.SimpleExecutor.prepareStatement.falsepositive::JPF.PathnameExpander.149.PathnameExpander.expandPath.falsepositive::",
//				"UPM.TestPasswordDatabase.80.TestPasswordDatabase.testAddAccount.falsepositive::Susi.ModifySkillService.271.AbstractAPIHandler_doPost.truepositive::Hsqldb.CodeSwitcher.324.CodeSwitcher.main.truepositive::Susi.GroupListService.46.AbstractAPIHandler_doPost.truepositive::H2.TestScript.189.TestScript.test.truepositive::Jetty.AbstractHandler.100.AbstractHandler.doError.truepositive::Susi.ModifySkillService.191.AbstractAPIHandler_doPost.truepositive::Hsqldb.DbBackup.102.DbBackupMain.main.truepositive::H2.TestIndexHints.66.TestIndexHints.test.truepositive::Hsqldb.FileRecordReader.79.FileRecordReader.main.truepositive::Susi.UndoDeleteSkillService.63.AbstractAPIHandler_doPost.truepositive::Susi.GetAllLanguages.62.AbstractAPIHandler_doPost.truepositive::Mybatis.SqlRunner.184.SqlRunnerTest.shouldDemonstrateDDLThroughRunMethod.falsepositive::Susi.ModifySkillService.135.AbstractAPIHandler_doPost.truepositive::H2.SQLInjection.247.SQLInjection.main.truepositive::Giraph.GeneratePrimitiveClasses.255.GeneratePrimitiveClasses.main.falsepositive::Susi.DAO.355.SusiServer.main.falsepositive::Susi.AIML2Susi.55.SusiServer.main.falsepositive::Biojava.CallableStructureAlignment.178.CallableStructureAlignment.call.falsepositive::Biojava.StructureName.231.RemoteDomainProvider.main.truepositive::H2.SQLInjection.350.SQLInjection.main.truepositive::Susi.JsonRepository.135.SusiServer.main.falsepositive::Susi.GetSkillJsonService.67.AbstractAPIHandler_doPost.truepositive::Hsqldb.DatabaseManager.349.DatabaseManager.main.truepositive::Jackrabbit.IndexingQueueStore.155.SearchIndex.doInit.falsepositive::Apollo.CtripLogoutHandler.39.CtripLogoutHandler.logout.truepositive::Susi.GetSkillJsonService.69.AbstractAPIHandler_doPost.truepositive::JPF.JPF_java_io_FileDescriptor.169.JPF_java_io_FileDescriptor.read____I.falsepositive::Biojava.ChemCompGroupFactory.116.ChemCompTest.testMEA.falsepositive::H2.TestCsv.489.TestCsv.test.falsepositive::H2.TestFunctionOverload.71.TestFunctionOverload.test.falsepositive::UPM.Preferences.160.DatabaseActions.newDatabase.falsepositive::Okhttp3.URLConnectionTest.1945.URLConnectionTest.authenticateWithCharset.truepositive::Susi.SusiSkill.457.SusiSkill.main.falsepositive::Hsqldb.DbBackup.98.DbBackupMain.main.truepositive::H2.TestCompress.153.TestCompress.test.falsepositive::H2.TestJoin.251.TestJoin.test.falsepositive::Susi.CreateSkillService.92.CreateSkillService.doPost.truepositive::Okhttp3.MockHttp2Peer.181.MockHttp2Peer.openSocket.truepositive::Mybatis.DefaultVFS.270.DefaultVFS.list.truepositive::JPF.Config.726.JPF.main.truepositive::H2.RunScript.283.CreateScriptFile.main.truepositive::Biojava.HasResultXMLConverter.78.JFatCatClient.hasPrecalculatedResult.falsepositive::Okhttp3.RecordingAuthenticator.35.RecordingAuthenticator.<init>.truepositive::H2.Migrate.217.Migrate.main.falsepositive::Susi.ModifySkillService.103.AbstractAPIHandler_doPost.truepositive::Susi.JsonRepository.153.SusiServer.main.falsepositive::Mybatis.SqlRunner.80.SqlRunnerTest.shouldUpdateCategory.falsepositive::Mybatis.PreparedStatementHandler.80.SimpleExecutor.prepareStatement.falsepositive::JodaTime.ZoneInfoCompiler.96.ZoneInfoCompiler.main.truepositive::Susi.GetAllLanguages.60.AbstractAPIHandler_doPost.truepositive::Giraph.FileUtils.58.TestFilters.testVertexFilter.falsepositive::Hsqldb.DbBackup.250.Logger.backup.falsepositive::H2.Script.139.CreateCluster.main.truepositive::Biojava.SimpleMMcifParser.359.AllChemCompProvider.run.falsepositive::JPF.JPF.689.JPF.main.truepositive::JodaTime.ZoneInfoCompiler.471.ZoneInfoCompiler.compile.falsepositive::H2.TestCsv.406.TestCsv.test.falsepositive::H2.WebApp.1380.WebApp.editResult.truepositive::H2.TestFullText.200.TestFullText.test.falsepositive::JPF.FileUtils.159.JPF.main.falsepositive::Hsqldb.TriggerSample.384.TriggerSample.setup.truepositive::Biojava.SimpleMMcifParser.598.AllChemCompProvider.run.falsepositive::Jetty.CookieCutter.372.SessionHandler.doScope.truepositive::Jetty.MovedContextHandler$Redirector.124.MovedContextHandler$Redirector.handle.truepositive::Susi.CreateSkillService.135.CreateSkillService.doPost.truepositive::H2.TestNativeSQL.253.TestNativeSQL.test.falsepositive::Mybatis.ExternalResources.75.ExternalResourcesTest.testGetConfiguredTemplate.falsepositive::Mybatis.SqlRunner.149.SqlRunnerTest.shouldUpdateCategory.falsepositive::H2.TestFuzzOptimizations.114.TestFuzzOptimizations.test.falsepositive::UPM.TestPasswordDatabase.71.TestPasswordDatabase.testOpenExistingDB.falsepositive::Hsqldb.SqlTool.563.SqlTool.main.truepositive::JPF.DebugJenkinsStateSet.66.DebugJenkinsStateSet.<init>.falsepositive::Hsqldb.Server.1040.Server.setWebRoot.falsepositive::Susi.Caretaker.53.SusiServer.main.falsepositive::H2.TestJoin.253.TestJoin.test.falsepositive::UPM.TestPasswordDatabase.137.TestPasswordDatabase.testRemoveAccount.falsepositive::UPM.TestPasswordDatabase.111.TestPasswordDatabase.testRemoveAccount.falsepositive::JPF.Source.160.VM.initSubsystems.falsepositive::Susi.AppsService.66.AbstractAPIHandler_doPost.falsepositive::",
//				"Susi.UndoDeleteSkillService.65.AbstractAPIHandler_doPost.truepositive::Giraph.AnnotationUtils$JarClassesIterator.121.GiraphRunner.run.falsepositive::Jetty.ResourceService.548.ResourceService.passConditionalHeaders.truepositive::H2.CreateCluster.164.CreateCluster.main.truepositive::H2.Shell.456.Shell.main.truepositive::Hsqldb.SqlFileEmbedder.85.SqlFileEmbedder.main.truepositive::Susi.SusiMemory.254.SusiServer.main.falsepositive::JPF.JPF_java_io_FileDescriptor.104.JPF_java_io_FileDescriptor.open__Ljava_lang_String_2I__I.falsepositive::Biojava.StructureTools.1664.FastaAFPChainConverterTest.testCpSymmetric2.falsepositive::Susi.JsonRepository.134.SusiServer.main.falsepositive::Okhttp3.UrlConnectionCacheTest.1670.UrlConnectionCacheTest.testGoldenCacheResponse.falsepositive::Apollo.SimpleApolloConfigDemo.45.SimpleApolloConfigDemo.main.truepositive::UPM.DatabaseActions.391.DatabaseActions.openDatabase.truepositive::Hsqldb.DbBackupMain.136.DbBackupMain.main.truepositive::Hsqldb.TarGenerator$TarEntrySupplicant.396.TarGeneratorMain.main.falsepositive::Susi.JsonRepository.178.SusiSkill.main.falsepositive::Jackrabbit.FileBasedNamespaceMappings.149.SlowQueryHandler.doInit.falsepositive::Susi.JsonFile.95.SusiSkill.main.falsepositive::Susi.AIML2Susi.55.AIML2Susi.main.falsepositive::Susi.EnableSkillService.73.AbstractAPIHandler.doPost.truepositive::Hsqldb.DbBackup.92.DbBackupMain.main.truepositive::Susi.JsonRepository.153.SusiSkill.main.falsepositive::Biojava.SimpleMMcifParser.301.AllChemCompProvider.run.falsepositive::Susi.ModifySkillService.130.AbstractAPIHandler_doPost.truepositive::Biojava.RepresentativeXMLConverter.69.GetRepresentatives.getRepresentatives.truepositive::Biojava.JFatCatClient.320.JFatCatClient.sendAFPChainToServer.truepositive::Apollo.LocalFileConfigRepository.80.LocalFileConfigRepository.<init>.falsepositive::Hsqldb.CodeSwitcher.323.CodeSwitcher.main.truepositive::Biojava.RepresentativeXMLConverter.69.JFatCatClient.main.truepositive::Mybatis.SimpleStatementHandler.67.BaseExecutorTest.shouldDeleteAuthor.falsepositive::Freecs.StaticRequestHandler.101.RequestReader.run.truepositive::Susi.JsonRepository.134.SusiSkill.main.falsepositive::Biojava.AbstractUserArgumentProcessor.338.CeCPMain.main.truepositive::Apollo.LocalFileConfigRepository.74.LocalFileConfigRepository.<init>.falsepositive::Susi.DeleteSkillService.49.AbstractAPIHandler.doPost.truepositive::JodaTime.ZoneInfoProvider.198.ZoneInfoProvider.<init>.falsepositive::Biojava.EcodInstallation$EcodParser.577.EcodInstallation.main.truepositive::Hsqldb.DbBackupMain.104.DbBackupMain.main.truepositive::Freecs.LogFile$LogFileShrinker.187.LogFile.main.truepositive::Biojava.SerializableCache.153.SerializableCache.getCacheFile.falsepositive::Hsqldb.Servlet.226.Servlet.doGet.truepositive::Mybatis.XPathParser.257.XPathParserTest.shouldTestXPathParserMethods.falsepositive::Susi.DeleteSkillService.53.AbstractAPIHandler.doPost.truepositive::H2.CreateCluster.165.CreateCluster.main.truepositive::Biojava.DownloadChemCompProvider.112.ChemCompDistribution.main.falsepositive::Hsqldb.DbBackup.116.Logger.backup.falsepositive::JPF.PathOutputMonitor.226.PathOutputMonitor.<init>.falsepositive::H2.SQLInjection.328.SQLInjection.main.truepositive::JPF.PathnameExpander.166.PathnameExpander.expandPath.falsepositive::Susi.SusiServer.311.SusiServer.main.falsepositive::Biojava.ScanSymmetry.77.ScanSymmetry.run.falsepositive::Biojava.DownloadChemCompProvider.338.PDBFileReader.main.falsepositive::Susi.DisableSkillService.72.AbstractAPIHandler.doPost.truepositive::UPM.Preferences.138.Preferences.load.falsepositive::Biojava.SimpleMMcifParser.306.AllChemCompProvider.run.falsepositive::Hsqldb.CodeSwitcher.480.CodeSwitcher.main.truepositive::H2.TestIndex.239.TestIndex.test.falsepositive::JPF.ChoicePoint.131.ChoiceSelector.<init>.falsepositive::JPF.JPF_java_lang_System.118.JPF_java_lang_System.getKeyValuePairs_____3Ljava_lang_String_2.falsepositive::Susi.ModifySkillService.321.AbstractAPIHandler_doPost.falsepositive::Freecs.MessageTemplateFinder.48.MessageTemplateFinder.main.truepositive::JPF.JPF.512.JPF.main.truepositive::Biojava.SandboxStyleStructureProvider.184.SandboxStyleStructureProvider.getAllPDBIDs.falsepositive::Hsqldb.DatabaseManager.853.DatabaseManager.actionPerformed.truepositive::JPF.JPF_java_io_File.42.JPF_java_io_File.canRead____Z.falsepositive::Hsqldb.WebServerConnection.461.WebServerConnection.run.truepositive::UPM.Preferences.147.Preferences.load.falsepositive::JPF.Config.728.JPF.main.truepositive::Susi.DisableSkillService.70.AbstractAPIHandler.doPost.truepositive::H2.TestCsv.494.TestCsv.test.falsepositive::Susi.JsonRepository.170.SusiSkill.main.falsepositive::Susi.EnableSkillService.71.AbstractAPIHandler.doPost.truepositive::Mybatis.PreparedStatementHandler.85.SimpleExecutor.prepareStatement.falsepositive::Hsqldb.Preprocessor.660.Preprocessor.processInclude.falsepositive::Susi.AIML2Susi.55.SusiMind.main.falsepositive::Susi.ModifySkillService.113.AbstractAPIHandler_doPost.truepositive::Hsqldb.DbBackup.99.DbBackupMain.main.truepositive::Mybatis.DefaultVFS.265.DefaultVFS.list.truepositive::Hsqldb.DbBackupMain.106.DbBackupMain.main.truepositive::Jackrabbit.CooperativeFileLock.150.CooperativeFileLock.acquire.truepositive::",
//				"Susi.JsonRepository.170.SusiServer.main.falsepositive::Mybatis.PreparedStatementHandler.82.SimpleExecutor.prepareStatement.falsepositive::Hsqldb.TarReader$TarEntryHeader.583.TarReaderMain.main.truepositive::H2.SQLInjection.190.SQLInjection.main.truepositive::Okhttp3.SampleServer.45.SampleServer.dispatch.truepositive::H2.TestFunctionOverload.54.TestFunctionOverload.test.falsepositive::H2.TestFullText$1.271.TestFullText.test.falsepositive::Biojava.RemoteBioUnitDataProvider.152.RemoteBioUnitDataProvider.getNrBiolAssemblies.truepositive::Apollo.ResourceUtils.52.MetaDomainConsts.<init>.falsepositive::Susi.DAO.355.SusiSkill.main.falsepositive::Hsqldb.Servlet.222.Servlet.doGet.truepositive::Susi.JsonRepository.135.SusiSkill.main.falsepositive::JodaTime.ZoneInfoCompiler.94.ZoneInfoCompiler.main.truepositive::Biojava.FastaAFPChainConverter.402.FastaAFPChainConverter.main.truepositive::Hsqldb.DatabaseManagerCommon.268.DatabaseManager.insertTestData.falsepositive::Susi.JsonRepository.161.SusiServer.main.falsepositive::Susi.SusiServer.335.SusiServer.main.falsepositive::Jackrabbit.DatabasePersistenceManager.1051.DatabasePersistenceManager.init.falsepositive::Susi.SusiSkill.436.SusiSkill.main.falsepositive::Hsqldb.CodeSwitcher.194.CodeSwitcher.main.truepositive::Susi.ModifySkillService.108.AbstractAPIHandler_doPost.truepositive::Hsqldb.DbBackup.101.DbBackupMain.main.truepositive::Hsqldb.Preprocessor.653.Preprocessor.processInclude.falsepositive::JPF.Source.216.Source.findSrcRoot.falsepositive::Biojava.SimpleMMcifParser.381.AllChemCompProvider.run.falsepositive::Susi.JsonRepository.152.SusiSkill.main.falsepositive::JPF.RunJPF.218.JPF.main.truepositive::Freecs.StaticRequestHandler.86.RequestReader.run.truepositive::JPF.JPF_gov_nasa_jpf_CachedROHttpConnection.117.JPF_gov_nasa_jpf_CachedROHttpConnection.getContents__Ljava_lang_String_2___3B.truepositive::Susi.CreateSkillService.138.CreateSkillService.doPost.truepositive::Biojava.SimpleMMcifParser.273.AllChemCompProvider.run.falsepositive::Jetty.AbstractDatabaseLoginModule.128.AbstractLoginModule.login.falsepositive::Jetty.ResourceService.418.ResourceService.sendWelcome.truepositive::Susi.GroupListService$1.51.AbstractAPIHandler_doPost.truepositive::Hsqldb.Testdb.68.Testdb.main.falsepositive::Biojava.GuiWrapper.216.FatCat.main.truepositive::Biojava.TestLoadStructureFromURL.46.TestLoadStructureFromURL.testLoadStructureFromURL.falsepositive::Mybatis.ScriptRunner.246.AbstractLazyTest.before.falsepositive::Hsqldb.TarReaderMain.85.TarReaderMain.main.truepositive::Hsqldb.TriggerSample.374.TriggerSample.getConnection.falsepositive::Susi.GetSkillJsonService.71.AbstractAPIHandler_doPost.truepositive::UPM.TestPasswordDatabase.156.TestPasswordDatabase.deleteFile.falsepositive::Giraph.JMap.98.JMapHistoDumper.startJMapThread.falsepositive::JPF.CoverageAnalyzer.608.CoverageAnalyzer.<init>.falsepositive::JPF.FileUtils.500.FileUtils.copyFile.falsepositive::Biojava.EcodInstallation$EcodParser.517.EcodInstallation.main.truepositive::UPM.TestPasswordDatabase.52.TestPasswordDatabase.testOpenNonExistantFile.falsepositive::JPF.Config.531.JPF.main.falsepositive::Hsqldb.TarGeneratorMain.53.TarGeneratorMain.main.truepositive::Jetty.SecuredRedirectHandler.66.SecuredRedirectHandler.handle.truepositive::Susi.JsonFile.96.SusiSkill.main.falsepositive::Susi.RateSkillService.63.AbstractAPIHandler_doPost.truepositive::H2.TestTwoPhaseCommit.100.TestTwoPhaseCommit.test.falsepositive::Okhttp3.Main$2.272.Main.run.truepositive::Biojava.PDBStatus.584.MMCIFFileReader.main.truepositive::H2.TestScript.253.TestScript.test.truepositive::JPF.Config.2103.ClassInfo.makeModelClassPath.falsepositive::Susi.UndoDeleteSkillService.79.AbstractAPIHandler_doPost.truepositive::Susi.GetFileAtCommitID.55.AbstractAPIHandler_doPost.truepositive::Hsqldb.DatabaseManagerCommon.272.DatabaseManager.insertTestData.falsepositive::Hsqldb.Preprocessor.225.PreprocessorAntTask.execute.falsepositive::Biojava.PDBDomainProvider.143.PDBDomainProvider.main.truepositive::Susi.ModifySkillService.140.AbstractAPIHandler_doPost.truepositive::JPF.JPF_java_io_FileDescriptor.79.JPF_java_io_FileDescriptor.open__Ljava_lang_String_2I__I.falsepositive::Freecs.GroupManager.175.RequestReader.run.truepositive::Biojava.EcodInstallation$EcodParser.586.EcodInstallation.main.truepositive::Biojava.SandboxStyleStructureProvider.192.SandboxStyleStructureProvider.getAllPDBIDs.falsepositive::JPF.Config.568.JPF.main.falsepositive::JPF.ClassFile.116.ClassFilePrinter.main.truepositive::Mybatis.PooledDataSource.527.PooledDataSource.pingConnection.falsepositive::Hsqldb.TarReader.337.TarReaderMain.main.truepositive::Susi.SusiServer.339.SusiServer.main.falsepositive::H2.TestCsv.399.TestCsv.test.falsepositive::JPF.Source.171.VM.initSubsystems.falsepositive::JPF.JPF_java_io_FileDescriptor.163.JPF_java_io_FileDescriptor.read____I.falsepositive::Mybatis.DefaultVFS.125.DefaultVFS.list.truepositive::Apollo.ApolloConfigDemo.66.ApolloConfigDemo.main.truepositive::Jetty.ResourceService.390.ResourceService.sendWelcome.truepositive::Okhttp3.SocksProxy.185.SocksProxy.acceptCommand.truepositive::Biojava.PdbPairXMLConverter.56.JFatCatClient.main.truepositive::" };
		reportMap = BugReportUtil.parseXML2Map(xmlFile);
		TaintConfigUtil.loadTaintConfig();
		ArrayList<ArrayList<String>> test_vectors = initializeVectors();
		ArrayList<ArrayList<String>> train_vectors = initializeVectors();
		prepareSrcMap(project_src, reportFiles);
		HashMap<String, Integer> fileStats = new HashMap<String, Integer>();
		for (String bench : benchList) {
			fileStats.putAll(BugReportUtil.parseXMLForStats(new File(reportFiles.get(bench))));
		}
		for (String w : warnings) {
			String[] fields = w.split(",");
			BugReport bugReport = reportMap.get(fields[0] + "_" + fields[1]);

			String clazz = "L" + bugReport.getClassName().replaceAll("\\.", "/");
			String fileName = clazz.substring(clazz.lastIndexOf("/") + 1);
			String eFileName = fields[4].substring(fields[4].lastIndexOf("/") + 1);
			String eMethodName = fields[4].substring(0, fields[4].indexOf("("));
			String dataPointName = fields[5] + "." + fileName + "." + bugReport.getLineNumber() + "." + eFileName + "."
					+ eMethodName + "." + fields[3];

			String singleDPFile = DirectoryUtil.PROJECT_DIR + "ml/feature/singles/" + dataPointName + ".arff";
			if (new File(singleDPFile).exists()) {
				//System.out.println(singleDPFile + " exist!");
				continue;
			}

			String srcFileClassPath = fields[0].replaceAll("\\.", "/");
			if (srcFileClassPath.contains("$")) {
				srcFileClassPath = srcFileClassPath.substring(0, srcFileClassPath.indexOf("$"));
			}
			String srcFileClass = srcFileClassPath.substring(srcFileClassPath.lastIndexOf("/") + 1);
			if (!dataPoints.contains(fields[5] + "_" + srcFileClass + "_" + fields[1])) {
				System.out.println(fields[5] + "_" + srcFileClass + "_" + fields[1]);
				continue;
			}
			srcFileClassPath += ".java";
			srcFileClass += ".java";
			String codeStr = locateSrcFile(project_src, fields, srcFileClassPath);
			//if (codeStr == null) continue;
			CompilationUnit cu = JavaParser.parse(codeStr);
			int numofCalls = 0, numofCond = 0;
			for (Entry<String, ArrayList<Integer>> entry : bugReport.getLinenumbers().entrySet()) {
				ArrayList<Integer> lineNumbers = entry.getValue();

				// finding tainted src
				Optional<MethodCallExpr> findAny = cu.getNodesByType(MethodCallExpr.class).stream().filter(c -> {
					return lineNumbers.contains(c.getBegin().get().line);
				}).findAny();
				//&& TaintConfigUtil.taintedSources.contains(src)
				if (findAny.isPresent()) {
					MethodCallExpr x = findAny.get();
					System.err.println(x);
					String taintSrc = x.getNameAsString();//x.getScope().toString() + "." +
					bugReport.setTaintSrc(taintSrc);
				}

				// finding num of function call in witness
				numofCalls += (int) cu.getNodesByType(MethodCallExpr.class).stream()
						.filter(c -> lineNumbers.contains(c.getBegin().get().line)).count();

				// finding num of condition in witness
				numofCond += (int) cu.getNodesByType(IfStmt.class).stream()
						.filter(c -> lineNumbers.contains(c.getBegin().get().line)).count();

				// finding num of loops in witness
				numofCond += (int) cu.getNodesByType(ForStmt.class).stream()
						.filter(c -> lineNumbers.contains(c.getBegin().get().line)).count();

				numofCond += (int) cu.getNodesByType(ForeachStmt.class).stream()
						.filter(c -> lineNumbers.contains(c.getBegin().get().line)).count();
			}
			String vec = bugReport.toCSV() + "," + fileStats.get(srcFileClass) + "," + numofCalls + "," + numofCond + ","
					+ bugReport.getTime() + "," + fields[3];
			System.out.println(bugReport.getClassName() + " :: " + vec);

			PrintUtil.printToFile(singleDPFile, metadata + String.join("\n", vec), false);

//			for (int i = 0; i < testProgsPW.length; i++) {
//				if (testProgsPW[i].contains(fields[5])) {
//					test_vectors.get(i).add(vec);
//				} else {
//					train_vectors.get(i).add(vec);
//				}
//			}

//			for (int i = 0; i < testProgsRand.length; i++) {
//				if (testProgsRand[i].contains(dataPointName + "::")) {
//					//System.out.println("In test " + i + " : " + dataPointName);
//					test_vectors.get(i).add(vec);
//				} else {
//					//System.out.println("In train " + i + " : " + dataPointName);
//					train_vectors.get(i).add(vec);
//				}
//			}
		}
		
//		for (int i = 0; i < test_outFile.length; i++) {
//			PrintUtil.printToFile(DirectoryUtil.MANGROVEDIR + "ml/feature/data/" + train_outFile[i],
//					metadata+String.join("\n", train_vectors.get(i)), false);
//			PrintUtil.printToFile(DirectoryUtil.MANGROVEDIR + "ml/feature/data/" + test_outFile[i],
//					metadata+String.join("\n", test_vectors.get(i)), false);
//
//		}
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

	private static String locateSrcFile(HashMap<String, String> project_src, String[] fields, String srcFileClassPath) {
		String codeStr = null;
		for (String path : project_src.get(fields[5]).split(":")) {
			try {
				codeStr = String.join("\n", Files.readAllLines(Paths.get(path + srcFileClassPath), StandardCharsets.UTF_8));
				return codeStr;
			} catch (Exception e) {
				System.err.println("not in " + path);
			}
		}
		System.err.println("Could not locate source file " + srcFileClassPath);
		return null;
	}

	private static void prepareSrcMap(HashMap<String, String> project_src, HashMap<String, String> reportFiles) {
		String benchdir = DirectoryUtil.BENCH_DIR;
		project_src.put("Apollo",
				benchdir + "apollo/apollo-core/src/main/java/:" + benchdir + "apollo/apollo-demo/src/main/java/:" + benchdir
						+ "apollo/apollo-portal/src/main/java/:" + benchdir + "apollo/apollo-client/src/main/java/:"
						+ benchdir + "apollo/apollo-configservice/src/main/java/" + benchdir
						+ "apollo/apollo-core/src/test/java/:" + benchdir + "apollo/apollo-demo/src/test/java/:" + benchdir
						+ "apollo/apollo-portal/src/test/java/:" + benchdir + "apollo/apollo-client/src/test/java/:"
						+ benchdir + "apollo/apollo-configservice/src/test/java/");
		project_src.put("Biojava", benchdir + "biojava/biojava-structure/src/main/java/:" + benchdir
				+ "biojava/biojava-structure/src/test/java/");
		project_src.put("Freecs", benchdir + "freecs-1.3.20111225/src/");
		project_src.put("Giraph",
				benchdir + "giraph/giraph-core/src/main/java/:" + benchdir + "giraph/giraph-core/src/test/java/");
		project_src.put("H2", benchdir + "h2db/h2/src/main/:" + benchdir + "h2db/h2/src/installer/:" + benchdir
				+ "h2db/h2/src/test/:" + benchdir + "h2db/h2/src/tools/");

		project_src.put("Hsqldb", benchdir + "hsqldb/src/");
		project_src.put("Jackrabbit", benchdir + "jackrabbit/jackrabbit-core/src/main/java/:" + benchdir
				+ "jackrabbit/jackrabbit-webdav/src/main/java/:" + benchdir + "jackrabbit/jackrabbit-core/src/test/java/:"
				+ benchdir + "jackrabbit/jackrabbit-webdav/src/test/java/:");
		project_src.put("Jetty",
				benchdir + "jetty/jetty-server/src/main/java/:" + benchdir + "jetty/jetty-util/src/main/java/:" + benchdir
						+ "jetty/jetty-jaas/src/main/java/:" + benchdir + "jetty/jetty-http/src/main/java/" + benchdir
						+ "jetty/jetty-server/src/test/java/:" + benchdir + "jetty/jetty-util/src/test/java/:" + benchdir
						+ "jetty/jetty-jaas/src/test/java/:" + benchdir + "jetty/jetty-http/src/test/java/");
		project_src.put("JodaTime", benchdir + "joda-time/src/main/java/:" + benchdir + "joda-time/src/example/java/:"
				+ benchdir + "joda-time/src/test/java/:");
		project_src.put("JPF", benchdir + "jpf-core/src/main/:" + benchdir + "jpf-core/src/annotations/:" + benchdir
				+ "jpf-core/src/examples/:" + benchdir + "jpf-core/src/peers/:" + benchdir + "jpf-core/src/tests/:");

		project_src.put("Mybatis", benchdir + "mybatis-3/src/main/java/:" + benchdir + "mybatis-3/test/main/java/:");
		project_src.put("Okhttp3",
				benchdir + "okhttp/okhttp/src/main/java/:" + benchdir + "okhttp/okcurl/src/main/java/:" + benchdir
						+ "okhttp/benchmarks/src/main/java/:" + benchdir + "okhttp/mockwebserver/src/main/java/:" + benchdir
						+ "okhttp/okhttp-tests/src/main/java/:" + benchdir + "okhttp/okhttp-urlconnection/src/main/java/:"
						+ benchdir + "okhttp/static-server/src/main/java/:" + benchdir
						+ "okhttp/samples/crawler/src/main/java/:" + benchdir + "okhttp/samples/guide/src/main/java/:"
						+ benchdir + "okhttp/samples/simple-client/src/main/java/:" + benchdir
						+ "okhttp/samples/slack/src/main/java/:" + benchdir + "okhttp/samples/static-server/src/main/java/:"
						+ benchdir + "okhttp/okhttp/src/test/java/:" + benchdir + "okhttp/okcurl/src/main/test/:" + benchdir
						+ "okhttp/benchmarks/src/test/java/:" + benchdir + "okhttp/mockwebserver/src/main/test/:" + benchdir
						+ "okhttp/okhttp-tests/src/test/java/:" + benchdir + "okhttp/okhttp-urlconnection/src/test/java/:"
						+ benchdir + "okhttp/static-server/src/test/java/");
		project_src.put("UPM", benchdir + "upm-swing/src/:" + benchdir + "upm-swing/test/");
		project_src.put("Susi", benchdir + "susi_server/src/"); // TODO NOK

		String reportsDir = DirectoryUtil.PROJECT_DIR + "data/findsecbugs/reports/";
		reportFiles.put("Apollo", reportsDir + "apollo-all-Aug5.xml");
		reportFiles.put("Biojava", reportsDir + "biojava-Aug5.xml");
		reportFiles.put("Freecs", reportsDir + "freecs-Aug5.xml");
		reportFiles.put("Giraph", reportsDir + "giraph-Aug5.xml");
		reportFiles.put("H2", reportsDir + "h2-Aug5.xml");
		reportFiles.put("Hsqldb", reportsDir + "hsqldb-Aug5.xml");
		reportFiles.put("Jackrabbit", reportsDir + "jackrabbit-all.xml");
		reportFiles.put("Jetty", reportsDir + "jetty.xml");
		reportFiles.put("JodaTime", reportsDir + "joda-time-Aug5.xml");
		reportFiles.put("JPF", reportsDir + "jpfcore-Jun5.xml");
		reportFiles.put("Mybatis", reportsDir + "mybatis-Aug5.xml");
		reportFiles.put("Okhttp3", reportsDir + "okhttp-Feb24.xml");
		reportFiles.put("Susi", reportsDir + "susi-Aug5.xml");
		reportFiles.put("UPM", reportsDir + "upm-Feb23.xml");
		String[] dps = { "Apollo_ApolloConfigDemo_66", "Apollo_ConsumerAuthenticationFilterTest_70",
				"Apollo_CtripLogoutHandler_28", "Apollo_CtripLogoutHandler_39", "Apollo_LocalFileConfigRepository_74",
				"Apollo_LocalFileConfigRepository_80", "Apollo_LocalFileConfigRepository_271",
				"Apollo_NamespaceService_131", "Apollo_ResourceUtils_52", "Apollo_SimpleApolloConfigDemo_45",
				"Biojava_AbstractUserArgumentProcessor_338", "Biojava_AbstractUserArgumentProcessor_342",
				"Biojava_AFPChainXMLParser_235", "Biojava_AllChemCompProvider_101", "Biojava_AllChemCompProvider_128",
				"Biojava_AllChemCompProvider_153", "Biojava_Astral_224", "Biojava_Astral_229",
				"Biojava_BiologicalAssemblyTransformation_239", "Biojava_CallableStructureAlignment_178",
				"Biojava_ChemCompGroupFactory_116", "Biojava_DownloadChemCompProvider_112",
				"Biojava_DownloadChemCompProvider_217", "Biojava_DownloadChemCompProvider_329",
				"Biojava_DownloadChemCompProvider_329", "Biojava_DownloadChemCompProvider_338",
				"Biojava_EcodInstallation_517", "Biojava_EcodInstallation_577", "Biojava_EcodInstallation_586",
				"Biojava_EcodInstallation_589", "Biojava_FastaAFPChainConverter_402", "Biojava_GuiWrapper_216",
				"Biojava_HasResultXMLConverter_78", "Biojava_JFatCatClient_121", "Biojava_JFatCatClient_255",
				"Biojava_JFatCatClient_320", "Biojava_MultipleAlignmentXMLParser_87", "Biojava_PDBDomainProvider_143",
				"Biojava_PdbPairXMLConverter_56", "Biojava_PDBStatus_584", "Biojava_PositionInQueueXMLConverter_69",
				"Biojava_ReadUtils_83", "Biojava_RemoteBioUnitDataProvider_152", "Biojava_RemoteRawBioUnitDataProvider_143",
				"Biojava_RepresentativeXMLConverter_69", "Biojava_RepresentativeXMLConverter_69",
				"Biojava_SandboxStyleStructureProvider_184", "Biojava_SandboxStyleStructureProvider_192",
				"Biojava_ScanSymmetry_76", "Biojava_ScanSymmetry_77", "Biojava_SerializableCache_153",
				"Biojava_SimpleMMcifParser_216", "Biojava_SimpleMMcifParser_225", "Biojava_SimpleMMcifParser_273",
				"Biojava_SimpleMMcifParser_287", "Biojava_SimpleMMcifParser_299", "Biojava_SimpleMMcifParser_301",
				"Biojava_SimpleMMcifParser_306", "Biojava_SimpleMMcifParser_315", "Biojava_SimpleMMcifParser_317",
				"Biojava_SimpleMMcifParser_359", "Biojava_SimpleMMcifParser_381", "Biojava_SimpleMMcifParser_577",
				"Biojava_SimpleMMcifParser_598", "Biojava_SimpleMMcifParser_840", "Biojava_StructureName_231",
				"Biojava_StructureTools_1664", "Biojava_TestLoadStructureFromURL_46", "Freecs_GroupManager_175",
				"Freecs_GroupManager_492", "Freecs_LogFile_136", "Freecs_LogFile_177", "Freecs_LogFile_187",
				"Freecs_MessageTemplateFinder_48", "Freecs_MessageTemplateFinder_56", "Freecs_StaticRequestHandler_78",
				"Freecs_StaticRequestHandler_86", "Freecs_StaticRequestHandler_101", "Giraph_AllOptions_154",
				"Giraph_AnnotationUtils_121", "Giraph_AnnotationUtils_197", "Giraph_FileUtils_58",
				"Giraph_GeneratePrimitiveClasses_255", "Giraph_JMap_98", "Giraph_ZooKeeperManager_434",
				"Giraph_ZooKeeperManager_462", "Giraph_ZooKeeperManager_500", "H2_BuildBase_390", "H2_CreateCluster_164",
				"H2_CreateCluster_165", "H2_Db_323", "H2_Db_338", "H2_DbConnection_61", "H2_Migrate_217",
				"H2_RunScript_283", "H2_Script_139", "H2_Shell_456", "H2_SourceCompiler_316", "H2_SQLInjection_132",
				"H2_SQLInjection_190", "H2_SQLInjection_247", "H2_SQLInjection_328", "H2_SQLInjection_350",
				"H2_TestCompress_153", "H2_TestCsv_399", "H2_TestCsv_406", "H2_TestCsv_487", "H2_TestCsv_489",
				"H2_TestCsv_494", "H2_TestFullText_195", "H2_TestFullText_200", "H2_TestFullText_204",
				"H2_TestFullText_271", "H2_TestFullText_291", "H2_TestFunctionOverload_54", "H2_TestFunctionOverload_59",
				"H2_TestFunctionOverload_71", "H2_TestFunctionOverload_91", "H2_TestFunctionOverload_107",
				"H2_TestFunctions_2200", "H2_TestFuzzOptimizations_111", "H2_TestFuzzOptimizations_114", "H2_TestIndex_239",
				"H2_TestIndexHints_66", "H2_TestJoin_251", "H2_TestJoin_253", "H2_TestNativeSQL_253", "H2_TestScript_189",
				"H2_TestScript_253", "H2_TestTwoPhaseCommit_98", "H2_TestTwoPhaseCommit_100", "H2_WebApp_1330",
				"H2_WebApp_1380", "H2_WebApp_1453", "Hsqldb_CodeSwitcher_151", "Hsqldb_CodeSwitcher_153",
				"Hsqldb_CodeSwitcher_193", "Hsqldb_CodeSwitcher_194", "Hsqldb_CodeSwitcher_299", "Hsqldb_CodeSwitcher_323",
				"Hsqldb_CodeSwitcher_324", "Hsqldb_CodeSwitcher_475", "Hsqldb_CodeSwitcher_480",
				"Hsqldb_DatabaseManager_349", "Hsqldb_DatabaseManager_853", "Hsqldb_DatabaseManager_1057",
				"Hsqldb_DatabaseManagerCommon_268", "Hsqldb_DatabaseManagerCommon_272", "Hsqldb_DbBackup_92",
				"Hsqldb_DbBackup_97", "Hsqldb_DbBackup_98", "Hsqldb_DbBackup_99", "Hsqldb_DbBackup_100",
				"Hsqldb_DbBackup_101", "Hsqldb_DbBackup_102", "Hsqldb_DbBackup_116", "Hsqldb_DbBackup_121",
				"Hsqldb_DbBackup_250", "Hsqldb_DbBackupMain_84", "Hsqldb_DbBackupMain_104", "Hsqldb_DbBackupMain_106",
				"Hsqldb_DbBackupMain_128", "Hsqldb_DbBackupMain_136", "Hsqldb_FileRecordReader_79",
				"Hsqldb_HsqlSocketFactory_125", "Hsqldb_HsqlSocketFactory_140", "Hsqldb_LdapAuthBeanTester_104",
				"Hsqldb_LdapAuthBeanTester_132", "Hsqldb_Preprocessor_224", "Hsqldb_Preprocessor_225",
				"Hsqldb_Preprocessor_322", "Hsqldb_Preprocessor_653", "Hsqldb_Preprocessor_656", "Hsqldb_Preprocessor_660",
				"Hsqldb_Server_1040", "Hsqldb_ServerAcl_506", "Hsqldb_Servlet_222", "Hsqldb_Servlet_226",
				"Hsqldb_SqlFileEmbedder_85", "Hsqldb_SqlTool_563", "Hsqldb_TarFileOutputStream_135",
				"Hsqldb_TarGenerator_396", "Hsqldb_TarGeneratorMain_53", "Hsqldb_TarGeneratorMain_59",
				"Hsqldb_TarReader_337", "Hsqldb_TarReader_583", "Hsqldb_TarReaderMain_55", "Hsqldb_TarReaderMain_85",
				"Hsqldb_Testdb_68", "Hsqldb_TriggerSample_374", "Hsqldb_TriggerSample_384",
				"Hsqldb_WebServerConnection_461", "Jackrabbit_CooperativeFileLock_150",
				"Jackrabbit_DatabasePersistenceManager_1051", "Jackrabbit_DatabasePersistenceManager_1147",
				"Jackrabbit_DatabasePersistenceManager_1184", "Jackrabbit_FileBasedNamespaceMappings_149",
				"Jackrabbit_IndexingQueueStore_145", "Jackrabbit_IndexingQueueStore_155",
				"Jetty_AbstractDatabaseLoginModule_95", "Jetty_AbstractDatabaseLoginModule_128",
				"Jetty_AbstractHandler_100", "Jetty_ContextHandler_1046", "Jetty_ContextHandler_1048",
				"Jetty_CookieCutter_372", "Jetty_CookieCutter_372", "Jetty_GzipHandler_487",
				"Jetty_MovedContextHandler_124", "Jetty_ResourceService_263", "Jetty_ResourceService_302",
				"Jetty_ResourceService_390", "Jetty_ResourceService_418", "Jetty_ResourceService_548",
				"Jetty_ResourceService_606", "Jetty_SecuredRedirectHandler_66", "JodaTime_ZoneInfoCompiler_94",
				"JodaTime_ZoneInfoCompiler_96", "JodaTime_ZoneInfoCompiler_118", "JodaTime_ZoneInfoCompiler_471",
				"JodaTime_ZoneInfoProvider_198", "JPF_ChoicePoint_131", "JPF_ClassFile_116", "JPF_Config_414",
				"JPF_Config_531", "JPF_Config_560", "JPF_Config_568", "JPF_Config_726", "JPF_Config_728", "JPF_Config_786",
				"JPF_Config_889", "JPF_Config_2103", "JPF_Config_2115", "JPF_CoverageAnalyzer_608",
				"JPF_DebugJenkinsStateSet_66", "JPF_ESParser_76", "JPF_FileUtils_83", "JPF_FileUtils_159",
				"JPF_FileUtils_500", "JPF_JPF_gov_nasa_jpf_CachedROHttpConnection_117", "JPF_JPF_java_io_File_42",
				"JPF_JPF_java_io_FileDescriptor_79", "JPF_JPF_java_io_FileDescriptor_104",
				"JPF_JPF_java_io_FileDescriptor_163", "JPF_JPF_java_io_FileDescriptor_169", "JPF_JPF_java_lang_System_118",
				"JPF_JPF_512", "JPF_JPF_689", "JPF_JPF_690", "JPF_JPFSiteUtils_55", "JPF_LogHandler_170",
				"JPF_PathnameExpander_149", "JPF_PathnameExpander_152", "JPF_PathnameExpander_166",
				"JPF_PathnameExpander_176", "JPF_PathOutputMonitor_226", "JPF_RunJPF_218", "JPF_RunJPF_220",
				"JPF_RunTest_288", "JPF_Source_160", "JPF_Source_171", "JPF_Source_203", "JPF_Source_216",
				"Mybatis_DefaultVFS_125", "Mybatis_DefaultVFS_265", "Mybatis_DefaultVFS_270",
				"Mybatis_ExternalResources_75", "Mybatis_PooledDataSource_527", "Mybatis_PreparedStatementHandler_80",
				"Mybatis_PreparedStatementHandler_82", "Mybatis_PreparedStatementHandler_85",
				"Mybatis_PreparedStatementHandler_87", "Mybatis_ScriptRunner_235", "Mybatis_ScriptRunner_246",
				"Mybatis_SimpleStatementHandler_67", "Mybatis_SqlRunner_80", "Mybatis_SqlRunner_105",
				"Mybatis_SqlRunner_107", "Mybatis_SqlRunner_149", "Mybatis_SqlRunner_184", "Mybatis_XPathParser_257",
				"Okhttp3_CacheTest_2453", "Okhttp3_Http2Server_187", "Okhttp3_Main_254", "Okhttp3_Main_272",
				"Okhttp3_MockHttp2Peer_181", "Okhttp3_RecordingAuthenticator_35", "Okhttp3_SampleServer_45",
				"Okhttp3_SampleServer_122", "Okhttp3_SocksProxy_185", "Okhttp3_SslClient_145",
				"Okhttp3_UrlConnectionCacheTest_1670", "Okhttp3_URLConnectionTest_1945", "Susi_AIML2Susi_55",
				"Susi_AIML2Susi_55", "Susi_AIML2Susi_55", "Susi_AIML2Susi_55", "Susi_AIML2Susi_118", "Susi_AppsService_66",
				"Susi_Caretaker_53", "Susi_CreateSkillService_87", "Susi_CreateSkillService_92",
				"Susi_CreateSkillService_97", "Susi_CreateSkillService_135", "Susi_CreateSkillService_136",
				"Susi_CreateSkillService_138", "Susi_CreateSkillService_167", "Susi_CreateSkillService_174", "Susi_DAO_355",
				"Susi_DAO_355", "Susi_DAO_355", "Susi_DeleteSkillService_49", "Susi_DeleteSkillService_51",
				"Susi_DeleteSkillService_53", "Susi_DeleteSkillService_65", "Susi_DeleteSkillService_68",
				"Susi_DisableSkillService_70", "Susi_DisableSkillService_72", "Susi_DisableSkillService_74",
				"Susi_DisableSkillService_76", "Susi_EnableSkillService_69", "Susi_EnableSkillService_71",
				"Susi_EnableSkillService_73", "Susi_EnableSkillService_75", "Susi_GetAllLanguages_60",
				"Susi_GetAllLanguages_62", "Susi_GetFileAtCommitID_51", "Susi_GetFileAtCommitID_53",
				"Susi_GetFileAtCommitID_55", "Susi_GetSkillJsonService_67", "Susi_GetSkillJsonService_69",
				"Susi_GetSkillJsonService_71", "Susi_GroupListService_46", "Susi_GroupListService_51", "Susi_JsonFile_95",
				"Susi_JsonFile_95", "Susi_JsonFile_96", "Susi_JsonFile_96", "Susi_JsonRepository_134",
				"Susi_JsonRepository_134", "Susi_JsonRepository_135", "Susi_JsonRepository_135", "Susi_JsonRepository_152",
				"Susi_JsonRepository_152", "Susi_JsonRepository_153", "Susi_JsonRepository_153", "Susi_JsonRepository_161",
				"Susi_JsonRepository_161", "Susi_JsonRepository_170", "Susi_JsonRepository_170", "Susi_JsonRepository_178",
				"Susi_JsonRepository_178", "Susi_ModifySkillService_103", "Susi_ModifySkillService_108",
				"Susi_ModifySkillService_113", "Susi_ModifySkillService_125", "Susi_ModifySkillService_130",
				"Susi_ModifySkillService_135", "Susi_ModifySkillService_140", "Susi_ModifySkillService_191",
				"Susi_ModifySkillService_193", "Susi_ModifySkillService_235", "Susi_ModifySkillService_271",
				"Susi_ModifySkillService_273", "Susi_ModifySkillService_314", "Susi_ModifySkillService_321",
				"Susi_ModifySkillService_351", "Susi_RateSkillService_63", "Susi_SusiMemory_254", "Susi_SusiMemory_254",
				"Susi_SusiMemory_254", "Susi_SusiServer_311", "Susi_SusiServer_316", "Susi_SusiServer_335",
				"Susi_SusiServer_339", "Susi_SusiSkill_423", "Susi_SusiSkill_431", "Susi_SusiSkill_436",
				"Susi_SusiSkill_444", "Susi_SusiSkill_457", "Susi_SusiSkill_462", "Susi_UndoDeleteSkillService_63",
				"Susi_UndoDeleteSkillService_65", "Susi_UndoDeleteSkillService_67", "Susi_UndoDeleteSkillService_76",
				"Susi_UndoDeleteSkillService_79", "UPM_DatabaseActions_391", "UPM_Preferences_105", "UPM_Preferences_138",
				"UPM_Preferences_147", "UPM_Preferences_160", "UPM_TestPasswordDatabase_48", "UPM_TestPasswordDatabase_52",
				"UPM_TestPasswordDatabase_65", "UPM_TestPasswordDatabase_71", "UPM_TestPasswordDatabase_80",
				"UPM_TestPasswordDatabase_94", "UPM_TestPasswordDatabase_111", "UPM_TestPasswordDatabase_130",
				"UPM_TestPasswordDatabase_137", "UPM_TestPasswordDatabase_156" };
		for (String string : dps) {
			dataPoints.add(string);
		}
	}
}
