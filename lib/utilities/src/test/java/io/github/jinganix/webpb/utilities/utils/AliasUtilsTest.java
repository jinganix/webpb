package io.github.jinganix.webpb.utilities.utils;

import static io.github.jinganix.webpb.utilities.utils.DescriptorUtils.resolveMessage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    RequestContext context = TestUtils.createRequest(Dump.proto2_alias_skip);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "AliasSkip");

    // When
    var aliases = AliasUtils.getAutoAliases(descriptor);

    // Then
    assertThat(aliases).hasSize(2).containsEntry("b", "c").containsEntry("a", "d");
  }

  @Test
  @DisplayName("should apply alias_reserve offset when generating auto aliases")
  void shouldApplyAliasReserveOffsetWhenGeneratingAutoAliases() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_auto_alias);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "AliasReserveChild");

    // When
    var aliases = AliasUtils.getAutoAliases(descriptor);

    // Then
    assertThat(aliases).containsEntry("foo_1", "b").containsEntry("foo_2", "g");
  }

  @Test
  @DisplayName("should not throw when alias_reserve is greater than max field number")
  void shouldNotThrowWhenAliasReserveIsGreaterThanMaxFieldNumber() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_auto_alias);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "AliasReserveParent");

    // When / Then
    assertThatCode(() -> AliasUtils.checkAliasReserve(descriptor)).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("should throw when alias_reserve is not greater than max field number")
  void shouldThrowWhenAliasReserveIsNotGreaterThanMaxFieldNumber() {
    // Given
    RequestContext context = TestUtils.createRequest(Dump.proto2_errors);
    Descriptor descriptor = resolveMessage(context.getDescriptors(), "InvalidAliasReserve");

    // When / Then
    assertThatThrownBy(() -> AliasUtils.checkAliasReserve(descriptor))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(
            "`alias_reserve` must be greater than max field number 1 in message"
                + " `InvalidAliasReserveProto.InvalidAliasReserve`");
  }
}
