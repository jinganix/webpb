// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// SubTypeSubValue.proto

package extend.test.main;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;

public class SubTypeSubValueStringSuper implements WebpbMessage {

  public static final String WEBPB_METHOD = "";

  public static final String WEBPB_CONTEXT = "";

  public static final String WEBPB_PATH = "";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  private String type;

  public SubTypeSubValueStringSuper() {
  }

  public SubTypeSubValueStringSuper(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }

  public SubTypeSubValueStringSuper setType(String type) {
    this.type = type;
    return this;
  }
}
