syntax = "proto2";

import "webpb/WebpbExtend.proto";

// global file options
option (f_opts).java = {
  import: '${java17?then('jakarta', 'javax')}.validation.Valid'
  import: '${java17?then('jakarta', 'javax')}.validation.constraints.NotBlank'
  import: '${java17?then('jakarta', 'javax')}.validation.constraints.NotNull'
  import: 'org.hibernate.validator.constraints.Length'
  import: 'org.hibernate.validator.constraints.Range'
};

option (f_opts).ts = {
  int64_as_string: false
};
