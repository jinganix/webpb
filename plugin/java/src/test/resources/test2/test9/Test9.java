// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// Test9.proto

package test9;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;

public class Test9 implements WebpbMessage {

  public static final String WEBPB_METHOD = "";

  public static final String WEBPB_CONTEXT = "";

  public static final String WEBPB_PATH = "";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  private Long test1;

  public Test9() {
  }

  public Test9(Long test1) {
    this.test1 = test1;
  }

  public Long getTest1() {
    return this.test1;
  }

  public Test9 setTest1(Long test1) {
    this.test1 = test1;
    return this;
  }
}
