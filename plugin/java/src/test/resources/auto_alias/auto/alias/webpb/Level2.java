// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// WebpbOpts.proto

package auto.alias.webpb;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Level2 implements WebpbMessage {

  public static final String WEBPB_METHOD = "";

  public static final String WEBPB_CONTEXT = "";

  public static final String WEBPB_PATH = "";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  @JsonProperty("a")
  @JsonAlias("test1")
  private Integer test1;

  @JsonProperty("b")
  @JsonAlias("test2")
  private Level3 test2;

  @JsonProperty("c")
  @JsonAlias("test3")
  private List<Level3> test3;

  public Level2() {
  }

  public Level2(Integer test1, Level3 test2, List<Level3> test3) {
    this.test1 = test1;
    this.test2 = test2;
    this.test3 = test3;
  }
}
