// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// MessageOpts.proto

package auto.alias.message;

import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;
import java.util.List;

public class Level2 implements WebpbMessage {

  public static final String WEBPB_METHOD = "";

  public static final String WEBPB_CONTEXT = "";

  public static final String WEBPB_PATH = "";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  private Integer test1;

  private Level3 test2;

  private List<Level3> test3;

  public Level2() {
  }

  public Level2(Integer test1, Level3 test2, List<Level3> test3) {
    this.test1 = test1;
    this.test2 = test2;
    this.test3 = test3;
  }

  public Integer getTest1() {
    return this.test1;
  }

  public Level2 setTest1(Integer test1) {
    this.test1 = test1;
    return this;
  }

  public Level3 getTest2() {
    return this.test2;
  }

  public Level2 setTest2(Level3 test2) {
    this.test2 = test2;
    return this;
  }

  public List<Level3> getTest3() {
    return this.test3;
  }

  public Level2 setTest3(List<Level3> test3) {
    this.test3 = test3;
    return this;
  }
}
