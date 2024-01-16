package io.github.jinganix.webpb.utilities.utils;

import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveMessage;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Descriptors.Descriptor;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AliasUtils")
public class AliasUtilsTest {

  @Nested
  @DisplayName("getAutoAliases")
  class GetAutoAliases {

    @Nested
    @DisplayName("when alias is in names")
    class WhenAliasIsInNames {

      @Test
      @DisplayName("then generate new alias")
      void thenGenerateNewAlias() {
        RequestContext context = TestUtils.createRequest(Dump.test2);
        Descriptor descriptor = resolveMessage(context.getDescriptors(), "AliasSkip");
        assertThat(AliasUtils.getAutoAliases(descriptor))
            .hasSize(2)
            .containsEntry("a", "c")
            .containsEntry("b", "d");
      }
    }
  }
}
