// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// SubTypeSubValue.proto

package extend.test.main;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;
import io.github.jinganix.webpb.runtime.WebpbSubValue;

@WebpbSubValue("foo.bar")
public class SubTypeSubValue5 extends SubTypeSubValueSuper implements WebpbMessage {

  public static final String WEBPB_METHOD = "";

  public static final String WEBPB_CONTEXT = "";

  public static final String WEBPB_PATH = "";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  private Integer value;

  public SubTypeSubValue5() {
  }

  public SubTypeSubValue5(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return this.value;
  }

  public SubTypeSubValue5 setValue(Integer value) {
    this.value = value;
    return this;
  }
}
