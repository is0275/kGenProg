package jp.kusumotolab.kgenprog.project;

public interface ASTLocation {

  LineNumberRange NONE = new LineNumberRange(0, -1);

  SourcePath getSourcePath();

  /**
   * このLocationが指すノードがソースコード中でどの位置にあるか、行番号の範囲を返す。 範囲が求められない場合、(0, -1)のRangeを返す
   *
   * @return 行番号の範囲
   */
  LineNumberRange inferLineNumbers();

  GeneratedAST<?> getGeneratedAST();
}
