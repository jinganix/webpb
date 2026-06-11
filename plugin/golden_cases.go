package goplugin_test

func fixtureCases() []string {
	return []string{
		"alias_skip",
		"auto_alias",
		"core_codegen",
		"enumeration",
		"message_extends",
		"generator_options",
		"imports",
	}
}

func proto2Dumps() []string {
	return prefixDumps("proto2", fixtureCases())
}

func proto3Dumps() []string {
	return prefixDumps("proto3", fixtureCases())
}

func prefixDumps(prefix string, cases []string) []string {
	out := make([]string, len(cases))
	for i, c := range cases {
		out[i] = prefix + "_" + c
	}
	return out
}

func isErrorDump(dump string) bool {
	return dump == "proto2_errors" || dump == "proto3_errors"
}
