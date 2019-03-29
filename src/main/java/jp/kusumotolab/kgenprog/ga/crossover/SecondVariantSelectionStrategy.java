package jp.kusumotolab.kgenprog.ga.crossover;

import java.util.List;
import java.util.Random;
import jp.kusumotolab.kgenprog.ga.variant.Variant;

/**
 * 交叉において，2つ目の親の選択アルゴリズムを表すインターフェース． 1つ目の親として選択された個体とは異なる個体を選択する必要がある．
 * 
 * @author higo
 *
 */
public interface SecondVariantSelectionStrategy {

  /**
   * 選択を行うメソッド．選択対象の個体群および1つ目の親として選択された個体をを引数として与える必要あり．
   * 
   * @param variants 選択対象の個体群
   * @param firstVariant 1つ目の親として選択された個体
   * @return 選択された個体
   */
  Variant exec(List<Variant> variants, Variant firstVariant) throws CrossoverInfeasibleException;

  /**
   * kGenProgが基本実装として持つ，2つ目の親を選択するアルゴリズムを表す列挙型．
   * 
   * TODO ここに定義すべきではないものな気がするので将来移動させる予定．
   * 
   * @author higo
   *
   */
  public enum Strategy {
    
    Elite {

      /**
       * 2つ目の親の選択を行うインスタンスを生成するための抽象メソッド．
       * 
       * @see SecondVariantSelectionStrategy.Strategy#initialize(Random)
       * 
       * @param random 選択においてランダム処理を行うためのシード
       * @return 2つ目の親の選択を行うインスタンス
       */
      @Override
      public SecondVariantSelectionStrategy initialize(final Random random) {
        return new SecondVariantEliteSelection();
      }
    },

    GeneSimilarity {

      /**
       * 2つ目の親の選択を行うインスタンスを生成するための抽象メソッド．
       * 
       * @see SecondVariantSelectionStrategy.Strategy#initialize(Random)
       * 
       * @param random 選択においてランダム処理を行うためのシード
       * @return 2つ目の親の選択を行うインスタンス
       */
      @Override
      public SecondVariantSelectionStrategy initialize(final Random random) {
        return new SecondVariantGeneSimilarityBasedSelection(random);
      }
    },

    Random {

      /**
       * 2つ目の親の選択を行うインスタンスを生成するための抽象メソッド．
       * 
       * @see SecondVariantSelectionStrategy.Strategy#initialize(Random)
       * 
       * @param random 選択においてランダム処理を行うためのシード
       * @return 2つ目の親の選択を行うインスタンス
       */
      @Override
      public SecondVariantSelectionStrategy initialize(final java.util.Random random) {
        return new SecondVariantRandomSelection(random);
      }
    },

    TestComplementary {

      /**
       * 2つ目の親の選択を行うインスタンスを生成するための抽象メソッド．
       * 
       * @see SecondVariantSelectionStrategy.Strategy#initialize(Random)
       * 
       * @param random 選択においてランダム処理を行うためのシード
       * @return 2つ目の親の選択を行うインスタンス
       */
      @Override
      public SecondVariantSelectionStrategy initialize(final Random random) {
        return new SecondVariantTestComplementaryBasedSelection();
      }
    };

    /**
     * 2つ目の親の選択を行うインスタンスを生成するための抽象メソッド．
     * 
     * @param random 選択においてランダム処理を行うためのシード
     * @return 2つ目の親の選択を行うインスタンス
     */
    public abstract SecondVariantSelectionStrategy initialize(final Random random);
  }
}
