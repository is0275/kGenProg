package jp.kusumotolab.kgenprog.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jp.kusumotolab.kgenprog.Configuration;
import jp.kusumotolab.kgenprog.ga.variant.Base;
import jp.kusumotolab.kgenprog.ga.variant.CrossoverHistoricalElement;
import jp.kusumotolab.kgenprog.ga.variant.HistoricalElement;
import jp.kusumotolab.kgenprog.ga.variant.MutationHistoricalElement;
import jp.kusumotolab.kgenprog.ga.variant.Variant;
import jp.kusumotolab.kgenprog.ga.variant.VariantStore;
import jp.kusumotolab.kgenprog.project.test.TestResult;
import jp.kusumotolab.kgenprog.project.test.TestResults;

/**
 * バリアントの出力をするクラス.
 */
public class VariantStoreExporter {

  private static final Logger log = LoggerFactory.getLogger(VariantStoreExporter.class);

  /**
   * JSONを出力する.
   *
   * @param config 実行時の設定
   * @param variantStore 生成したバリアントを保持しているオブジェクト
   */
  public void writeToFile(final Configuration config, final VariantStore variantStore) {

    final Path outputPath = config.getOutDir()
        .resolve("history.json");
    final Gson gson = createGson(config);

    try {
      if (Files.notExists(config.getOutDir())) {
        Files.createDirectories(config.getOutDir());
      }

      final BufferedWriter out = Files.newBufferedWriter(outputPath);
      gson.toJson(variantStore, out);
      out.close();
    } catch (final IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  private Gson createGson(final Configuration config) {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    return gsonBuilder.registerTypeAdapter(VariantStore.class, new VariantStoreSerializer(config))
        .registerTypeHierarchyAdapter(Variant.class, new VariantSerializer())
        .registerTypeHierarchyAdapter(TestResults.class, new TestResultsSerializer())
        .registerTypeHierarchyAdapter(TestResult.class, new TestResultSerializer())
        .registerTypeHierarchyAdapter(HistoricalElement.class, new HistoricalElementSerializer())
        .registerTypeHierarchyAdapter(MutationHistoricalElement.class,
            new MutationHistoricalElementSerializer())
        .registerTypeHierarchyAdapter(CrossoverHistoricalElement.class,
            new CrossoverHistoricalElementSerializer())
        .registerTypeHierarchyAdapter(Base.class, new BaseSerializer())
        .registerTypeHierarchyAdapter(Patch.class, new PatchSerializer())
        .registerTypeHierarchyAdapter(FileDiff.class, new FileDiffSerializer())
        .registerTypeHierarchyAdapter(Path.class, new PathSerializer())
        .create();
  }
}
