// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// NoPackage.proto

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;

public class NoPackageTest implements WebpbMessage {

  public static final String WEBPB_METHOD = "";

  public static final String WEBPB_CONTEXT = "";

  public static final String WEBPB_PATH = "";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  private Integer foo;

  public NoPackageTest() {
  }

  public NoPackageTest(Integer foo) {
    this.foo = foo;
  }

  public Integer getFoo() {
    return this.foo;
  }

  public NoPackageTest setFoo(Integer foo) {
    this.foo = foo;
    return this;
  }
}
