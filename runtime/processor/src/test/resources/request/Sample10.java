import io.github.jinganix.webpb.processor.model.FooRequest;
import io.github.jinganix.webpb.runtime.mvc.WebpbRequestMapping;

class SubRequest extends FooRequest {
}

public class Sample10 {

    @WebpbRequestMapping
    void sample(SubRequest request) {
    }
}
