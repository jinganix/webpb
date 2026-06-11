package io.github.jinganix.webpb.utilities.utils;

import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveMessage;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.Descriptors.Descriptor;
import io.github.jinganix.webpb.tests.Dump;
import io.github.jinganix.webpb.utilities.context.RequestContext;
import io.github.jinganix.webpb.utilities.test.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AliasUtils")
class AliasUtilsTest {

  @Test
  @DisplayName("should generate new alias when alias is in names")
  void shouldGenerateNewAliasWhenAliasIsInNames() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.alias_skip);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "AliasSkip");

    // When
    var aliases = AliasUtils.getAutoAliases(descriptor);

    // Then
    assertThat(aliases).hasSize(2).containsEntry("a", "c").containsEntry("b", "d");
  }
}
