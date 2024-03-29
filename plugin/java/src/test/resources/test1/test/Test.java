// Code generated by Webpb compiler, do not edit.
// https://github.com/jinganix/webpb
// Test.proto

package test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import include.Enum;
import include.Message;
import io.github.jinganix.webpb.runtime.Any;
import io.github.jinganix.webpb.runtime.WebpbMessage;
import io.github.jinganix.webpb.runtime.WebpbMeta;
import io.github.jinganix.webpb.runtime.common.InQuery;
import io.github.jinganix.webpb.tests.Const;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.ToString;

@ToString
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Test implements WebpbMessage {

  public static final String WEBPB_METHOD = "GET";

  public static final String WEBPB_CONTEXT = "/test";

  public static final String WEBPB_PATH = "/test/{test1}";

  public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

  @Override
  public WebpbMeta webpbMeta() {
    return WEBPB_META;
  }

  @InQuery
  private Integer test1;

  @NotNull
  private Message test2;

  private Enum test3;

  private Test4 test4;

  private Map<Long, Integer> test5;

  private Map<Integer, Message> test6;

  private Any test7;

  private Test.NestedTest test8;

  @JsonSerialize(using = ToStringSerializer.class)
  private Long test10;

  private List<Message> test11;

  private Message.Nested test12;

  private List<include2.Message> test13;

  private include2.Message.Nested test14;

  @Pattern(regexp = Const.REGEX)
  private String test15;

  private byte[] test16;

  private Test.Test17 test17;

  private Integer test18;

  private String test19;

  public Test() {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class NestedTest implements WebpbMessage {

    public static final String WEBPB_METHOD = "GET";

    public static final String WEBPB_CONTEXT = "/test";

    public static final String WEBPB_PATH = "/test/nested/{test1}";

    public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

    @Override
    public WebpbMeta webpbMeta() {
      return WEBPB_META;
    }

    private Integer test1;

    public NestedTest() {
    }

    public NestedTest(Integer test1) {
      this.test1 = test1;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Test17 implements WebpbMessage {

    public static final String WEBPB_METHOD = "";

    public static final String WEBPB_CONTEXT = "";

    public static final String WEBPB_PATH = "";

    public static final WebpbMeta WEBPB_META = new WebpbMeta.Builder().method(WEBPB_METHOD).context(WEBPB_CONTEXT).path(WEBPB_PATH).build();

    @Override
    public WebpbMeta webpbMeta() {
      return WEBPB_META;
    }

    private String test;

    public Test17() {
    }

    public Test17(String test) {
      this.test = test;
    }
  }
}
