package jp.kusumotolab.kgenprog.project.build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import com.google.common.base.Objects;

public class JavaMemoryObject implements JavaFileObject {

  private final String fqn;
  private final Kind kind;
  private final String digest;
  private final URI uri;
  private final ByteArrayOutputStream bos;

  @Deprecated
  public JavaMemoryObject(final String fqn, final Kind kind) {
     this(fqn, kind, "xxxx");
  }
  /**
   * 書き込み用FileObjectの生成コンストラクタ．ビルド結果の書き込みに用いられる．
   * 
   * @param fqn
   * @param kind
   */
  public JavaMemoryObject(final String fqn, final Kind kind, final String digest) {
    this.fqn = fqn;
    this.kind = kind;
    this.digest = digest;
    this.uri = URI.create("jmo:///" + fqn.replace('.', '/') + kind.extension);
    this.bos = new ByteArrayOutputStream();
  }

  /**
   * InMemoryClassManager#inferBinaryNameで呼ばれるメソッド． inferする必要がないので直接binaryNameを返す．
   * 
   * @return
   */
  public String getBinaryName() {
    return fqn;
  }

  public byte[] getClassBytes() {
    return bos.toByteArray();
  }

  @Override
  public final URI toUri() {
    return uri;
  }

  @Override
  public final String getName() {
    return uri.getPath();
  }

  @Override
  public final InputStream openInputStream() throws IOException {
    return new ByteArrayInputStream(bos.toByteArray());
  }

  @Override
  public final OutputStream openOutputStream() throws IOException {
    return bos;
  }

  @Override
  public final Reader openReader(boolean ignoreEncodingErrors) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public final CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public final Writer openWriter() throws IOException {
    return new OutputStreamWriter(openOutputStream());
  }

  @Override
  public final long getLastModified() {
    return 0L;
  }

  @Override
  public final boolean delete() {
    return false;
  }

  @Override
  public final Kind getKind() {
    return kind;
  }

  @Override
  public final boolean isNameCompatible(final String simpleName, final Kind fileKind) {
    final String baseName = simpleName + kind.extension;
    return fileKind.equals(getKind()) && (baseName.equals(toUri().getPath()) || toUri().getPath()
        .endsWith("/" + baseName));
  }

  @Override
  public final NestingKind getNestingKind() {
    return null;
  }

  @Override
  public final Modifier getAccessLevel() {
    return null;
  }

  @Override
  public final String toString() {
    return fqn + "#" + digest.substring(0, 4);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final JavaMemoryObject that = (JavaMemoryObject) o;
    return Objects.equal(uri, that.uri) && Objects.equal(kind, that.kind);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(uri, kind);
  }
}
