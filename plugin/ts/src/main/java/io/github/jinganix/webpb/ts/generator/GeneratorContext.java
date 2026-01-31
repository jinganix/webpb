package io.github.jinganix.webpb.ts.generator;

import static io.github.jinganix.webpb.utilities.utils.OptionUtils.getOpts;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.MessageOpts;
import io.github.jinganix.webpb.utilities.descriptor.WebpbExtend.OptMessageOpts;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/** GeneratorContext */
@Getter
public class GeneratorContext {

  private final Map<String, Descriptor> baseTypes = new HashMap<>();

  private final Map<String, List<Descriptor>> subTypes = new HashMap<>();

  /**
   * Constructor.
   *
   * @param descriptors List of {@link FileDescriptor}
   */
  public GeneratorContext(List<FileDescriptor> descriptors) {
    for (FileDescriptor fileDescriptor : descriptors) {
      for (Descriptor descriptor : fileDescriptor.getMessageTypes()) {
        OptMessageOpts opt = getOpts(descriptor, MessageOpts::hasOpt).getOpt();
        if (isNotEmpty(opt.getSubType())) {
          baseTypes.put(descriptor.getName(), descriptor);
        }
        if (isNotEmpty(opt.getExtends()) && !opt.getSubValuesList().isEmpty()) {
          subTypes.computeIfAbsent(opt.getExtends(), (k) -> new ArrayList<>()).add(descriptor);
        }
      }
    }
  }
}
