package jp.kusumotolab.kgenprog.ga;

import java.util.List;
import java.util.function.Function;
import org.eclipse.jdt.core.dom.Statement;
import jp.kusumotolab.kgenprog.project.GeneratedAST;

public class RouletteStatementSelection implements CandidateSelection {

  private final RandomNumberGeneration randomNumberGeneration;
  private Roulette<Statement> roulette;

  public RouletteStatementSelection(final RandomNumberGeneration randomNumberGeneration) {
    this.randomNumberGeneration = randomNumberGeneration;
  }

  @Override
  public void setCandidates(final List<GeneratedAST> candidates) {
    final StatementVisitor visitor = new StatementVisitor(candidates);

    final Function<Statement, Double> weightFunction = statement -> {
      final int statementWeight = getStatementWeight(statement);

      final double inverse = 1 / ((double) statementWeight);
      return Math.pow(inverse, 2);
    };

    final List<Statement> statements = visitor.getStatements();
    roulette = new Roulette<>(statements, weightFunction, randomNumberGeneration);
  }

  protected int getStatementWeight(Statement statement) {
    final StatementVisitor statementVisitor = new StatementVisitor(statement);
    final List<Statement> statements = statementVisitor.getStatements();
    return statements.size();
  }

  @Override
  public Statement exec() {
    return roulette.exec();
  }
}
