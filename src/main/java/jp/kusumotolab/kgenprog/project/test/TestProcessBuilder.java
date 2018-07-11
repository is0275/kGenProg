package jp.kusumotolab.kgenprog.project.test;

import static java.util.stream.Collectors.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.kgenprog.project.BuildResults;
import jp.kusumotolab.kgenprog.project.GeneratedSourceCode;
import jp.kusumotolab.kgenprog.project.ProjectBuilder;
import jp.kusumotolab.kgenprog.project.SourceFile;
import jp.kusumotolab.kgenprog.project.factory.TargetProject;

/**
 * テスト実行クラス． 外部プロジェクトの単体テストclassファイルを実行してその結果を回収する．
 *
 * @author shinsuke
 *
 */
public class TestProcessBuilder {

  private static Logger log = LoggerFactory.getLogger(TestProcessBuilder.class);

  final private TargetProject targetProject;
  final private Path workingDir;
  final private ProjectBuilder projectBuilder;

  final static private String javaHome = System.getProperty("java.home");
  final static private String javaBin = Paths.get(javaHome, "bin/java").toString();
  final static private String testExecutorMain =
      "jp.kusumotolab.kgenprog.project.test.TestExecutorMain";

  public TestProcessBuilder(final TargetProject targetProject, final Path workingDir) {
    this.targetProject = targetProject;
    this.workingDir = workingDir;
    this.projectBuilder = new ProjectBuilder(this.targetProject);
  }

  public TestResults start(final GeneratedSourceCode generatedSourceCode) {
    log.debug("enter start(GeneratedSourceCode)");

    final BuildResults buildResults = projectBuilder.build(generatedSourceCode, this.workingDir);

    // ビルド失敗時の特殊処理
    // TODO BuildResults自体もNullableなのでNullObjectパターン適用すべきか．
    if (buildResults.isBuildFailed) {
      return EmptyTestResults.instance;
    }

    final String classpath = filterClasspathFromSystemClasspath();
    final String targetFQNs = joinFQNs(getTargetFQNs(buildResults));
    final String testFQNs = joinFQNs(getTestFQNs(buildResults));

    final ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, testExecutorMain,
        "-b", workingDir.toAbsolutePath().toString(), "-s", targetFQNs, "-t", testFQNs);

    // テスト実行のためにworking dirを移動（対象プロジェクトが相対パスを利用している可能性が高いため）
    builder.directory(this.targetProject.rootPath.toFile());

    try {
      final Process process = builder.start();
      process.waitFor();

      final TestResults testResults = TestResults.deserialize();

      // TODO 翻訳のための一時的な処理
      testResults.setBuildResults(buildResults);

      log.debug("exit start(GeneratedSourceCode)");
      return testResults;

      // String out_result = IOUtils.toString(process.getInputStream(), "UTF-8");
      // String err_result = IOUtils.toString(process.getErrorStream(), "SJIS");
      // System.out.println(out_result);
      // System.err.println(err_result);
      // System.out.println(process.exitValue());
    } catch (NoSuchFileException e) {
      // Serializeに失敗
    } catch (IOException e) {
      // TODO 自動生成された catch ブロック
      log.error(e.getMessage(), e);
//      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO 自動生成された catch ブロック
      log.error(e.getMessage(), e);
//      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO 自動生成された catch ブロック
      log.error(e.getMessage(), e);
      e.printStackTrace();
    }

    log.debug("exit start(GeneratedSourceCode)");
    return EmptyTestResults.instance;

  }

  private String joinFQNs(final Collection<FullyQualifiedName> fqns) {
    log.debug("enter joinFQNs(Collection<>)");
    return fqns.stream().map(fqn -> fqn.value).collect(joining(TestExecutorMain.SEPARATOR));
  }

  private Set<FullyQualifiedName> getTargetFQNs(final BuildResults buildResults) {
    log.debug("enter getTargetFQNs(BuildResults)");

    final Set<FullyQualifiedName> sourceFQNs =
        getFQNs(buildResults, this.targetProject.getSourceFiles());

    // TODO testにsourceが含まれるのでsubtractしておく．
    // https://github.com/kusumotolab/kGenProg/issues/79
    sourceFQNs.removeAll(getTestFQNs(buildResults));

    log.debug("exit getTargetFQNs(BuildResults)");
    return sourceFQNs;
  }

  private Set<FullyQualifiedName> getTestFQNs(final BuildResults buildResults) {
    log.debug("enter getTestFQNs(BuildResults)");
    return getFQNs(buildResults, this.targetProject.getTestFiles());
  }

  private Set<FullyQualifiedName> getFQNs(final BuildResults buildResults,
      final List<SourceFile> sources) {
    log.debug("enter getFQNs(BuildResults, List<>)");
    return sources.stream().map(source -> buildResults.getPathToFQNs(source.path))
        .filter(fqn -> null != fqn).flatMap(c -> c.stream()).collect(toSet());
  }

  private final String jarFileTail = "-(\\d+\\.)+jar$";

  /**
   * 現在実行中のjavaプロセスのcpから，TestExecutorMain実行に必要なcpをフィルタリングする．
   *
   * @return
   */
  private String filterClasspathFromSystemClasspath() {
    log.debug("enter filterClasspathFromSystemClasspath()");
    // 依存する外部ライブラリを定義
    // TODO もうちょいcoolに改善
    final String[] classpaths = System.getProperty("java.class.path").split(File.pathSeparator);
    final List<String> filter = new ArrayList<>();
    filter.add("args4j");
    filter.add("jacoco\\.core");
    filter.add("asm");
    filter.add("asm-commons");
    filter.add("asm-tree");
    filter.add("junit");
    filter.add("hamcrest-core");

    // cp一覧から必須外部ライブラリのみをフィルタリング
    final List<String> result = Stream.of(classpaths)
        .filter(cp -> filter.stream().anyMatch(f -> cp.matches(".*" + f + jarFileTail)))
        .collect(toList());

    // 自身（TestProcessBuilder.class）へのcpを追加
    try {
      result.add(Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
          .toString());
    } catch (URISyntaxException e) {
      log.error(e.getMessage(), e);
//      e.printStackTrace();
    }
    log.debug("exit filterClasspathFromSystemClasspath()");
    return String.join(File.pathSeparator, result);
  }
}
