// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// Extend.proto

package auto.alias.extend;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;

public class Level1 extends Level2 implements WebpbMessage {

  public static final String WEBPB_METHOD = "";

  public static final String WEBPB_CONTEXT = "";

  public static final String WEBPB_PATH = "";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  @JsonAlias("foo_3")
  @JsonProperty("c")
  private Integer foo_3;

  @JsonAlias("foo_4")
  @JsonProperty("d")
  private Long foo_4;

  public Level1() {
  }

  public Level1(Integer foo_3, Long foo_4) {
    this.foo_3 = foo_3;
    this.foo_4 = foo_4;
  }

  public Integer getFoo_3() {
    return this.foo_3;
  }

  public Level1 setFoo_3(Integer foo_3) {
    this.foo_3 = foo_3;
    return this;
  }

  public Long getFoo_4() {
    return this.foo_4;
  }

  public Level1 setFoo_4(Long foo_4) {
    this.foo_4 = foo_4;
    return this;
  }
}
