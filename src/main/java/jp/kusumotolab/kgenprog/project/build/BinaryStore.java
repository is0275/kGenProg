package jp.kusumotolab.kgenprog.project.build;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaFileObject;
import jp.kusumotolab.kgenprog.project.SourcePath;

/**
 * 差分ビルド + インメモリビルドのためのバイナリ格納庫．<br>
 * FQNをキーとしてビルド結果であるJavaMemoryObjectバイナリをメモリ上にキャッシュする． <br>
 * 
 * ref jsr107 https://static.javadoc.io/javax.cache/cache-api/1.0.0/javax/cache/package-summary.html
 * 
 * @author shin
 *
 */
public class BinaryStore {

  private Set<JavaBinaryObject> cache;

  public BinaryStore() {
    cache = new HashSet<>();
  }

  public void add(final JavaBinaryObject object) {
    cache.add(object);
  }

  public boolean exists(final BinaryStoreKey key) {
    return cache.stream()
        .anyMatch(jmo -> jmo.getPrimaryKey().equals(key.toString()));
  }
  
  public Set<JavaBinaryObject> get(final BinaryStoreKey key) {
    return cache.stream()
        .filter(jmo -> jmo.getPrimaryKey().equals(key.toString()))
        .collect(Collectors.toSet());
  }
  
  public JavaBinaryObject get(final String fqn) {
    return cache.stream()
        .filter(jmo -> jmo.getBinaryName().equals(fqn))
        .findFirst().orElseThrow(RuntimeException::new);
  }

  public Set<JavaBinaryObject> get(final SourcePath path) {
    return cache.stream()
        .filter(jmo -> jmo.getPath().equals(path))
        .collect(Collectors.toSet());
  }
  
  public Set<JavaBinaryObject> getAll() {
    return cache;
  }

  public Iterable<JavaBinaryObject> list(final String packageName) {
    return cache.stream()
        .filter(jmo -> jmo.getName()
            .startsWith("/" + packageName)) // TODO: スラッシュ開始で決め打ち．uriからの変換なので間違いないとは思う
        .collect(Collectors.toList());
  }

  public void removeAll() {
    cache.clear();
  }

  public void addAll(Set<JavaBinaryObject> binaries) {
    cache.addAll(binaries);
  }

}
