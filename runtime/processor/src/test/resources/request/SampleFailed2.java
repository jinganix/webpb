import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

public class SampleFailed2 {

    @WebpbRequestMapping(value = "/a", path = "/b")
    void sample(FooRequest request) {
    }
}
