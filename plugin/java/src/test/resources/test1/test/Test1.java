// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// Test.proto

package test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;
import test.message.InterfaceB;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Test1 implements WebpbMessage, InterfaceA, InterfaceB {

  public static final String WEBPB_METHOD = "GET";

  public static final String WEBPB_CONTEXT = "/test";

  public static final String WEBPB_PATH = "/test?a=123&b={test1}&c=321&d={test2}&e=456";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  private Long test1;

  private Integer test2;

  public Test1() {
  }

  public Test1(Long test1, Integer test2) {
    this.test1 = test1;
    this.test2 = test2;
  }
}
